package com.fraudsentinel.scoring.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RuleConfigProvider {

    private static final Logger log = LoggerFactory.getLogger(RuleConfigProvider.class);

    private final RuleConfigRepository repository;
    private final JsonMapper json;
    private final Map<String, Rule<?>> rulesByCode;

    /** Snapshot bất biến; được thay NGUYÊN CẢ MAP trong một phép gán. */
    private volatile Map<String, RuleConfig> snapshot = Map.of();

    public RuleConfigProvider(RuleConfigRepository repository, JsonMapper json, List<Rule<?>> rules) {
        this.repository = repository;
        this.json = json;
        this.rulesByCode = rules.stream().collect(Collectors.toMap(Rule::code, Function.identity()));
        reload();   // nạp lần đầu ngay khi khởi động; DB lỗi → app không khởi động (fail fast)
    }

    public RuleConfig get(String code) {
        return snapshot.get(code);
    }

    /** Làm mới định kỳ. Lỗi → giữ nguyên snapshot cũ, scoring không bị ảnh hưởng. */
    @Scheduled(fixedDelayString = "${app.rules.refresh-ms}", initialDelayString = "${app.rules.refresh-ms}")
    public void refresh() {
        try {
            reload();
        } catch (Exception e) {
            log.error("Rule config refresh failed, keeping previous snapshot", e);
        }
    }

    public synchronized void reload() {
        Map<String, RuleConfig> next = new HashMap<>();
        for (RuleConfigEntity e : repository.findAll()) {
            Rule<?> rule = rulesByCode.get(e.getCode());
            if (rule == null) {
                log.warn("Config found for unknown rule {}, ignored", e.getCode());
                continue;
            }
            try {
                Object params = json.readValue(e.getParams(), rule.paramsType());
                next.put(e.getCode(), new RuleConfig(e.getCode(), e.isEnabled(), e.getScore(), params, e.getVersion()));
            } catch (JacksonException ex) {
                RuleConfig previous = snapshot.get(e.getCode());
                log.error("Invalid params for rule {} (version {}), keeping previous: {}",
                        e.getCode(), e.getVersion(), previous, ex);
                if (previous != null) next.put(e.getCode(), previous);
            }
        }
        rulesByCode.keySet().stream()
                .filter(code -> !next.containsKey(code))
                .forEach(code -> log.warn("Rule {} has no config, it will not run", code));

        if (!next.equals(snapshot)) {
            log.info("Rule config loaded: {}", next.values());
        }
        snapshot = Map.copyOf(next);
    }

    /** Dùng khi admin cập nhật: kiểm tra params TRƯỚC khi lưu. */
    public void validateParams(String code, String paramsJson) {
        Rule<?> rule = rulesByCode.get(code);
        try {
            json.readValue(paramsJson, rule.paramsType());
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid params for " + code + ": " + e.getOriginalMessage());
        }
    }
}