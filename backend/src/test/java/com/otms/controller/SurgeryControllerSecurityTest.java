package com.otms.controller;

import com.otms.config.JwtAuthenticationFilter;
import com.otms.config.JwtService;
import com.otms.config.SecurityConfig;
import com.otms.repository.UserAccountRepository;
import com.otms.service.SchedulingConflictException;
import com.otms.service.SchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurgeryController.class)
@Import({SecurityConfig.class, SurgeryControllerSecurityTest.TestConfig.class})
class SurgeryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchedulingService schedulingService;

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
    void surgeriesEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/surgeries"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COORDINATOR")
    void authenticatedUserCanListSurgeries() throws Exception {
        when(schedulingService.list(isNull(), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/surgeries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void invalidSurgeryPayloadIsRejected() throws Exception {
        mockMvc.perform(post("/api/surgeries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "COORDINATOR")
    void schedulingConflictReturns409() throws Exception {
        when(schedulingService.schedule(any()))
                .thenThrow(new SchedulingConflictException("Scheduling conflict detected: [OT-1 occupied]"));

        mockMvc.perform(post("/api/surgeries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientName": "Test Patient",
                                  "procedureName": "Appendectomy",
                                  "leadSurgeonId": 10,
                                  "theatreId": 1,
                                  "startTime": "2030-01-10T09:00",
                                  "endTime": "2030-01-10T11:00"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Scheduling conflict detected: [OT-1 occupied]"));
    }
}
