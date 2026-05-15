package com.booking.javaproject.booking.repository;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = "room")
    Page<Booking> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    boolean existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Collection<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}
