package com.booking.javaproject.booking.controller;

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

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;

    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping("/my")
    public String myBookings(
            @PageableDefault(size = 10) Pageable pageable,
            Principal principal,
            Model model
    ) {
        model.addAttribute("bookingsPage", bookingService.findCurrentUserBookings(principal, pageable));
        return "bookings/my";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.cancelCurrentUserBooking(principal, id);
            redirectAttributes.addFlashAttribute("success", "Бронь отменена");
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getReason());
        }

        return "redirect:/bookings/my";
    }

    @GetMapping("/new")
    public String newBookingForm(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime,
            Model model
    ) {
        fillFormModel(model, roomId, startTime, endTime, null);
        model.addAttribute("success", Boolean.TRUE.equals(success));
        return "bookings/new";
    }

    @PostMapping
    public String createBooking(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime,
            @RequestParam(required = false) String comment,
            Principal principal,
            Model model
    ) {
        try {
            bookingService.createBooking(principal, roomId, startTime, endTime, comment);
        } catch (ResponseStatusException exception) {
            fillFormModel(model, roomId, startTime, endTime, comment);
            model.addAttribute("error", exception.getReason());
            return "bookings/new";
        }

        return "redirect:/bookings/new?roomId=" + roomId + "&success=true";
    }

    private void fillFormModel(
            Model model,
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String comment
    ) {
        model.addAttribute("rooms", roomService.findActive());
        model.addAttribute("roomId", roomId);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        model.addAttribute("comment", comment);
    }
}
