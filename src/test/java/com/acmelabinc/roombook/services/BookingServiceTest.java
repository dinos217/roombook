package com.acmelabinc.roombook.services;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetByRoomAndDate_ValidRoomAndDate() {

        String roomName = "room1";
        LocalDate date = LocalDate.of(2024, 11, 18);
        Pageable pageable = PageRequest.of(0, 10);

        Room room = buildRoom();
        Booking booking = buildBooking(room, buildEmployee());
        Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

        when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoomAndBookingDate(room, date, pageable)).thenReturn(bookingPage);

        Page<BookingResponseDto> result = bookingService.getByRoomAndDate(roomName, date, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(roomName, result.getContent().getFirst().getRoom());
    }

    @Test
    void testGetByRoomAndDate_RoomNotFound() {

        String roomName = "Milky Way";
        LocalDate date = LocalDate.of(2024, 11, 17);
        Pageable pageable = PageRequest.of(0, 10);

        when(roomRepository.findByName(roomName)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getByRoomAndDate(roomName, date, pageable));

        assertEquals("Room not found: Milky Way", exception.getMessage());
    }

    @Test
    void testSave_Successful() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        Room room = buildRoom();
        Employee employee = buildEmployee();
        Booking booking = buildBooking(room, employee);

        when(roomRepository.findByName("room1")).thenReturn(Optional.of(room));
        when(employeeRepository.findByEmail("dinos@acme.com")).thenReturn(Optional.of(employee));
        when(bookingRepository.existsByRoomAndBookingDateAndStartTimeLessThanAndEndTimeGreaterThan(room, requestDto.getBookingDate(),
                requestDto.getStartTime(), requestDto.getEndTime())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.save(requestDto);

        assertEquals(result.getBookedBy(), (requestDto.getEmployeeEmail()));
        assertEquals(result.getBookingDate(), (LocalDate.of(2024, 11, 17)));
        assertEquals(result.getRoom(), (requestDto.getRoomName()));
    }

    @Test
    void testSave_PastDay() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setBookingDate(LocalDate.now().minusDays(1));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.save(requestDto));

        assertEquals("This day is gone forever.", exception.getMessage());
    }

    @Test
    void testSave_InvalidStartEndTime() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setStartTime(LocalTime.of(10, 0));
        requestDto.setEndTime(LocalTime.of(9, 0));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.save(requestDto));

        assertEquals("This booking can only take place in a time machine!", exception.getMessage());
    }

    @Test
    void testSave_NotHourlyDuration() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setStartTime(LocalTime.of(10, 0));
        requestDto.setEndTime(LocalTime.of(11, 30));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> bookingService.save(requestDto));

        assertEquals("Bookings should last at least 1 hour or consecutive multiples of 1 hour (2, 3, 4, ...).",
                exception.getMessage());
    }

    @Test
    void testSave_InvalidRoomName() {
        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setRoomName("Earth");

        when(roomRepository.findByName("room1")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.save(requestDto));

        assertEquals("Room not found: Earth", exception.getMessage());
    }

    @Test
    void testSave_InvalidEmployeeEmail() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setEmployeeEmail("eddie@acme.com");

        Room room = buildRoom();

        when(roomRepository.findByName("room1")).thenReturn(Optional.of(room));
        when(employeeRepository.findByEmail("dinos@acme.com")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.save(requestDto));

        assertEquals("Employee not found: eddie@acme.com", exception.getMessage());
    }

    @Test
    void testSave_SameBookingAlreadyExists() {
        BookingRequestDto requestDto = builidValidBookingRequestDto();
        Room room = buildRoom();
        Employee employee = buildEmployee();

        when(roomRepository.findByName("room1")).thenReturn(Optional.of(room));
        when(employeeRepository.findByEmail("dinos@acme.com")).thenReturn(Optional.of(employee));
        when(bookingRepository.existsByRoomAndBookingDateAndStartTimeLessThanAndEndTimeGreaterThan(room, requestDto.getBookingDate(),
                requestDto.getEndTime(), requestDto.getStartTime())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class,
                () -> bookingService.save(requestDto));

        assertEquals("The booking overlaps with an existing booking for the same room.",
                exception.getMessage());
    }

    @Test
    void testCancel_Successful() {

        Long bookingId = 1L;
        Booking booking = buildBooking(buildRoom(), buildEmployee());
        booking.setBookingDate(LocalDate.of(2025, 11, 19));
        booking.setStartTime(LocalTime.of(10, 0));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        String result = bookingService.cancel(bookingId);

        assertEquals("Booking was cancelled successfully.", result);
    }

    @Test
    void testCancel_PastBooking() {

        Long bookingId = 1L;
        Booking booking = buildBooking(buildRoom(), buildEmployee());
        booking.setBookingDate(LocalDate.now().minusDays(1));
        booking.setStartTime(LocalTime.of(10, 0));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.cancel(bookingId));

        assertEquals("This is not a future booking so it cannot be canceled.", exception.getMessage());
    }

    @Test
    void testCancel_BookingNotFound() {

        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.cancel(bookingId));

        assertEquals("Booking was not found.", exception.getMessage());
    }

    private static Employee buildEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail("dinos@acme.com");
        return employee;
    }

    private static Room buildRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setName("room1");
        return room;
    }

    private static Booking buildBooking(Room room, Employee employee) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingDate(LocalDate.of(2024, 11, 17));
        booking.setRoom(room);
        booking.setEmployee(employee);
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(12, 0));
        return booking;
    }

    private static BookingRequestDto builidValidBookingRequestDto() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setRoomName("room1");
        requestDto.setEmployeeEmail("dinos@acme.com");
        requestDto.setBookingDate(LocalDate.of(2024, 12, 17));
        requestDto.setStartTime(LocalTime.of(10, 0));
        requestDto.setEndTime(LocalTime.of(12, 0));
        return requestDto;
    }
}
