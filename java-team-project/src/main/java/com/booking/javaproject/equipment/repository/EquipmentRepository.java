package com.booking.javaproject.equipment.repository;

import com.booking.javaproject.equipment.model.Equipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByActiveTrueOrderByNameAsc();

    Optional<Equipment> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
