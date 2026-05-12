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

    @Transactional(readOnly = true)
    public Page<Room> searchRooms(
            String query,
            Integer minCapacity,
            Integer floor,
            Long equipmentId,
            boolean activeOnly,
            Pageable pageable
    ) {
        return roomRepository.search(
                normalizeQuery(query),
                minCapacity,
                floor,
                equipmentId,
                activeOnly,
                pageable
        );
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }
}
