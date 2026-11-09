package com.fraudsentinel.scoring.deadletter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Bỏ qua một message là quyết định có hậu quả (có thể là mất một alert), nên bắt buộc ghi lý do. */
public record DiscardRequest(@NotBlank @Size(max = 1000) String note) {}
