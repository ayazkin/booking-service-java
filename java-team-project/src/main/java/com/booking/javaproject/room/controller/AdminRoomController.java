package com.booking.javaproject.room.controller;

import com.booking.javaproject.equipment.service.EquipmentService;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;
    private final EquipmentService equipmentService;

    public AdminRoomController(RoomService roomService, EquipmentService equipmentService) {
        this.roomService = roomService;
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/rooms/list";
    }

    @GetMapping("/new")
    public String newRoomForm(Model model) {
        model.addAttribute("equipmentOptions", equipmentService.findAll());
        return "admin/rooms/new";
    }

    @PostMapping
    public String createRoom(
            @RequestParam String number,
            @RequestParam String name,
            @RequestParam Integer capacity,
            @RequestParam Integer floor,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Collection<Long> equipmentIds,
            Model model
    ) {
        if (hasInvalidRequiredFields(number, name, capacity, floor)) {
            fillRoomFormModel(model, number, name, capacity, floor, description, true, equipmentIds);
            model.addAttribute("error", "Заполните номер, название, вместимость и этаж");
            return "admin/rooms/new";
        }

        try {
            roomService.create(number, name, capacity, floor, description, equipmentIds);
        } catch (ResponseStatusException exception) {
            fillRoomFormModel(model, number, name, capacity, floor, description, true, equipmentIds);
            model.addAttribute("error", exception.getReason());
            return "admin/rooms/new";
        }

        return "redirect:/admin/rooms";
    }

    @GetMapping("/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.findById(id);
        model.addAttribute("room", room);
        model.addAttribute("equipmentOptions", equipmentService.findAll());
        model.addAttribute("selectedEquipmentIds", selectedEquipmentIds(room));
        return "admin/rooms/edit";
    }

    @PostMapping("/{id}")
    public String updateRoom(
            @PathVariable Long id,
            @RequestParam String number,
            @RequestParam String name,
            @RequestParam Integer capacity,
            @RequestParam Integer floor,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean active,
            @RequestParam(required = false) Collection<Long> equipmentIds,
            Model model
    ) {
        if (hasInvalidRequiredFields(number, name, capacity, floor)) {
            fillRoomFormModel(model, id, number, name, capacity, floor, description, active, equipmentIds);
            model.addAttribute("error", "Заполните номер, название, вместимость и этаж");
            return "admin/rooms/edit";
        }

        try {
            roomService.update(id, number, name, capacity, floor, description, active, equipmentIds);
        } catch (ResponseStatusException exception) {
            fillRoomFormModel(model, id, number, name, capacity, floor, description, active, equipmentIds);
            model.addAttribute("error", exception.getReason());
            return "admin/rooms/edit";
        }

        return "redirect:/admin/rooms";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateRoom(@PathVariable Long id) {
        roomService.deactivate(id);
        return "redirect:/admin/rooms";
    }

    private boolean hasInvalidRequiredFields(String number, String name, Integer capacity, Integer floor) {
        return number == null
                || number.isBlank()
                || name == null
                || name.isBlank()
                || capacity == null
                || capacity <= 0
                || floor == null;
    }

    private void fillRoomFormModel(
            Model model,
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            boolean active,
            Collection<Long> equipmentIds
    ) {
        Room room = new Room(number, name, capacity, floor);
        room.setDescription(description);
        room.setActive(active);
        model.addAttribute("room", room);
        model.addAttribute("equipmentOptions", equipmentService.findAll());
        model.addAttribute("selectedEquipmentIds", equipmentIds == null ? Set.of() : Set.copyOf(equipmentIds));
    }

    private void fillRoomFormModel(
            Model model,
            Long id,
            String number,
            String name,
            Integer capacity,
            Integer floor,
            String description,
            boolean active,
            Collection<Long> equipmentIds
    ) {
        fillRoomFormModel(model, number, name, capacity, floor, description, active, equipmentIds);
        model.addAttribute("roomId", id);
    }

    private Set<Long> selectedEquipmentIds(Room room) {
        return room.getEquipment().stream()
                .map(equipment -> equipment.getId())
                .collect(Collectors.toSet());
    }
}
