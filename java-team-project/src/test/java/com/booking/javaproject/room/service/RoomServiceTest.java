package com.booking.javaproject.room.service;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import com.booking.javaproject.booking.repository.BookingRepository;
import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.model.UserProfile;
import com.booking.javaproject.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:room-service;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.enabled=false",
        "app.seed.enabled=false"
})
@Transactional
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createsRoomWithValidatedEquipmentAndDeduplicatedIds() {
        Equipment projector = saveEquipment("Projector");
        Equipment whiteboard = saveEquipment("Whiteboard");

        Room room = roomService.create(
                " 101 ",
                " Lecture hall ",
                80,
                1,
                "  Large room  ",
                List.of(projector.getId(), projector.getId(), whiteboard.getId())
        );

        Room savedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertThat(savedRoom.getNumber()).isEqualTo("101");
        assertThat(savedRoom.getName()).isEqualTo("Lecture hall");
        assertThat(savedRoom.getDescription()).isEqualTo("Large room");
        assertThat(savedRoom.getEquipment())
                .extracting(Equipment::getName)
                .containsExactlyInAnyOrder("Projector", "Whiteboard");
    }

    @Test
    void rejectsInvalidRoomFieldsDuplicateNumberAndUnknownEquipment() {
        Equipment projector = saveEquipment("Projector");
        roomService.create("101", "Lecture hall", 80, 1, null, List.of(projector.getId()));

        assertBadRequest(() -> roomService.create(null, "Room", 10, 1, null, null));
        assertBadRequest(() -> roomService.create("102", "Room", 0, 1, null, null));
        assertBadRequest(() -> roomService.create("101", "Another room", 10, 1, null, null));
        assertBadRequest(() -> roomService.create("103", "Room", 10, 1, null, List.of(9999L)));
    }

    @Test
    void updatesAndDeactivatesRoom() {
        Equipment projector = saveEquipment("Projector");
        Equipment videoConference = saveEquipment("Video conference");
        Room room = roomService.create("201", "Old room", 12, 2, null, List.of(projector.getId()));

        Room updated = roomService.update(
                room.getId(),
                " 202 ",
                " Meeting room ",
                16,
                3,
                "  Updated description  ",
                true,
                List.of(videoConference.getId())
        );
        roomService.deactivate(updated.getId());

        Room savedRoom = roomRepository.findById(updated.getId()).orElseThrow();
        assertThat(savedRoom.getNumber()).isEqualTo("202");
        assertThat(savedRoom.getName()).isEqualTo("Meeting room");
        assertThat(savedRoom.getCapacity()).isEqualTo(16);
        assertThat(savedRoom.getFloor()).isEqualTo(3);
        assertThat(savedRoom.getDescription()).isEqualTo("Updated description");
        assertThat(savedRoom.isActive()).isFalse();
        assertThat(savedRoom.getEquipment())
                .extracting(Equipment::getName)
                .containsExactly("Video conference");
    }

    @Test
    void searchesRoomsByQueryCapacityFloorEquipmentAndActiveFlag() {
        Equipment projector = saveEquipment("Projector");
        Equipment computers = saveEquipment("Computers");
        Room lectureRoom = roomService.create("101", "Lecture hall", 80, 1, null, List.of(projector.getId()));
        Room computerLab = roomService.create("305", "Computer lab", 30, 3, null, List.of(computers.getId()));
        Room inactiveRoom = roomService.create("410", "Meeting room", 12, 4, null, List.of(projector.getId()));
        roomService.deactivate(inactiveRoom.getId());

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("number"));

        assertThat(roomService.searchRooms("lab", null, null, null, null, null, true, pageable).getContent())
                .extracting(Room::getId)
                .containsExactly(computerLab.getId());
        assertThat(roomService.searchRooms(null, 50, null, null, null, null, true, pageable).getContent())
                .extracting(Room::getId)
                .containsExactly(lectureRoom.getId());
        assertThat(roomService.searchRooms(null, null, 3, null, null, null, true, pageable).getContent())
                .extracting(Room::getId)
                .containsExactly(computerLab.getId());
        assertThat(roomService.searchRooms(null, null, null, projector.getId(), null, null, true, pageable).getContent())
                .extracting(Room::getId)
                .containsExactly(lectureRoom.getId());
        assertThat(roomService.searchRooms(null, null, null, projector.getId(), null, null, false, pageable).getContent())
                .extracting(Room::getId)
                .containsExactly(lectureRoom.getId(), inactiveRoom.getId());
    }

    @Test
    void searchesAvailableRoomsByIntervalAndIgnoresCanceledBookings() {
        Equipment projector = saveEquipment("Projector");
        Room busyRoom = roomService.create("501", "Busy room", 40, 5, null, List.of(projector.getId()));
        Room availableRoom = roomService.create("502", "Available room", 40, 5, null, List.of(projector.getId()));
        Room canceledRoom = roomService.create("503", "Canceled booking room", 40, 5, null, List.of(projector.getId()));
        User user = saveUser("availability@example.com");
        LocalDateTime start = LocalDateTime.of(2026, 5, 22, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 22, 11, 0);

        saveBooking(user, busyRoom, start, end, BookingStatus.APPROVED);
        saveBooking(user, canceledRoom, start, end, BookingStatus.CANCELED);

        assertThat(roomService.searchRooms(
                null,
                30,
                5,
                projector.getId(),
                start.plusMinutes(15),
                end.minusMinutes(15),
                true,
                PageRequest.of(0, 10, Sort.by("number"))
        ).getContent())
                .extracting(Room::getId)
                .containsExactly(availableRoom.getId(), canceledRoom.getId());
    }

    private Equipment saveEquipment(String name) {
        return equipmentRepository.save(new Equipment(name));
    }

    private User saveUser(String email) {
        User user = new User(email, email, "{noop}password");
        user.setProfile(new UserProfile("Test", "User", null));
        return userRepository.save(user);
    }

    private void saveBooking(User user, Room room, LocalDateTime startTime, LocalDateTime endTime, BookingStatus status) {
        Booking booking = new Booking(user, room, startTime, endTime);
        booking.setStatus(status);
        bookingRepository.save(booking);
    }

    private void assertBadRequest(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
