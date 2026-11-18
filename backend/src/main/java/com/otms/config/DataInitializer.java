
package com.otms.config;

import com.otms.model.*;
import com.otms.repository.EquipmentRepository;
import com.otms.repository.NotificationRepository;
import com.otms.repository.SurgeryRepository;
import com.otms.repository.TheatreRepository;
import com.otms.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!prod")
public class DataInitializer implements ApplicationRunner {

        private final UserAccountRepository userRepository;
        private final TheatreRepository theatreRepository;
        private final SurgeryRepository surgeryRepository;
        private final EquipmentRepository equipmentRepository;
        private final NotificationRepository notificationRepository;
        private final PasswordEncoder passwordEncoder;

        public DataInitializer(
                        UserAccountRepository userRepository,
                        TheatreRepository theatreRepository,
                        SurgeryRepository surgeryRepository,
                        EquipmentRepository equipmentRepository,
                        NotificationRepository notificationRepository,
                        PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.theatreRepository = theatreRepository;
                this.surgeryRepository = surgeryRepository;
                this.equipmentRepository = equipmentRepository;
                this.notificationRepository = notificationRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(ApplicationArguments args) {
                user("admin@otms.local", "Admin123!", "Asha Admin", Role.ADMIN);
                user("coordinator@otms.local", "Coord123!", "Kiran Coordinator", Role.COORDINATOR);

                UserAccount drRam = user(
                                "dr.ram@otms.local", "Doctor123!", "Dr. Ram Sharma", Role.DOCTOR);

                UserAccount drPriya = user(
                                "dr.priya@otms.local", "Doctor123!", "Dr. Priya Nair", Role.DOCTOR);

                UserAccount nurse = user(
                                "nurse@otms.local", "Nurse123!", "Meera Nurse", Role.NURSE);

                user("tech@otms.local", "Tech123!", "Arun Technician", Role.TECHNICIAN);

                Theatre ot1 = theatre("OT-1", "General Surgery");
                Theatre ot2 = theatre("OT-2", "Cardiac");
                Theatre ot3 = theatre("OT-3", "Orthopaedic");

                Equipment laparoscope = equipment("Laparoscope", "Imaging", true);
                Equipment cArm = equipment("C-arm", "Imaging", true);
                Equipment defibrillator = equipment("Defibrillator", "Monitoring", true);

                // In maintenance — demonstrates the equipment availability check.
                equipment("Heart-lung machine", "Perfusion", false);

                LocalDate today = LocalDate.now();

                // Deliberate surgeon double-booking for the conflict demonstration.
                // Meera Nurse is assigned to the first seeded surgery.
                surgery("Ravi Kumar", "Appendectomy", drRam, ot1,
                                today.atTime(9, 0), today.atTime(11, 0),
                                false, "Routine", nurse, laparoscope);

                surgery("Sunita Devi", "Angioplasty", drRam, ot2,
                                today.atTime(10, 0), today.atTime(12, 0),
                                false, "Demo conflict: Dr. Ram double-booked", defibrillator);

                surgery("Imran Khan", "Hip replacement", drPriya, ot3,
                                today.atTime(13, 30), today.atTime(16, 0),
                                false, null, cArm);

                // Emergency-impact scenario for testing assisted rescheduling.
                surgery("Mohan Verma", "Gallbladder removal", drPriya, ot1,
                                today.plusDays(2).atTime(15, 0),
                                today.plusDays(2).atTime(17, 0),
                                false, "Displaced by emergency");

                surgery("Lakshmi Rao", "Emergency laparotomy", drPriya, ot1,
                                today.plusDays(2).atTime(15, 0),
                                today.plusDays(2).atTime(17, 0),
                                true, "Emergency case - prioritise");

                surgery("Vikram Singh", "Knee arthroscopy", drRam, ot3,
                                today.plusDays(1).atTime(9, 30),
                                today.plusDays(1).atTime(11, 0),
                                false, null, cArm);

                surgery("Anita Desai", "Pacemaker implant", drPriya, ot2,
                                today.plusDays(1).atTime(14, 0),
                                today.plusDays(1).atTime(16, 30),
                                false, null, defibrillator);

                // Seed one unread notification for the coordinator demo.
                // Notifications are shared because the current entity has no recipient field.
                if (notificationRepository.count() == 0) {
                        notificationRepository.save(new Notification(
                                        NotificationType.EMERGENCY_SCHEDULED,
                                        "Emergency surgery scheduled: 'Emergency laparotomy' for Lakshmi Rao in OT-1",
                                        null));
                }
        }

        private UserAccount user(String email, String password, String name, Role role) {
                return userRepository.save(
                                new UserAccount(email, passwordEncoder.encode(password), name, role));
        }

        private Theatre theatre(String name, String speciality) {
                return theatreRepository.save(new Theatre(name, speciality));
        }

        private Equipment equipment(String name, String type, boolean available) {
                Equipment e = new Equipment(name, type);
                e.setAvailable(available);
                return equipmentRepository.save(e);
        }

        // Standard surgery helper, with optional equipment.
        private void surgery(
                        String patient,
                        String procedure,
                        UserAccount surgeon,
                        Theatre theatre,
                        LocalDateTime start,
                        LocalDateTime end,
                        boolean emergency,
                        String notes,
                        Equipment... equipment) {

                surgery(patient, procedure, surgeon, theatre, start, end,
                                emergency, notes, null, equipment);
        }

        // Surgery helper supporting both an assigned nurse and equipment.
        private void surgery(
                        String patient,
                        String procedure,
                        UserAccount surgeon,
                        Theatre theatre,
                        LocalDateTime start,
                        LocalDateTime end,
                        boolean emergency,
                        String notes,
                        UserAccount assignedNurse,
                        Equipment... equipment) {

                Surgery s = new Surgery();
                s.setPatientName(patient);
                s.setProcedureName(procedure);
                s.setLeadSurgeon(surgeon);
                s.setTheatre(theatre);
                s.setAssignedNurse(assignedNurse);
                s.setStartTime(start);
                s.setEndTime(end);
                s.setEmergency(emergency);
                s.setNotes(notes);
                s.setStatus(SurgeryStatus.SCHEDULED);
                s.getEquipment().addAll(List.of(equipment));

                surgeryRepository.save(s);
        }

        private void surgery(
                        String patient,
                        String procedure,
                        UserAccount surgeon,
                        Theatre theatre,
                        LocalDateTime start,
                        boolean emergency) {

                surgery(patient, procedure, surgeon, theatre,
                                start, start.plusHours(2), emergency,
                                emergency ? "Emergency case - prioritise" : null);
        }
}
