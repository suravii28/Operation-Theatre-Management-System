package com.otms.controller;

import com.otms.dto.TheatreResponse;
import com.otms.model.Theatre;
import com.otms.repository.TheatreRepository;
import com.otms.service.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreRepository theatreRepository;

    public TheatreController(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @GetMapping
    public List<TheatreResponse> list(@RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        List<Theatre> theatres = includeInactive
                ? theatreRepository.findAllByOrderByName()
                : theatreRepository.findByActiveTrueOrderByName();
        return theatres.stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public TheatreResponse updateAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        if (active == null) {
            throw new IllegalArgumentException("Field 'active' is required");
        }

        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + id));
        theatre.setActive(active);
        return toResponse(theatreRepository.save(theatre));
    }

    private TheatreResponse toResponse(Theatre theatre) {
        return new TheatreResponse(theatre.getId(), theatre.getName(), theatre.getSpeciality(), theatre.isActive());
    }
}
