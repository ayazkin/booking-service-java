package com.booking.javaproject.booking.controller;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import com.booking.javaproject.booking.repository.BookingRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.model.UserProfile;
import com.booking.javaproject.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=${POSTGRES_TEST_URL}",
        "spring.datasource.username=${POSTGRES_TEST_USERNAME:postgres}",
        "spring.datasource.password=${POSTGRES_TEST_PASSWORD:postgres}",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.hibernate.ddl-auto=update",
        "app.admin.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "POSTGRES_TEST_URL", matches = ".+")
class AdminBookingPostgresSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminBookingsPageOpensWithEmptyAndDateFiltersOnPostgres() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        User user = saveUser("pg-smoke-" + suffix + "@example.com");
        Room room = saveRoom("PG-" + suffix.substring(suffix.length() - 8));
        saveBooking(user, room);

        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/bookings")
                        .param("userQuery", "PG-SMOKE")
                        .param("startFrom", "2026-05-21T00:00:00")
                        .param("startTo", "2026-05-21T23:59:00"))
                .andExpect(status().isOk());
    }

    private User saveUser(String email) {
        User user = new User(email, email, "{noop}password");
        user.setProfile(new UserProfile("Postgres", "Smoke", null));
        return userRepository.save(user);
    }

    private Room saveRoom(String number) {
        return roomRepository.save(new Room(number, "Postgres smoke room", 12, 1));
    }

    private void saveBooking(User user, Room room) {
        Booking booking = new Booking(
                user,
                room,
                LocalDateTime.of(2026, 5, 21, 10, 0),
                LocalDateTime.of(2026, 5, 21, 11, 0)
        );
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }
}
