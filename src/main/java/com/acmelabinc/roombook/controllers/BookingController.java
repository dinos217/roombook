package com.acmelabinc.roombook.controllers;

import com.acmelabinc.roombook.dtos.BookingRequestDto;
import com.acmelabinc.roombook.dtos.BookingResponseDto;
import com.acmelabinc.roombook.services.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController(value = "/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping(value = "/per-room")
    ResponseEntity<Page<BookingResponseDto>> getBookingsPerRoom(@PathVariable String roomName,
                                                                @PathVariable LocalDate date,
                                                                @RequestParam Integer page,
                                                                @RequestParam Integer pageSize,
                                                                @RequestParam String sortBy,
                                                                @RequestParam String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        return ResponseEntity.status(HttpStatus.OK).body(bookingService.getByRoomAndDate(roomName, date, pageable));
    }

    @PostMapping
    ResponseEntity<BookingResponseDto> save(@RequestBody BookingRequestDto bookingRequestDto) {

        return null;
    }

    @DeleteMapping(value = "/cancel")
    ResponseEntity<String> cancel(@RequestParam Long id) {

        return null;
    }

}
