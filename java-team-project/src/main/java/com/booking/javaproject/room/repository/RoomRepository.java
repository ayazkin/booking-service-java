package com.booking.javaproject.room.repository;

import com.booking.javaproject.room.model.Room;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByNumber(String number);

    boolean existsByNumber(String number);

    Page<Room> findByActiveTrue(Pageable pageable);

    @Query("""
            select distinct r
            from Room r
            left join r.equipment e
            where (:query is null
                or lower(r.number) like lower(concat('%', :query, '%'))
                or lower(r.name) like lower(concat('%', :query, '%')))
              and (:minCapacity is null or r.capacity >= :minCapacity)
              and (:floor is null or r.floor = :floor)
              and (:equipmentId is null or e.id = :equipmentId)
              and (:activeOnly = false or r.active = true)
            """)
    Page<Room> search(
            @Param("query") String query,
            @Param("minCapacity") Integer minCapacity,
            @Param("floor") Integer floor,
            @Param("equipmentId") Long equipmentId,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );
}
