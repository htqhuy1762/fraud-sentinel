package com.fraudsentinel.scoring.rule;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RuleConfigService {

    private final RuleConfigRepository repository;
    private final RuleConfigAuditRepository audits;
    private final RuleConfigProvider provider;
    private final JsonMapper json;

    public RuleConfigService(RuleConfigRepository repository, RuleConfigAuditRepository audits,
                             RuleConfigProvider provider, JsonMapper json) {
        this.repository = repository;
        this.audits = audits;
        this.provider = provider;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<RuleConfigView> list() {
        return repository.findAll().stream().map(this::view).toList();
    }

    @Transactional
    public RuleConfigView update(String code, UpdateRuleRequest req, String actor) {
        RuleConfigEntity e = repository.findById(code).orElseThrow(() -> new RuleNotFoundException(code));
        if (!e.getVersion().equals(req.version())) {
            throw new StaleRuleConfigException(code, req.version(), e.getVersion());
        }
        String paramsJson = json.writeValueAsString(req.params());
        provider.validateParams(code, paramsJson);

        String before = describe(e);
        Instant now = Instant.now();
        e.update(req.enabled(), req.score(), paramsJson, actor, now);
        audits.save(new RuleConfigAudit(code, before, describe(e), actor, now));
        return view(e);
    }

    private String describe(RuleConfigEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", e.isEnabled());
        m.put("score", e.getScore());
        m.put("params", json.readTree(e.getParams()));
        return json.writeValueAsString(m);
    }

    private RuleConfigView view(RuleConfigEntity e) {
        return new RuleConfigView(e.getCode(), e.isEnabled(), e.getScore(), json.readTree(e.getParams()),
                e.getVersion(), e.getUpdatedBy(), e.getUpdatedAt());
    }
}