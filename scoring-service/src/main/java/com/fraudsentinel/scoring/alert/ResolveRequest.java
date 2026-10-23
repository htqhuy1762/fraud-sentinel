package com.fraudsentinel.scoring.alert;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveRequest(@NotNull AlertStatus outcome, @Size(max = 1000) String note) {}