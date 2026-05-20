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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    void unknownPublicPagesUseNotFoundTemplate() throws Exception {
        mockMvc.perform(get("/not-existing-page")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanOpenBookingPagesButCannotOpenAdminPages() throws Exception {
        mockMvc.perform(get("/bookings/new"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/rooms")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void forbiddenPageUsesCustomTemplate() throws Exception {
        mockMvc.perform(get("/error/403")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
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

    @Test
    @WithMockUser(roles = "USER")
    void apiSecurityErrorsAreReturnedAsJson() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number": "401",
                                  "name": "Seminar room",
                                  "capacity": 20,
                                  "floor": 4
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Доступ запрещен"));
    }

    @Test
    void apiErrorsAreReturnedAsJson() throws Exception {
        mockMvc.perform(get("/api/rooms/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void htmlErrorsUseErrorTemplate() throws Exception {
        mockMvc.perform(get("/admin/rooms/999/edit")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }
}
