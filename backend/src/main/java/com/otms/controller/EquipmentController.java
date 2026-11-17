package com.otms.controller;

import com.otms.dto.EquipmentRequest;
import com.otms.dto.EquipmentResponse;
import com.otms.model.Equipment;
import com.otms.repository.EquipmentRepository;
import com.otms.service.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;

    public EquipmentController(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @GetMapping
    public List<EquipmentResponse> list(
            @RequestParam(required = false, defaultValue = "false") boolean includeUnavailable) {
        List<Equipment> equipment = includeUnavailable
                ? equipmentRepository.findAllByOrderByName()
                : equipmentRepository.findByAvailableTrueOrderByName();
        return equipment.stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public EquipmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + id));
        equipment.setAvailable(request.available());
        return toResponse(equipmentRepository.save(equipment));
    }

    private EquipmentResponse toResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(), equipment.getName(), equipment.getType(), equipment.isAvailable());
    }
}
