package com.sky.dao;

import com.sky.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeDAO extends JpaRepository<Employee,Long> {
    
    Employee findByUsername(String username);
    
}
