package com.booking.javaproject.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPagesAreAvailableForGuests() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/calendar"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/calendar/bookings")
                        .param("start", "2026-05-01T00:00:00+03:00")
                        .param("end", "2026-06-01T00:00:00+03:00"))
                .andExpect(status().isOk());
    }

    @Test
    void guestIsRedirectedFromProtectedPagesToLogin() throws Exception {
        mockMvc.perform(get("/bookings/new")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/admin/rooms")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanOpenBookingPagesButCannotOpenAdminPages() throws Exception {
        mockMvc.perform(get("/bookings/new"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenAdminPages() throws Exception {
        mockMvc.perform(get("/admin/rooms"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/equipment"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateRoomThroughApi() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number": "401",
                                  "name": "Seminar room",
                                  "capacity": 20,
                                  "floor": 4
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
