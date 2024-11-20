package com.acmelabinc.roombook.services;

import com.acmelabinc.roombook.converters.BookingConverter;
import com.acmelabinc.roombook.dtos.BookingRequestDto;
import com.acmelabinc.roombook.dtos.BookingResponseDto;
import com.acmelabinc.roombook.entities.Booking;
import com.acmelabinc.roombook.entities.Employee;
import com.acmelabinc.roombook.entities.Room;
import com.acmelabinc.roombook.exceptions.AlreadyExistsException;
import com.acmelabinc.roombook.exceptions.BadRequestException;
import com.acmelabinc.roombook.exceptions.NotFoundException;
import com.acmelabinc.roombook.repositories.BookingRepository;
import com.acmelabinc.roombook.repositories.EmployeeRepository;
import com.acmelabinc.roombook.repositories.RoomRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int HOUR_MINUTES = 60;
    private static final String BOOKING_CANCELLATION_MSG = "Booking was cancelled successfully.";
    private static final String ROOM_NOT_FOUND = "Room not found: ";
    private static final String EMPLOYEE_NOT_FOUND = "Employee not found: ";
    private static final String BOOKING_NOT_FOUND = "Booking was not found.";
    private static final String BOOKING_OVERLAP = "This room is already booked for the selected hours or overlaps another booking.";
    private static final String BOOKING_CANNOT_BE_CANCELED = "This is not a future booking so it cannot be canceled.";
    private static final String END_BEFORE_START_WARNING = "This booking can only take place in a time machine!";
    private static final String BOOKING_VALID_DURATION = "Bookings should last at least 1 hour or consecutive multiples of 1 hour (2, 3, 4, ...).";
    private static final String PAST_DAY_WARNING = "This day is gone forever.";

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository,
                              EmployeeRepository employeeRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Page<BookingResponseDto> getAll(Pageable pageable) {

        logger.info("Finding all bookings");

        Page<Booking> bookingsFromDb = bookingRepository.findAll(pageable);

        if (bookingsFromDb.isEmpty()) {
            logger.info("There are no bookings.");
            return new PageImpl<>(Collections.emptyList());
        }

        return buildResponseListPaged(bookingsFromDb, pageable);
    }

    @Override
    public Page<BookingResponseDto> getByRoomAndDate(String roomName, LocalDate date, Pageable pageable) {

        logger.info("Searching for bookings in room {} on date {}", roomName, date);

        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND + roomName));

        Page<Booking> bookingsFromDb = bookingRepository.findByRoomAndBookingDate(room, date, pageable);

        if (bookingsFromDb.isEmpty()) {
            logger.info("There are no bookings for room {} on date {}", roomName, date);
            return new PageImpl<>(Collections.emptyList());
        }

        logger.info("Successfully found bookings in room {} on date {}", roomName, date);
        return buildResponseListPaged(bookingsFromDb, pageable);
    }

    @Transactional
    @Override
    public BookingResponseDto save(BookingRequestDto bookingRequestDto) {

        logger.info("Saving a new booking in room {} on date {}",
                bookingRequestDto.getRoomName(), bookingRequestDto.getBookingDate());

        validateDuration(bookingRequestDto.getBookingDate(), bookingRequestDto.getStartTime(),
                bookingRequestDto.getEndTime());

        Room room = roomRepository.findByName(bookingRequestDto.getRoomName())
                .orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND + bookingRequestDto.getRoomName()));

        Employee employee = employeeRepository.findByEmail(bookingRequestDto.getEmployeeEmail())
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + bookingRequestDto.getEmployeeEmail()));

        validateNoOverlap(bookingRequestDto, room);

        Booking bookingToBeSaved = BookingConverter.convert(bookingRequestDto, room, employee);
        Booking booking = bookingRepository.save(bookingToBeSaved);

        logger.info("Booking saved successfully with ID {}", booking.getId());
        return BookingConverter.convert(booking);
    }

    @Override
    public String cancel(Long id) {

        logger.info("Cancelling booking with ID {}", id);

        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND));

        if (isPastBooking(booking.getBookingDate(), booking.getStartTime())) {
            throw new BadRequestException(BOOKING_CANNOT_BE_CANCELED);
        }

        logger.info("Booking with ID {} was cancelled successfully.", id);
        return BOOKING_CANCELLATION_MSG;
    }

    private void validateDuration(LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new BadRequestException(PAST_DAY_WARNING);
        }

        if (endTime.isBefore(startTime)) {
            throw new BadRequestException(END_BEFORE_START_WARNING);
        }

        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes < HOUR_MINUTES || minutes % HOUR_MINUTES != 0) {
            throw new BadRequestException(BOOKING_VALID_DURATION);
        }
    }

    private void validateNoOverlap(BookingRequestDto bookingRequestDto, Room room) {

        if (bookingRepository.existsByRoomAndBookingDateAndStartTimeLessThanAndEndTimeGreaterThan(room,
                bookingRequestDto.getBookingDate(), bookingRequestDto.getEndTime(), bookingRequestDto.getStartTime())) {
            throw new AlreadyExistsException(BOOKING_OVERLAP);
        }
    }

    private boolean isPastBooking(LocalDate bookingDate, LocalTime bookingStartTime) {
        return bookingDate.isBefore(LocalDate.now()) || (bookingDate.isEqual(LocalDate.now()) &&
                bookingStartTime.isBefore(LocalTime.now()));
    }

    private Page<BookingResponseDto> buildResponseListPaged(Page<Booking> bookingsFromDb, Pageable pageable) {

        long total = bookingsFromDb.getTotalElements();

        List<BookingResponseDto> bookings = bookingsFromDb.stream()
                .map(BookingConverter::convert)
                .toList();

        return new PageImpl<>(bookings, pageable, total);
    }
}
