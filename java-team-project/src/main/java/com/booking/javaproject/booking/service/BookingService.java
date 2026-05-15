package com.booking.javaproject.booking.service;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import com.booking.javaproject.booking.repository.BookingRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private static final List<BookingStatus> BUSY_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.APPROVED
    );

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(
            Principal principal,
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String comment
    ) {
        User user = findCurrentUser(principal);
        Room room = findActiveRoom(roomId);
        validateTime(startTime, endTime);
        validateRoomAvailability(room.getId(), startTime, endTime);

        Booking booking = new Booking(user, room, startTime, endTime);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setComment(normalizeComment(comment));
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Page<Booking> findCurrentUserBookings(Principal principal, Pageable pageable) {
        User user = findCurrentUser(principal);
        return bookingRepository.findByUserIdOrderByStartTimeDesc(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Booking> searchBookings(
            BookingStatus status,
            Long roomId,
            String userQuery,
            LocalDateTime startFrom,
            LocalDateTime startTo,
            Pageable pageable
    ) {
        return bookingRepository.search(
                status,
                roomId,
                normalizeQuery(userQuery),
                startFrom,
                startTo,
                pageable
        );
    }

    @Transactional
    public void cancelCurrentUserBooking(Principal principal, Long bookingId) {
        User user = findCurrentUser(principal);
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронь не найдена"));

        if (!isActiveBookingStatus(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отменить можно только активную бронь");
        }

        booking.setStatus(BookingStatus.CANCELED);
    }

    private User findCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация");
        }

        String login = principal.getName();
        return userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Пользователь не найден в базе"
                ));
    }

    private Room findActiveRoom(Long roomId) {
        if (roomId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Выберите аудиторию");
        }

        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аудитория не найдена"));
        if (!room.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аудитория отключена");
        }
        return room;
    }

    private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите время начала и окончания");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Время окончания должно быть позже начала");
        }
    }

    private void validateRoomAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean roomBusy = bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId,
                BUSY_STATUSES,
                endTime,
                startTime
        );
        if (roomBusy) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "На это время аудитория уже занята");
        }
    }

    private boolean isActiveBookingStatus(BookingStatus status) {
        return status == BookingStatus.PENDING || status == BookingStatus.APPROVED;
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }
}
