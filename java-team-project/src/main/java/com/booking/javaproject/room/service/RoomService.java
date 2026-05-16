package com.booking.javaproject.room.service;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.repository.EquipmentRepository;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;

    public RoomService(RoomRepository roomRepository, EquipmentRepository equipmentRepository) {
        this.roomRepository = roomRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> findAll() {
        List<Room> rooms = roomRepository.findAllByOrderByNumberAsc();
        initializeEquipment(rooms);
        return rooms;
    }

    @Transactional(readOnly = true)
    public List<Room> findActive() {
        List<Room> rooms = roomRepository.findAllByActiveTrueOrderByNumberAsc();
        initializeEquipment(rooms);
        return rooms;
    }

    @Transactional(readOnly = true)
    public Room findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аудитория не найдена"));
        room.getEquipment().size();
        return room;
    }

    @Transactional(readOnly = true)
    public Page<Room> findActiveRooms(Pageable pageable) {
        Page<Room> rooms = roomRepository.findByActiveTrue(pageable);
        initializeEquipment(rooms.getContent());
        return rooms;
    }

    @Transactional(readOnly = true)
    public Page<Room> searchRooms(
            String query,
            Integer minCapacity,
            Integer floor,
            Long equipmentId,
            boolean activeOnly,
            Pageable pageable
    ) {
        Page<Room> rooms = roomRepository.search(
                normalizeQuery(query),
                minCapacity,
                floor,
                equipmentId,
                activeOnly,
                pageable
        );
        initializeEquipment(rooms.getContent());
        return rooms;
    }

    @Transactional
    public Room create(
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            Collection<Long> equipmentIds
    ) {
        String normalizedNumber = number.trim();
        if (roomRepository.existsByNumber(normalizedNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аудитория с таким номером уже существует");
        }

        Room room = new Room(normalizedNumber, name.trim(), capacity, floor);
        room.setDescription(normalizeText(description));
        room.getEquipment().addAll(findEquipment(equipmentIds));
        return roomRepository.save(room);
    }

    @Transactional
    public Room update(
            Long id,
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            boolean active,
            Collection<Long> equipmentIds
    ) {
        Room room = findById(id);
        String normalizedNumber = number.trim();
        if (roomRepository.existsByNumberAndIdNot(normalizedNumber, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аудитория с таким номером уже существует");
        }

        room.setNumber(normalizedNumber);
        room.setName(name.trim());
        room.setCapacity(capacity);
        room.setFloor(floor);
        room.setDescription(normalizeText(description));
        room.setActive(active);
        room.getEquipment().clear();
        room.getEquipment().addAll(findEquipment(equipmentIds));
        return room;
    }

    @Transactional
    public void deactivate(Long id) {
        Room room = findById(id);
        room.setActive(false);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        return query.trim();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Set<Equipment> findEquipment(Collection<Long> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(equipmentRepository.findAllById(equipmentIds));
    }

    private void initializeEquipment(Collection<Room> rooms) {
        rooms.forEach(room -> room.getEquipment().size());
    }
}
