package com.booking.javaproject.common.config;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final boolean seedEnabled;
    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;

    public DemoDataInitializer(
            @Value("${app.seed.enabled:true}") boolean seedEnabled,
            EquipmentRepository equipmentRepository,
            RoomRepository roomRepository
    ) {
        this.seedEnabled = seedEnabled;
        this.equipmentRepository = equipmentRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            return;
        }

        Equipment projector = findOrCreateEquipment("Projector", "Ceiling or portable projector");
        Equipment whiteboard = findOrCreateEquipment("Whiteboard", "Marker board");
        Equipment videoConference = findOrCreateEquipment("Video conference", "Camera, microphone and display");
        Equipment computers = findOrCreateEquipment("Computers", "Student workstations");

        findOrCreateRoom("101", "Lecture hall", 80, 1, "Large lecture room", projector, whiteboard);
        findOrCreateRoom("203", "Seminar room", 24, 2, "Room for group classes", projector, whiteboard);
        findOrCreateRoom("305", "Computer lab", 30, 3, "Lab with workstations", computers, projector);
        findOrCreateRoom("410", "Meeting room", 12, 4, "Small room for meetings", videoConference, whiteboard);
    }

    private Equipment findOrCreateEquipment(String name, String description) {
        return equipmentRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Equipment equipment = new Equipment(name);
                    equipment.setDescription(description);
                    return equipmentRepository.save(equipment);
                });
    }

    private void findOrCreateRoom(
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            Equipment... equipment
    ) {
        if (roomRepository.existsByNumber(number)) {
            return;
        }

        Room room = new Room(number, name, capacity, floor);
        room.setDescription(description);
        addEquipment(room, equipment);
        roomRepository.save(room);
    }

    private void addEquipment(Room room, Equipment... equipment) {
        for (Equipment item : equipment) {
            room.addEquipment(item);
        }
    }
}
