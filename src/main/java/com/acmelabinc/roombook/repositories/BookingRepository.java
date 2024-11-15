package com.acmelabinc.roombook.repositories;

import com.acmelabinc.roombook.entities.Booking;
import com.acmelabinc.roombook.entities.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByRoomAndBookingDate(Room room, LocalDate date, Pageable pageable);
    Boolean existsByRoomAndBookingDateAndStartTime(Room room, LocalDate localDate, LocalTime startTime, LocalTime endTime);
}
