package com.booking.javaproject.room.controller;

import com.booking.javaproject.room.service.RoomService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(@PageableDefault(size = 10, sort = "number") Pageable pageable, Model model) {
        model.addAttribute("roomsPage", roomService.findActiveRooms(pageable));
        return "rooms/list";
    }
}
