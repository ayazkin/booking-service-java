package com.booking.javaproject.room.service;

import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public Page<Room> findActiveRooms(Pageable pageable) {
        return roomRepository.findByActiveTrue(pageable);
    }
}
