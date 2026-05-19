package com.booking.javaproject.booking.repository;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @EntityGraph(attributePaths = "room")
    Page<Booking> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"room", "user"})
    Page<Booking> findAll(Specification<Booking> specification, Pageable pageable);

    boolean existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Collection<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    @EntityGraph(attributePaths = "room")
    List<Booking> findByStatusInAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeAsc(
            Collection<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}
