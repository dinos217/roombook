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

import java.time.LocalDate;
import java.time.LocalTime;
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
    void testSave() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        Room room = buildRoom();
        Employee employee = buildEmployee();
        Booking booking = buildBooking(room, employee);

        when(roomRepository.findByName("room1")).thenReturn(Optional.of(room));
        when(employeeRepository.findByEmail("dinos@acme.com")).thenReturn(Optional.of(employee));
        when(bookingRepository.existsByRoomAndBookingDateAndStartTimeAndEndTime(room, requestDto.getBookingDate(),
                requestDto.getStartTime(), requestDto.getEndTime())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.save(requestDto);

        assertEquals(result.getBookedBy(), (requestDto.getEmployeeEmail()));
        assertEquals(result.getBookingDate(), (LocalDate.of(2024, 11, 17)));
        assertEquals(result.getRoom(), (requestDto.getRoomName()));
    }

    @Test
    void testSaveWithInvalidStartEndTime() {

        BookingRequestDto requestDto = builidInvalidStartEndTimeBookingRequestDto();

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.save(requestDto));

        assertEquals("This booking can only take place in a time machine!", exception.getMessage());
    }

    @Test
    void testSaveWithNotHourlyDuration() {

        BookingRequestDto requestDto = builidNotHourlyBookingRequestDto();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> bookingService.save(requestDto));

        assertEquals("Bookings should last at least 1 hour or consecutive multiples of 1 hour (2, 3, 4, ...).",
                exception.getMessage());
    }

    @Test
    void testSaveInvalidRoomName() {
        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setRoomName("Earth");

        when(roomRepository.findByName("room1")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.save(requestDto));

        assertEquals("Room not found: Earth", exception.getMessage());
    }

    @Test
    void testSaveInvalidEmployeeEmail() {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setEmployeeEmail("eddie@acme.com");

        Room room = buildRoom();

        when(roomRepository.findByName("room1")).thenReturn(Optional.of(room));
        when(employeeRepository.findByEmail("dinos@acme.com")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.save(requestDto));

        assertEquals("Employee not found: eddie@acme.com", exception.getMessage());
    }

    @Test
    void testSaveButSameBookingAlreadyExists() {
        BookingRequestDto requestDto = builidValidBookingRequestDto();
        Room room = buildRoom();
        Employee employee = buildEmployee();
        Booking booking = buildBooking(room, employee);

        when(roomRepository.findByName("room1")).thenReturn(Optional.of(room));
        when(employeeRepository.findByEmail("dinos@acme.com")).thenReturn(Optional.of(employee));
        when(bookingRepository.existsByRoomAndBookingDateAndStartTimeAndEndTime(room, requestDto.getBookingDate(),
                requestDto.getStartTime(), requestDto.getEndTime())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class,
                () -> bookingService.save(requestDto));

        assertEquals("The booking overlaps with an existing booking for the same room.",
                exception.getMessage());
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
        requestDto.setBookingDate(LocalDate.of(2024, 11, 17));
        requestDto.setStartTime(LocalTime.of(10, 0));
        requestDto.setEndTime(LocalTime.of(12, 0));
        return requestDto;
    }

    private static BookingRequestDto builidInvalidStartEndTimeBookingRequestDto() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setRoomName("room1");
        requestDto.setEmployeeEmail("dinos@acme.com");
        requestDto.setBookingDate(LocalDate.of(2024, 11, 17));
        requestDto.setStartTime(LocalTime.of(10, 0));
        requestDto.setEndTime(LocalTime.of(9, 0));
        return requestDto;
    }

    private static BookingRequestDto builidNotHourlyBookingRequestDto() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setRoomName("room1");
        requestDto.setEmployeeEmail("dinos@acme.com");
        requestDto.setBookingDate(LocalDate.of(2024, 11, 17));
        requestDto.setStartTime(LocalTime.of(10, 0));
        requestDto.setEndTime(LocalTime.of(11, 30));
        return requestDto;
    }
}
