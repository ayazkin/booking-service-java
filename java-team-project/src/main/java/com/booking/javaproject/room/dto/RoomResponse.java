package com.booking.javaproject.room.dto;

import com.booking.javaproject.room.model.Room;

import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
        Long id,
        String number,
        String name,
        Integer capacity,
        Integer floor,
        String description,
        boolean active,
        List<String> equipment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getNumber(),
                room.getName(),
                room.getCapacity(),
                room.getFloor(),
                room.getDescription(),
                room.isActive(),
                room.getEquipment().stream()
                        .map(item -> item.getName())
                        .sorted()
                        .toList(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
