package com.booking.javaproject.common.config;

import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.auth.repository.RoleRepository;
import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer {

    @Bean
    @Transactional
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            EquipmentRepository equipmentRepository,
            RoomRepository roomRepository
    ) {
        return args -> {
            createRoleIfMissing(roleRepository, "ROLE_USER");
            createRoleIfMissing(roleRepository, "ROLE_ADMIN");

            Equipment projector = createEquipmentIfMissing(
                    equipmentRepository,
                    "Projector",
                    "Multimedia projector for presentations"
            );
            Equipment whiteboard = createEquipmentIfMissing(
                    equipmentRepository,
                    "Whiteboard",
                    "Marker board for lectures and seminars"
            );
            Equipment computers = createEquipmentIfMissing(
                    equipmentRepository,
                    "Computer class",
                    "Workstations for practical lessons"
            );

            createRoomIfMissing(
                    roomRepository,
                    "101",
                    "Lecture hall",
                    60,
                    1,
                    "Large auditorium for lectures and public events",
                    projector,
                    whiteboard
            );
            createRoomIfMissing(
                    roomRepository,
                    "205",
                    "Seminar room",
                    24,
                    2,
                    "Room for seminars and team work",
                    whiteboard
            );
            createRoomIfMissing(
                    roomRepository,
                    "310",
                    "Computer lab",
                    32,
                    3,
                    "Computer classroom for practical Java lessons",
                    projector,
                    computers
            );
        };
    }

    private void createRoleIfMissing(RoleRepository roleRepository, String name) {
        roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private Equipment createEquipmentIfMissing(
            EquipmentRepository equipmentRepository,
            String name,
            String description
    ) {
        return equipmentRepository.findByName(name)
                .orElseGet(() -> {
                    Equipment equipment = new Equipment(name);
                    equipment.setDescription(description);
                    return equipmentRepository.save(equipment);
                });
    }

    private void createRoomIfMissing(
            RoomRepository roomRepository,
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            Equipment... equipmentItems
    ) {
        if (roomRepository.existsByNumber(number)) {
            return;
        }

        Room room = new Room(number, name, capacity, floor);
        room.setDescription(description);
        for (Equipment equipment : equipmentItems) {
            room.addEquipment(equipment);
        }
        roomRepository.save(room);
    }
}
