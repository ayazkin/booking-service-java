package com.booking.javaproject.booking.service;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import com.booking.javaproject.booking.repository.BookingRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.model.UserProfile;
import com.booking.javaproject.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking-service;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.enabled=false"
})
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createsApprovedBookingForCurrentUser() {
        User user = saveUser("student@example.com");
        Room room = saveRoom("101", "Lecture room");
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 11, 0);

        Booking booking = bookingService.createBooking(
                principal(user.getEmail()),
                room.getId(),
                start,
                end,
                " Need projector "
        );

        Booking savedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(savedBooking.getComment()).isEqualTo("Need projector");
        assertThat(savedBooking.getStartTime()).isEqualTo(start);
        assertThat(savedBooking.getEndTime()).isEqualTo(end);
        assertThat(savedBooking.getRoom().getId()).isEqualTo(room.getId());
        assertThat(savedBooking.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void rejectsOverlappingBookingForSameRoom() {
        User firstUser = saveUser("first@example.com");
        User secondUser = saveUser("second@example.com");
        Room room = saveRoom("102", "Seminar room");
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 10, 0);

        bookingService.createBooking(
                principal(firstUser.getEmail()),
                room.getId(),
                start,
                start.plusHours(1),
                null
        );

        assertThatThrownBy(() -> bookingService.createBooking(
                principal(secondUser.getEmail()),
                room.getId(),
                start.plusMinutes(30),
                start.plusHours(2),
                null
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void cancelsCurrentUserBooking() {
        User user = saveUser("cancel@example.com");
        Room room = saveRoom("103", "Small room");
        Booking booking = bookingService.createBooking(
                principal(user.getEmail()),
                room.getId(),
                LocalDateTime.of(2026, 5, 21, 9, 0),
                LocalDateTime.of(2026, 5, 21, 10, 0),
                null
        );

        bookingService.cancelCurrentUserBooking(principal(user.getEmail()), booking.getId());

        Booking canceledBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(canceledBooking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void filtersBookingsByStatusRoomUserAndDates() {
        User alphaUser = saveUser("alpha@example.com");
        User betaUser = saveUser("beta@example.com");
        Room firstRoom = saveRoom("201", "First room");
        Room secondRoom = saveRoom("202", "Second room");

        Booking firstBooking = bookingService.createBooking(
                principal(alphaUser.getEmail()),
                firstRoom.getId(),
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 11, 0),
                null
        );
        Booking secondBooking = bookingService.createBooking(
                principal(betaUser.getEmail()),
                secondRoom.getId(),
                LocalDateTime.of(2026, 5, 21, 12, 0),
                LocalDateTime.of(2026, 5, 21, 13, 0),
                null
        );
        Booking canceledBooking = bookingService.createBooking(
                principal(alphaUser.getEmail()),
                secondRoom.getId(),
                LocalDateTime.of(2026, 5, 22, 14, 0),
                LocalDateTime.of(2026, 5, 22, 15, 0),
                null
        );
        bookingService.cancelBookingByAdmin(canceledBooking.getId(), "Canceled by admin");

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Booking> allBookings = bookingService.searchBookings(null, null, null, null, null, pageable);
        assertThat(allBookings.getContent())
                .extracting(Booking::getId)
                .containsExactly(canceledBooking.getId(), secondBooking.getId(), firstBooking.getId());

        Page<Booking> approvedBookings = bookingService.searchBookings(
                BookingStatus.APPROVED,
                null,
                null,
                null,
                null,
                pageable
        );
        assertThat(approvedBookings.getContent())
                .extracting(Booking::getId)
                .containsExactly(secondBooking.getId(), firstBooking.getId());

        Page<Booking> roomBookings = bookingService.searchBookings(null, firstRoom.getId(), null, null, null, pageable);
        assertThat(roomBookings.getContent())
                .extracting(Booking::getId)
                .containsExactly(firstBooking.getId());

        Page<Booking> userBookings = bookingService.searchBookings(null, null, "ALPHA", null, null, pageable);
        assertThat(userBookings.getContent())
                .extracting(Booking::getId)
                .containsExactly(canceledBooking.getId(), firstBooking.getId());

        Page<Booking> dateBookings = bookingService.searchBookings(
                null,
                null,
                null,
                LocalDateTime.of(2026, 5, 21, 0, 0),
                LocalDateTime.of(2026, 5, 21, 23, 59),
                pageable
        );
        assertThat(dateBookings.getContent())
                .extracting(Booking::getId)
                .containsExactly(secondBooking.getId());
    }

    private User saveUser(String email) {
        User user = new User(email, email, "{noop}password");
        user.setProfile(new UserProfile("Test", "User", null));
        return userRepository.save(user);
    }

    private Room saveRoom(String number, String name) {
        return roomRepository.save(new Room(number, name, 20, 2));
    }

    private Principal principal(String name) {
        return () -> name;
    }
}
