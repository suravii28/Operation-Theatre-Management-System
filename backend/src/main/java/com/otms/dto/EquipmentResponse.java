package com.otms.dto;

public record EquipmentResponse(
        Long id,
        String name,
        String type,
        boolean available
) {
}
