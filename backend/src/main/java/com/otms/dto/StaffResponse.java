package com.otms.dto;

import com.otms.model.Role;

public record StaffResponse(
        Long id,
        String name,
        Role role
) {
}
