package com.otms.controller;

import com.otms.config.JwtAuthenticationFilter;
import com.otms.config.JwtService;
import com.otms.config.SecurityConfig;
import com.otms.model.Theatre;
import com.otms.repository.TheatreRepository;
import com.otms.repository.UserAccountRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TheatreController.class)
@Import({SecurityConfig.class, TheatreControllerTest.TestConfig.class})
class TheatreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TheatreRepository theatreRepository;

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
    @WithMockUser(roles = "COORDINATOR")
    void listsActiveTheatresByDefault() throws Exception {
        when(theatreRepository.findByActiveTrueOrderByName()).thenReturn(List.of(
                theatre(1L, "OT-1", "General Surgery", true)));

        mockMvc.perform(get("/api/theatres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("OT-1"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void includesInactiveTheatresWhenRequested() throws Exception {
        when(theatreRepository.findAllByOrderByName()).thenReturn(List.of(
                theatre(1L, "OT-1", "General Surgery", true),
                theatre(2L, "OT-2", "Cardiac", false)));

        mockMvc.perform(get("/api/theatres?includeInactive=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void technicianCanToggleTheatreAvailability() throws Exception {
        Theatre ot2 = theatre(2L, "OT-2", "Cardiac", true);
        when(theatreRepository.findById(2L)).thenReturn(java.util.Optional.of(ot2));
        when(theatreRepository.save(any())).thenAnswer(invocation -> {
            Theatre t = invocation.getArgument(0);
            t.setActive(false);
            return t;
        });

        mockMvc.perform(patch("/api/theatres/2/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(theatreRepository).save(any(Theatre.class));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void nurseCannotToggleTheatreAvailability() throws Exception {
        mockMvc.perform(patch("/api/theatres/2/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isForbidden());

        verify(theatreRepository, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void unknownTheatreReturns404() throws Exception {
        when(theatreRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(patch("/api/theatres/99/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void missingActiveFieldRejected() throws Exception {
        when(theatreRepository.findById(2L)).thenReturn(java.util.Optional.of(theatre(2L, "OT-2", "Cardiac", true)));

        mockMvc.perform(patch("/api/theatres/2/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Theatre theatre(Long id, String name, String speciality, boolean active) {
        Theatre t = new Theatre(name, speciality);
        org.springframework.test.util.ReflectionTestUtils.setField(t, "id", id);
        t.setActive(active);
        return t;
    }
}
