package com.fraudsentinel.scoring.deadletter;

import com.fraudsentinel.scoring.infra.KafkaSendSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final DeadLetterRepository repository;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock;

    public DeadLetterService(DeadLetterRepository repository, KafkaTemplate<String, String> kafka, Clock clock) {
        this.repository = repository;
        this.kafka = kafka;
        this.clock = clock;
    }

    /** Ghi một message từ topic DLT vào bảng dead_letter. Đọc lại lần hai (redelivery) thì bỏ qua. */
    @Transactional
    public void record(ConsumerRecord<String, String> r) {
        if (repository.existsByDltTopicAndDltPartitionAndDltOffset(r.topic(), r.partition(), r.offset())) {
            return;
        }
        DeadLetter saved = repository.save(DeadLetter.received(
                r.topic(), r.partition(), r.offset(),
                truncate(stringHeader(r, KafkaHeaders.DLT_ORIGINAL_TOPIC), 128),
                intHeader(r, KafkaHeaders.DLT_ORIGINAL_PARTITION),
                longHeader(r, KafkaHeaders.DLT_ORIGINAL_OFFSET),
                truncate(r.key(), 256),
                r.value(),
                truncate(stringHeader(r, KafkaHeaders.DLT_EXCEPTION_FQCN), 512),
                stringHeader(r, KafkaHeaders.DLT_EXCEPTION_MESSAGE),
                Instant.now(clock)));
        // ERROR: mỗi dead letter là một việc cần người xử lý, phải nổi bật trong log và cảnh báo
        log.error("Dead letter recorded id={} from {} (key={}): {} - {}",
                saved.getId(), saved.getOriginalTopic(), saved.getMessageKey(),
                saved.getExceptionClass(), saved.getExceptionMessage());
    }

    @Transactional(readOnly = true)
    public List<DeadLetterView> list(DeadLetterStatus status) {
        return repository.findTop100ByStatusOrderByReceivedAtDesc(status).stream().map(DeadLetterView::of).toList();
    }

    @Transactional(readOnly = true)
    public DeadLetterView get(Long id) {
        return DeadLetterView.of(find(id));
    }

    /**
     * Gửi lại message vào topic gốc. Thứ tự có chủ đích:
     * 1. đổi trạng thái và FLUSH: nếu hai người cùng bấm replay, @Version chặn người sau TRƯỚC khi gửi;
     * 2. gửi Kafka và chờ ack: lỗi thì transaction rollback, trạng thái vẫn là NEW;
     * 3. commit. Nếu commit lỗi sau khi đã gửi, message có thể bị replay lại lần nữa,
     *    nhưng các consumer đều idempotent nên không gây hại.
     */
    @Transactional
    public DeadLetterView replay(Long id, String actor) {
        DeadLetter d = find(id);
        d.markReplayed(actor, Instant.now(clock));
        repository.saveAndFlush(d);
        KafkaSendSupport.await(kafka.send(d.getOriginalTopic(), d.getMessageKey(), d.getPayload()),
                "replay of dead letter " + id);
        log.info("Dead letter {} replayed to {} by {}", id, d.getOriginalTopic(), actor);
        return DeadLetterView.of(d);
    }

    @Transactional
    public DeadLetterView discard(Long id, String actor, String note) {
        DeadLetter d = find(id);
        d.discard(actor, note, Instant.now(clock));
        log.warn("Dead letter {} discarded by {}: {}", id, actor, note);
        return DeadLetterView.of(d);
    }

    private DeadLetter find(Long id) {
        return repository.findById(id).orElseThrow(() -> new DeadLetterNotFoundException(id));
    }

    // ─── Đọc header mà DeadLetterPublishingRecoverer gắn vào ───

    private static byte[] headerBytes(ConsumerRecord<?, ?> r, String name) {
        Header h = r.headers().lastHeader(name);
        return h == null ? null : h.value();
    }

    private static String stringHeader(ConsumerRecord<?, ?> r, String name) {
        byte[] b = headerBytes(r, name);
        return b == null ? null : new String(b, StandardCharsets.UTF_8);
    }

    /** Partition gốc được ghi dạng int 4 byte. */
    private static Integer intHeader(ConsumerRecord<?, ?> r, String name) {
        byte[] b = headerBytes(r, name);
        return b != null && b.length == Integer.BYTES ? ByteBuffer.wrap(b).getInt() : null;
    }

    /** Offset gốc được ghi dạng long 8 byte. */
    private static Long longHeader(ConsumerRecord<?, ?> r, String name) {
        byte[] b = headerBytes(r, name);
        return b != null && b.length == Long.BYTES ? ByteBuffer.wrap(b).getLong() : null;
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
