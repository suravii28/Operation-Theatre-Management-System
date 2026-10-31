package com.otms.controller;

import com.otms.dto.StaffResponse;
import com.otms.model.Role;
import com.otms.repository.UserAccountRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final UserAccountRepository userAccountRepository;

    public StaffController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public List<StaffResponse> list(@RequestParam(required = false) String role) {
        if (role == null || role.isBlank()) {
            return userAccountRepository.findAll().stream()
                    .map(u -> new StaffResponse(u.getId(), u.getName(), u.getRole()))
                    .toList();
        }

        Role parsed = Arrays.stream(Role.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + role));

        return userAccountRepository.findByRoleAndActiveTrue(parsed).stream()
                .map(u -> new StaffResponse(u.getId(), u.getName(), u.getRole()))
                .toList();
    }
}
