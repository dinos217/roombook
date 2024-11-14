package com.acmelabinc.roombook.repositories;

import com.acmelabinc.roombook.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Short, Room> {
}
