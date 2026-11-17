package com.otms.controller;

import com.otms.dto.SurgeryRequest;
import com.otms.dto.SurgeryResponse;
import com.otms.model.SurgeryStatus;
import com.otms.service.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surgeries")
public class SurgeryController {

    private final SchedulingService schedulingService;

    public SurgeryController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping
    public List<SurgeryResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) SurgeryStatus status) {
        return schedulingService.list(date, theatreId, status);
    }

    @GetMapping("/emergencies")
    public List<SurgeryResponse> emergencies() {
        return schedulingService.listEmergencies();
    }

    @GetMapping("/{id}")
    public SurgeryResponse get(@PathVariable Long id) {
        return schedulingService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SurgeryResponse create(@Valid @RequestBody SurgeryRequest request) {
        return schedulingService.schedule(request);
    }

    @PatchMapping("/{id}/status")
    public SurgeryResponse updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SurgeryStatus status = SurgeryStatus.valueOf(body.get("status"));
        return schedulingService.updateStatus(id, status);
    }
}
