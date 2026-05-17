package com.booking.javaproject.room.controller;

import com.booking.javaproject.room.dto.RoomResponse;
import com.booking.javaproject.room.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    private final RoomService roomService;

    public RoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Page<RoomResponse> listRooms(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 10, sort = "number") Pageable pageable
    ) {
        return roomService.searchRooms(query, minCapacity, floor, equipmentId, activeOnly, pageable)
                .map(RoomResponse::from);
    }

    @GetMapping("/{id}")
    public RoomResponse getRoom(@PathVariable Long id) {
        return RoomResponse.from(roomService.findById(id));
    }
}
