package com.booking.javaproject.Calendar;

import com.booking.javaproject.booking.service.BookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
public class CalendarApiController {

    private final BookingService bookingService;

    public CalendarApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/api/calendar/bookings")
    public List<CalendarEventResponse> bookings(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = parseDateTime(start, now.minusMonths(1));
        LocalDateTime endTime = parseDateTime(end, now.plusMonths(3));

        return bookingService.findCalendarBookings(startTime, endTime).stream()
                .map(CalendarEventResponse::from)
                .toList();
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException offsetException) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException localException) {
                return fallback;
            }
        }
    }
}
