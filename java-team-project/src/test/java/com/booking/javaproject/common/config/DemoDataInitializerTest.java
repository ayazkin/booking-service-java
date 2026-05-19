package com.booking.javaproject.common.config;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo-data;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.enabled=false",
        "app.seed.enabled=true"
})
@Transactional
class DemoDataInitializerTest {

    @Autowired
    private DemoDataInitializer demoDataInitializer;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void createsDemoEquipmentAndRoomsOnlyOnce() {
        assertThat(equipmentRepository.findAll())
                .extracting(Equipment::getName)
                .contains("Projector", "Whiteboard", "Video conference", "Computers");
        assertThat(roomRepository.findAll())
                .extracting(Room::getNumber)
                .contains("101", "203", "305", "410");

        long equipmentCount = equipmentRepository.count();
        long roomCount = roomRepository.count();

        demoDataInitializer.run(null);

        assertThat(equipmentRepository.count()).isEqualTo(equipmentCount);
        assertThat(roomRepository.count()).isEqualTo(roomCount);
    }
}
