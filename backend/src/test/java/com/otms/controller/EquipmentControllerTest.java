package com.otms.controller;

import com.otms.config.JwtAuthenticationFilter;
import com.otms.config.JwtService;
import com.otms.config.SecurityConfig;
import com.otms.model.Equipment;
import com.otms.repository.EquipmentRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
@Import({SecurityConfig.class, EquipmentControllerTest.TestConfig.class})
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentRepository equipmentRepository;

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
    void listsAvailableEquipmentByDefault() throws Exception {
        when(equipmentRepository.findByAvailableTrueOrderByName()).thenReturn(List.of(
                equipment(1L, "C-arm", "Imaging", true)));

        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("C-arm"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void includesUnavailableEquipmentWhenRequested() throws Exception {
        when(equipmentRepository.findAllByOrderByName()).thenReturn(List.of(
                equipment(1L, "C-arm", "Imaging", true),
                equipment(4L, "Heart-lung machine", "Perfusion", false)));

        mockMvc.perform(get("/api/equipment?includeUnavailable=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void technicianCanToggleEquipmentStatus() throws Exception {
        Equipment machine = equipment(4L, "Heart-lung machine", "Perfusion", false);
        when(equipmentRepository.findById(4L)).thenReturn(Optional.of(machine));
        when(equipmentRepository.save(any())).thenAnswer(invocation -> {
            Equipment e = invocation.getArgument(0);
            return e;
        });

        mockMvc.perform(patch("/api/equipment/4/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanToggleEquipmentStatus() throws Exception {
        Equipment machine = equipment(4L, "Heart-lung machine", "Perfusion", true);
        when(equipmentRepository.findById(4L)).thenReturn(Optional.of(machine));
        when(equipmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/equipment/4/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void nurseCannotToggleEquipmentStatus() throws Exception {
        mockMvc.perform(patch("/api/equipment/4/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": false}"))
                .andExpect(status().isForbidden());

        verify(equipmentRepository, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void unknownEquipmentReturns404() throws Exception {
        when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/equipment/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void missingAvailableFieldRejected() throws Exception {
        when(equipmentRepository.findById(4L)).thenReturn(Optional.of(equipment(4L, "Heart-lung machine", "Perfusion", true)));

        mockMvc.perform(patch("/api/equipment/4/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Equipment equipment(Long id, String name, String type, boolean available) {
        Equipment e = new Equipment(name, type);
        org.springframework.test.util.ReflectionTestUtils.setField(e, "id", id);
        e.setAvailable(available);
        return e;
    }
}
