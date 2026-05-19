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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
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
        String normalizedNumber = normalizeNumber(number);
        String normalizedName = normalizeName(name);
        validateCapacity(capacity);
        validateFloor(floor);

        if (roomRepository.existsByNumber(normalizedNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room with this number already exists");
        }

        Room room = new Room(normalizedNumber, normalizedName, capacity, floor);
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
        String normalizedNumber = normalizeNumber(number);
        String normalizedName = normalizeName(name);
        validateCapacity(capacity);
        validateFloor(floor);

        if (roomRepository.existsByNumberAndIdNot(normalizedNumber, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room with this number already exists");
        }

        room.setNumber(normalizedNumber);
        room.setName(normalizedName);
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

    private String normalizeNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room number is required");
        }
        String normalizedNumber = number.trim();
        if (normalizedNumber.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room number must be 30 characters or less");
        }
        return normalizedNumber;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name is required");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name must be 120 characters or less");
        }
        return normalizedName;
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room capacity must be greater than zero");
        }
    }

    private void validateFloor(Integer floor) {
        if (floor == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room floor is required");
        }
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalizedValue = value.trim();
        if (normalizedValue.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room description must be 1000 characters or less");
        }
        return normalizedValue;
    }

    private Set<Equipment> findEquipment(Collection<Long> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return Set.of();
        }

        Set<Long> uniqueIds = equipmentIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.isEmpty()) {
            return Set.of();
        }

        List<Equipment> equipment = equipmentRepository.findAllById(uniqueIds);
        Set<Long> foundIds = equipment.stream()
                .map(Equipment::getId)
                .collect(Collectors.toSet());
        Set<Long> missingIds = new LinkedHashSet<>(uniqueIds);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown equipment ids: " + missingIds);
        }

        return new HashSet<>(equipment);
    }

    private void initializeEquipment(Collection<Room> rooms) {
        rooms.forEach(room -> room.getEquipment().size());
    }
}
