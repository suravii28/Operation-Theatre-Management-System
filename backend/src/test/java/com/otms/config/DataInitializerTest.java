package com.otms.config;

import com.otms.dto.SurgeryResponse;
import com.otms.service.SchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Verifies the seeded emergency-impact scenario used by the system validation
 * suite (TC-EMG-02/TC-EMG-03): the seeded emergency case in OT-1 displaces the
 * seeded routine surgery sharing its slot, so impact analysis returns it.
 *
 * Demo seed data is intentionally disabled in the prod profile, so these
 * seed-specific checks are skipped when the PostgreSQL production-profile
 * integration suite runs.
 */
@SpringBootTest
class DataInitializerTest {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private Environment environment;

    private void assumeDemoSeedDataIsEnabled() {
        assumeFalse(Arrays.asList(environment.getActiveProfiles()).contains("prod"),
                "Demo seed data is disabled in the prod profile");
    }

    @Test
    void seededEmergencyHasADeterministicDisplacedSurgery() {
        assumeDemoSeedDataIsEnabled();

        List<SurgeryResponse> emergencies = schedulingService.listEmergencies();

        SurgeryResponse emergency = emergencies.stream()
                .filter(s -> "Emergency laparotomy".equals(s.procedureName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seed emergency 'Emergency laparotomy' not found"));

        List<SurgeryResponse> impact = schedulingService.impactAnalysis(emergency.id());

        assertThat(impact).hasSize(1);
        assertThat(impact.get(0).patientName()).isEqualTo("Mohan Verma");
        assertThat(impact.get(0).theatreName()).isEqualTo("OT-1");
    }

    @Test
    void seededSurgeonDoubleBookingExistsForConflictDemo() {
        assumeDemoSeedDataIsEnabled();

        List<SurgeryResponse> all = schedulingService.list(null, null, null);

        long drRamToday = all.stream()
                .filter(s -> "Dr. Ram Sharma".equals(s.leadSurgeonName()))
                .filter(s -> s.emergency() == Boolean.FALSE || !s.emergency())
                .count();

        assertThat(drRamToday).isGreaterThanOrEqualTo(2);
    }
}
