package com.booking.javaproject.room.controller;

import com.booking.javaproject.room.dto.RoomCreateRequest;
import com.booking.javaproject.room.dto.RoomResponse;
import com.booking.javaproject.room.model.Room;
import com.booking.javaproject.room.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomService roomService;

    public RoomApiController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> listRooms(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        List<Room> rooms = activeOnly ? roomService.findActive() : roomService.findAll();
        return rooms.stream()
                .map(RoomResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public RoomResponse getRoom(@PathVariable Long id) {
        return RoomResponse.from(roomService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@RequestBody RoomCreateRequest request) {
        validateCreateRequest(request);
        return RoomResponse.from(roomService.create(
                request.getNumber(),
                request.getName(),
                request.getCapacity(),
                request.getFloor(),
                request.getDescription(),
                request.getEquipmentIds()
        ));
    }

    private void validateCreateRequest(RoomCreateRequest request) {
        if (request == null
                || request.getNumber() == null
                || request.getNumber().isBlank()
                || request.getName() == null
                || request.getName().isBlank()
                || request.getCapacity() == null
                || request.getCapacity() <= 0
                || request.getFloor() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Заполните номер, название, вместимость и этаж"
            );
        }
    }
}
