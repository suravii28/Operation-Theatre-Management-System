package com.otms.dto;

import com.otms.model.Role;

public record LoginResponse(
        String token,
        String name,
        Role role
) {
}
