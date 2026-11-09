package com.fraudsentinel.scoring.deadletter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dead-letters")
public class DeadLetterController {

    private static final String OPERATOR_HEADER = "X-Analyst-Id";

    private final DeadLetterService service;

    public DeadLetterController(DeadLetterService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeadLetterView> list(@RequestParam(defaultValue = "NEW") DeadLetterStatus status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    public DeadLetterView get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/replay")
    public DeadLetterView replay(@PathVariable Long id, @RequestHeader(OPERATOR_HEADER) String operator) {
        return service.replay(id, operator);
    }

    @PostMapping("/{id}/discard")
    public DeadLetterView discard(@PathVariable Long id, @RequestHeader(OPERATOR_HEADER) String operator,
                                  @Valid @RequestBody DiscardRequest request) {
        return service.discard(id, operator, request.note());
    }
}
