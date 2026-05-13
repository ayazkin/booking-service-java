package com.booking.javaproject.room.controller;

import com.booking.javaproject.equipment.service.EquipmentService;
import com.booking.javaproject.room.service.RoomService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final EquipmentService equipmentService;

    public RoomController(RoomService roomService, EquipmentService equipmentService) {
        this.roomService = roomService;
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public String listRooms(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 10, sort = "number") Pageable pageable,
            Model model
    ) {
        boolean onlyActive = filter == null || Boolean.TRUE.equals(activeOnly);

        model.addAttribute("roomsPage", roomService.searchRooms(
                query,
                minCapacity,
                floor,
                equipmentId,
                onlyActive,
                pageable
        ));
        model.addAttribute("equipmentOptions", equipmentService.findActive());
        model.addAttribute("query", query);
        model.addAttribute("minCapacity", minCapacity);
        model.addAttribute("floor", floor);
        model.addAttribute("equipmentId", equipmentId);
        model.addAttribute("activeOnly", onlyActive);

        return "rooms/list";
    }
}
