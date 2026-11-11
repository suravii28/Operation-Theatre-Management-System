package com.otms.dto;

import jakarta.validation.constraints.NotNull;

public record EquipmentRequest(
        @NotNull Boolean available
) {
}
