package com.booking.javaproject.equipment.repository;

import com.booking.javaproject.equipment.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findAllByOrderByNameAsc();

    List<Equipment> findByActiveTrueOrderByNameAsc();

    Optional<Equipment> findByName(String name);
}
