package com.otms.controller;

import com.otms.config.JwtAuthenticationFilter;
import com.otms.config.JwtService;
import com.otms.config.SecurityConfig;
import com.otms.dto.NotificationResponse;
import com.otms.model.NotificationType;
import com.otms.repository.UserAccountRepository;
import com.otms.service.NotificationService;
import com.otms.service.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, NotificationControllerTest.TestConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserAccountRepository userAccountRepository;

    @MockBean
    private JwtService jwtService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
            return new JwtAuthenticationFilter(jwtService);
        }
    }

    @Test
    void notificationsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void authenticatedUserCanListNotifications() throws Exception {
        when(notificationService.list()).thenReturn(List.of(
                new NotificationResponse(1L, NotificationType.EMERGENCY_SCHEDULED,
                        "Emergency surgery scheduled: 'Laparotomy' for Lakshmi in OT-1",
                        7L, LocalDateTime.of(2030, 1, 10, 9, 5), false)));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("EMERGENCY_SCHEDULED"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void markReadReturnsUpdatedNotification() throws Exception {
        when(notificationService.markRead(1L)).thenReturn(
                new NotificationResponse(1L, NotificationType.SURGERY_SCHEDULED,
                        "Surgery scheduled: 'Appendectomy' for Ravi in OT-1",
                        5L, LocalDateTime.of(2030, 1, 10, 9, 0), true));

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void unknownNotificationReturns404() throws Exception {
        when(notificationService.markRead(99L)).thenThrow(new ResourceNotFoundException("Notification not found: 99"));

        mockMvc.perform(patch("/api/notifications/99/read"))
                .andExpect(status().isNotFound());
    }
}
