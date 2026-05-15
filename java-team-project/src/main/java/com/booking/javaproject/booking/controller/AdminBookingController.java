package com.booking.javaproject.booking.controller;

import com.booking.javaproject.booking.model.BookingStatus;
import com.booking.javaproject.booking.service.BookingService;
import com.booking.javaproject.room.service.RoomService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;
    private final RoomService roomService;

    public AdminBookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping
    public String listBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String userQuery,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTo,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        model.addAttribute("bookingsPage", bookingService.searchBookings(
                status,
                roomId,
                userQuery,
                startFrom,
                startTo,
                pageable
        ));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("status", status);
        model.addAttribute("roomId", roomId);
        model.addAttribute("userQuery", userQuery);
        model.addAttribute("startFrom", startFrom);
        model.addAttribute("startTo", startTo);

        return "admin/bookings/list";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String adminComment,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.cancelBookingByAdmin(id, adminComment);
            redirectAttributes.addFlashAttribute("success", "Бронь отменена");
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getReason());
        }

        return "redirect:/admin/bookings";
    }
}
