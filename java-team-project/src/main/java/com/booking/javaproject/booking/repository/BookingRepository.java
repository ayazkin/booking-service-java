package com.booking.javaproject.booking.repository;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    Page<Booking> findByRoomIdOrderByStartTimeDesc(Long roomId, Pageable pageable);

    Page<Booking> findByStatusOrderByStartTimeDesc(BookingStatus status, Pageable pageable);

    @Query("""
            select count(b) > 0
            from Booking b
            where b.room.id = :roomId
              and b.status in :activeStatuses
              and b.startTime < :endTime
              and b.endTime > :startTime
            """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("activeStatuses") Collection<BookingStatus> activeStatuses
    );
}
