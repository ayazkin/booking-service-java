package com.booking.javaproject.booking.repository;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = "room")
    Page<Booking> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"room", "user"})
    @Query("""
            select b
            from Booking b
            where (:status is null or b.status = :status)
              and (:roomId is null or b.room.id = :roomId)
              and (:userQuery = ''
                or lower(b.user.username) like lower(concat('%', :userQuery, '%'))
                or lower(b.user.email) like lower(concat('%', :userQuery, '%')))
              and (:startFrom is null or b.startTime >= :startFrom)
              and (:startTo is null or b.startTime <= :startTo)
            order by b.startTime desc
            """)
    Page<Booking> search(
            @Param("status") BookingStatus status,
            @Param("roomId") Long roomId,
            @Param("userQuery") String userQuery,
            @Param("startFrom") LocalDateTime startFrom,
            @Param("startTo") LocalDateTime startTo,
            Pageable pageable
    );

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
