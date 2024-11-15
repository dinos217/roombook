package com.acmelabinc.roombook.services;

import com.acmelabinc.roombook.converters.BookingConverter;
import com.acmelabinc.roombook.dtos.BookingRequestDto;
import com.acmelabinc.roombook.dtos.BookingResponseDto;
import com.acmelabinc.roombook.entities.Booking;
import com.acmelabinc.roombook.entities.Employee;
import com.acmelabinc.roombook.entities.Room;
import com.acmelabinc.roombook.exceptions.BadRequestException;
import com.acmelabinc.roombook.exceptions.NotFoundException;
import com.acmelabinc.roombook.repositories.BookingRepository;
import com.acmelabinc.roombook.repositories.EmployeeRepository;
import com.acmelabinc.roombook.repositories.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final String BOOKING_CANCELLATION_MSG = "Booking was cancelled successfully.";
    private static final String ROOM_NOT_FOUND = "Room not found: ";
    private static final String EMPLOYEE_NOT_FOUND = "Employee not found: ";
    private static final String BOOKING_NOT_FOUND = "Booking was not found.";
    private static final String BOOKING_OVERLAP = "The booking overlaps with an existing booking for the same room.";
    private static final String BOOKING_CANNOT_BE_CANCELED = "This booking is not a future booking and it cannot be canceled.";
    private static final String END_BEFORE_START_WARNING = "This booking can only take place in a time machine!";

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, EmployeeRepository employeeRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Page<BookingResponseDto> getByRoomAndDate(String roomName, LocalDate date, Pageable pageable) {

        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND + roomName));

        Page<Booking> bookingsFromDb = bookingRepository.findByRoomAndBookingDate(room, date, pageable);

        if (bookingsFromDb.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        return buildResponseListPaged(bookingsFromDb, pageable);
    }

    @Transactional
    @Override
    public BookingResponseDto save(BookingRequestDto bookingRequestDto) {

        Room room = roomRepository.findByName(bookingRequestDto.getRoomName())
                .orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND + bookingRequestDto.getRoomName()));

        Employee employee = employeeRepository.findByEmail(bookingRequestDto.getEmployeeEmail())
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + bookingRequestDto.getEmployeeEmail()));

        if (bookingRequestDto.getEndTime().isBefore(bookingRequestDto.getStartTime())) {
            throw new BadRequestException(END_BEFORE_START_WARNING);
        }

        //todo: add validation for hourly meetings

        if (bookingRepository.existsByRoomAndBookingDateAndStartTime(room, bookingRequestDto.getBookingDate(),
                bookingRequestDto.getStartTime(), bookingRequestDto.getEndTime())) {
            throw new BadRequestException(BOOKING_OVERLAP);
        }

        Booking bookingToBeSaved = BookingConverter.convert(bookingRequestDto, room, employee);
        Booking booking = bookingRepository.save(bookingToBeSaved);

        return BookingConverter.convert(booking);
    }

    @Override
    public String cancel(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND));

        if (booking.getBookingDate().isBefore(LocalDate.now()) && booking.getStartTime().isBefore(LocalTime.now())) {
            throw new BadRequestException(BOOKING_CANNOT_BE_CANCELED);
        }

        return BOOKING_CANCELLATION_MSG;
    }

    private Page<BookingResponseDto> buildResponseListPaged(Page<Booking> bookingsFromDb, Pageable pageable) {

        long total = bookingsFromDb.getTotalElements();

        List<BookingResponseDto> bookings = bookingsFromDb.stream()
                .map(BookingConverter::convert)
                .toList();

        return new PageImpl<>(bookings, pageable, total);
    }
}
