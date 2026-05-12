package com.booking.javaproject.room.repository;

import com.booking.javaproject.room.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByActiveTrue(Pageable pageable);
}
