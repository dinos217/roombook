package com.acmelabinc.roombook.services;

import com.acmelabinc.roombook.dtos.BookingRequestDto;
import com.acmelabinc.roombook.dtos.BookingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface BookingService {

    Page<BookingResponseDto> getByRoomAndDate(String roomName, LocalDate date, Pageable pageable);
    BookingResponseDto save(BookingRequestDto bookingRequestDto);
    String cancel(Long id);
}
