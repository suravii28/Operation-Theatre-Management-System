
package com.otms.service;

import com.otms.dto.SurgeryRequest;
import com.otms.model.Role;
import com.otms.model.SurgeryStatus;
import com.otms.model.Theatre;
import com.otms.model.UserAccount;
import com.otms.repository.SurgeryRepository;
import com.otms.repository.TheatreRepository;
import com.otms.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SchedulingConcurrencyTest {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private SurgeryRepository surgeryRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void concurrentSchedulingOfSameSlotNeverDoubleBooks() throws Exception {
        LocalDate day = LocalDate.of(2031, 6, 1);

        Theatre theatre = theatreRepository.findAll().stream()
                .filter(t -> "CONC-OT".equals(t.getName()))
                .findFirst()
                .orElseGet(() -> theatreRepository.save(
                        new Theatre("CONC-OT", "Concurrency Test")));

        UserAccount surgeon = userAccountRepository.findByEmail("conc.surgeon@otms.local")
                .orElseGet(() -> userAccountRepository.save(
                        new UserAccount("conc.surgeon@otms.local", "unused",
                                "Dr. Conc Test", Role.DOCTOR)));

        // Create a dedicated nurse for this test instead of relying on seeded data.
        UserAccount nurse = userAccountRepository.findByEmail("conc.nurse@otms.local")
                .orElseGet(() -> userAccountRepository.save(
                        new UserAccount("conc.nurse@otms.local", "unused",
                                "Nurse Concurrency Test", Role.NURSE)));

        for (int round = 0; round < 5; round++) {
            LocalDateTime start = day.atTime(8, 0).plusHours(round * 2);
            LocalDateTime end = start.plusHours(2);

            int threads = 6;
            AtomicInteger successes = new AtomicInteger();
            AtomicInteger conflicts = new AtomicInteger();
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch startGun = new CountDownLatch(1);

            Long theatreId = theatre.getId();
            Long surgeonId = surgeon.getId();
            Long nurseId = nurse.getId();

            ExecutorService pool = Executors.newFixedThreadPool(threads);

            try {
                List<Future<Void>> jobs = java.util.stream.IntStream.range(0, threads)
                        .mapToObj(i -> pool.submit((java.util.concurrent.Callable<Void>) () -> {
                            ready.countDown();
                            startGun.await();

                            try {
                                schedulingService.schedule(new SurgeryRequest(
                                        "Concurrent patient " + i,
                                        "Concurrency probe",
                                        surgeonId,
                                        theatreId,
                                        nurseId,
                                        start,
                                        end,
                                        false,
                                        null,
                                        null));
                                successes.incrementAndGet();
                            } catch (SchedulingConflictException ex) {
                                conflicts.incrementAndGet();
                            }

                            return null;
                        }))
                        .toList();

                if (!ready.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("workers did not start");
                }

                startGun.countDown();

                for (Future<Void> job : jobs) {
                    job.get(30, TimeUnit.SECONDS);
                }
            } finally {
                pool.shutdownNow();
            }

            assertThat(successes.get())
                    .as("round %d: exactly one booking wins the slot", round)
                    .isEqualTo(1);

            assertThat(conflicts.get())
                    .as("round %d: everyone else gets a conflict", round)
                    .isEqualTo(threads - 1);

            assertThat(surgeryRepository.findOverlapping(
                    List.of(SurgeryStatus.SCHEDULED, SurgeryStatus.IN_PROGRESS),
                    start, end))
                    .as("round %d: database still holds exactly one surgery for the slot", round)
                    .hasSize(1);
        }
    }
}
