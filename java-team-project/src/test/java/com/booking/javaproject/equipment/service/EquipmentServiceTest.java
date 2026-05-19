package com.booking.javaproject.equipment.service;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:equipment-service;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.enabled=false",
        "app.seed.enabled=false"
})
@Transactional
class EquipmentServiceTest {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void createsEquipmentWithNormalizedFields() {
        Equipment equipment = equipmentService.create(" Projector ", "  HDMI projector  ");

        Equipment savedEquipment = equipmentRepository.findById(equipment.getId()).orElseThrow();
        assertThat(savedEquipment.getName()).isEqualTo("Projector");
        assertThat(savedEquipment.getDescription()).isEqualTo("HDMI projector");
        assertThat(savedEquipment.isActive()).isTrue();
    }

    @Test
    void rejectsBlankAndDuplicateEquipmentName() {
        equipmentService.create("Projector", null);

        assertBadRequest(() -> equipmentService.create(" ", null));
        assertBadRequest(() -> equipmentService.create("projector", null));
    }

    @Test
    void updatesEquipmentAndRejectsDuplicateName() {
        Equipment projector = equipmentService.create("Projector", null);
        equipmentService.create("Whiteboard", null);

        Equipment updated = equipmentService.update(projector.getId(), " Display ", "  Wall display  ", false);

        assertThat(updated.getName()).isEqualTo("Display");
        assertThat(updated.getDescription()).isEqualTo("Wall display");
        assertThat(updated.isActive()).isFalse();
        assertBadRequest(() -> equipmentService.update(projector.getId(), "Whiteboard", null, true));
    }

    @Test
    void deactivatesEquipment() {
        Equipment equipment = equipmentService.create("Microphone", null);

        equipmentService.deactivate(equipment.getId());

        assertThat(equipmentRepository.findById(equipment.getId()).orElseThrow().isActive()).isFalse();
    }

    private void assertBadRequest(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
