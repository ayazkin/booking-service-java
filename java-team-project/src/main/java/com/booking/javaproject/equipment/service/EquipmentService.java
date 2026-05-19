package com.booking.javaproject.equipment.service;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipment> findAll() {
        return equipmentRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Equipment> findActive() {
        return equipmentRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found"));
    }

    @Transactional
    public Equipment create(String name, String description) {
        String normalizedName = normalizeName(name);
        if (equipmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment with this name already exists");
        }

        Equipment equipment = new Equipment(normalizedName);
        equipment.setDescription(normalizeDescription(description));
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment update(Long id, String name, String description, boolean active) {
        Equipment equipment = findById(id);
        String normalizedName = normalizeName(name);
        if (equipmentRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment with this name already exists");
        }

        equipment.setName(normalizedName);
        equipment.setDescription(normalizeDescription(description));
        equipment.setActive(active);
        return equipment;
    }

    @Transactional
    public void deactivate(Long id) {
        Equipment equipment = findById(id);
        equipment.setActive(false);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment name is required");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment name must be 120 characters or less");
        }
        return normalizedName;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String normalizedDescription = description.trim();
        if (normalizedDescription.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment description must be 500 characters or less");
        }
        return normalizedDescription;
    }
}
