package com.acmelabinc.roombook.repositories;

import com.acmelabinc.roombook.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Long, Employee> {
}
