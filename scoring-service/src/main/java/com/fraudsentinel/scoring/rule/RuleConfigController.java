package com.fraudsentinel.scoring.rule;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleConfigController {

    private final RuleConfigService service;
    private final RuleConfigProvider provider;

    public RuleConfigController(RuleConfigService service, RuleConfigProvider provider) {
        this.service = service;
        this.provider = provider;
    }

    @GetMapping
    public List<RuleConfigView> list() {
        return service.list();
    }

    @PutMapping("/{code}")
    public RuleConfigView update(@PathVariable String code,
                                 @RequestHeader("X-Analyst-Id") String actor,
                                 @Valid @RequestBody UpdateRuleRequest request) {
        RuleConfigView updated = service.update(code, request, actor);
        provider.reload();   // instance này áp dụng ngay; instance khác nhận trong ≤ refresh-ms
        return updated;
    }
}