package com.fraudsentinel.simulator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TransactionGenerator {

    private final SimulatorProperties props;

    public TransactionGenerator(SimulatorProperties props) {
        this.props = props;
    }

    /** Giao dịch bình thường: phần lớn số tiền nhỏ, thỉnh thoảng vài triệu. */
    public SimTransaction normal() {
        double r = rnd().nextDouble();
        long amount = 50_000 + (long) (r * r * r * 5_000_000);
        return build(randomAccount(), roundThousand(amount));
    }

    /** Kịch bản 1: một giao dịch rất lớn. */
    public SimTransaction highAmount() {
        long amount = rnd().nextLong(80_000_000, 200_000_000);
        return build(randomAccount(), roundThousand(amount));
    }

    /** Kịch bản 2: một tài khoản bắn liên tiếp nhiều giao dịch nhỏ (card testing / account takeover). */
    public List<SimTransaction> burst() {
        String account = randomAccount();
        List<SimTransaction> txns = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            txns.add(build(account, roundThousand(rnd().nextLong(10_000, 200_000))));
        }
        return txns;
    }

    private SimTransaction build(String accountId, long amount) {
        return new SimTransaction(
                UUID.randomUUID().toString(),
                accountId,
                BigDecimal.valueOf(amount),
                "VND",
                "MOBILE",
                randomAccount(),
                "DEV-" + accountId.substring(3),
                "10.0.%d.%d".formatted(rnd().nextInt(256), rnd().nextInt(256)),
                Instant.now());
    }

    private String randomAccount() {
        return "ACC%05d".formatted(rnd().nextInt(props.accounts()));
    }

    private static long roundThousand(long v) { return v / 1000 * 1000; }

    private static ThreadLocalRandom rnd() { return ThreadLocalRandom.current(); }
}