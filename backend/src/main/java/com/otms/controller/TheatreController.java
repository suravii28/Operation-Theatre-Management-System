package com.otms.controller;

import com.otms.dto.TheatreResponse;
import com.otms.repository.TheatreRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreRepository theatreRepository;

    public TheatreController(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @GetMapping
    public List<TheatreResponse> list() {
        return theatreRepository.findByActiveTrueOrderByName().stream()
                .map(t -> new TheatreResponse(t.getId(), t.getName(), t.getSpeciality()))
                .toList();
    }
}
