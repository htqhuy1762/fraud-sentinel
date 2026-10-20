package com.fraudsentinel.scoring.alert;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertRepository repository;

    public AlertController(AlertRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AlertView> list(@RequestParam(defaultValue = "OPEN") AlertStatus status) {
        return repository.findTop50ByStatusOrderByCreatedAtDesc(status)
                .stream().map(AlertView::of).toList();
    }
}