package com.acmelabinc.roombook.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingResponseDto {

    private Long id;
    private String bookedBy;
    private LocalDate bookingDate;
    private LocalTime timeFrom;
    private LocalTime timeTo;

    public BookingResponseDto() {
    }

    public BookingResponseDto(Long id, String bookedBy, LocalDate bookingDate, LocalTime timeFrom, LocalTime timeTo) {
        this.id = id;
        this.bookedBy = bookedBy;
        this.bookingDate = bookingDate;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(LocalTime timeFrom) {
        this.timeFrom = timeFrom;
    }

    public LocalTime getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(LocalTime timeTo) {
        this.timeTo = timeTo;
    }
}
