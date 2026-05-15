package com.booking.javaproject.room.dto;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.room.model.Room;

import java.util.Comparator;
import java.util.List;

public class RoomResponse {

    private final Long id;
    private final String number;
    private final String name;
    private final Integer capacity;
    private final Integer floor;
    private final String description;
    private final boolean active;
    private final List<EquipmentSummaryResponse> equipment;

    public RoomResponse(
            Long id,
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            boolean active,
            List<EquipmentSummaryResponse> equipment
    ) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.capacity = capacity;
        this.floor = floor;
        this.description = description;
        this.active = active;
        this.equipment = equipment;
    }

    public static RoomResponse from(Room room) {
        List<EquipmentSummaryResponse> equipment = room.getEquipment().stream()
                .sorted(Comparator.comparing(Equipment::getName))
                .map(EquipmentSummaryResponse::from)
                .toList();

        return new RoomResponse(
                room.getId(),
                room.getNumber(),
                room.getName(),
                room.getCapacity(),
                room.getFloor(),
                room.getDescription(),
                room.isActive(),
                equipment
        );
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getFloor() {
        return floor;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public List<EquipmentSummaryResponse> getEquipment() {
        return equipment;
    }
}
