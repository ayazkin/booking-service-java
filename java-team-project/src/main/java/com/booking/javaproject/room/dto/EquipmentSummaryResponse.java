package com.booking.javaproject.room.dto;

import com.booking.javaproject.equipment.model.Equipment;

public class EquipmentSummaryResponse {

    private final Long id;
    private final String name;

    public EquipmentSummaryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static EquipmentSummaryResponse from(Equipment equipment) {
        return new EquipmentSummaryResponse(equipment.getId(), equipment.getName());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
