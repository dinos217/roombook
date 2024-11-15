package com.acmelabinc.roombook.converters;

import com.acmelabinc.roombook.dtos.BookingRequestDto;
import com.acmelabinc.roombook.dtos.BookingResponseDto;
import com.acmelabinc.roombook.entities.Booking;
import com.acmelabinc.roombook.entities.Employee;
import com.acmelabinc.roombook.entities.Room;

public class BookingConverter {

    private BookingConverter() {
    }

    public static BookingResponseDto convert(Booking booking) {
        return new BookingResponseDto(booking.getId(), booking.getEmployee().getEmail(), booking.getBookingDate(),
                booking.getStartTime(), booking.getEndTime());
    }

    public static Booking convert(BookingRequestDto bookingRequestDto, Room room, Employee employee) {
        return new Booking(room, employee, bookingRequestDto.getBookingDate(), bookingRequestDto.getStartTime(),
                bookingRequestDto.getEndTime());
    }
}
