package com.booking.javaproject.Calendar;

import com.booking.javaproject.booking.model.Booking;
import com.booking.javaproject.booking.model.BookingStatus;

import java.util.Map;

public class CalendarEventResponse {

    private final Long id;
    private final String title;
    private final String start;
    private final String end;
    private final String color;
    private final String textColor;
    private final Map<String, String> extendedProps;

    public CalendarEventResponse(
            Long id,
            String title,
            String start,
            String end,
            String color,
            String textColor,
            Map<String, String> extendedProps
    ) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.color = color;
        this.textColor = textColor;
        this.extendedProps = extendedProps;
    }

    public static CalendarEventResponse from(Booking booking) {
        String roomTitle = booking.getRoom().getNumber() + " - " + booking.getRoom().getName();
        return new CalendarEventResponse(
                booking.getId(),
                roomTitle,
                booking.getStartTime().toString(),
                booking.getEndTime().toString(),
                colorForStatus(booking.getStatus()),
                "#ffffff",
                Map.of(
                        "status", booking.getStatus().name(),
                        "roomNumber", booking.getRoom().getNumber(),
                        "roomName", booking.getRoom().getName()
                )
        );
    }

    private static String colorForStatus(BookingStatus status) {
        if (status == BookingStatus.PENDING) {
            return "#d97706";
        }
        return "#2563eb";
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getColor() {
        return color;
    }

    public String getTextColor() {
        return textColor;
    }

    public Map<String, String> getExtendedProps() {
        return extendedProps;
    }
}
