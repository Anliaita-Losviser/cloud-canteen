package com.sky.dao;

import com.sky.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDAO extends JpaRepository<Orders,Long> {
    Orders findByNumber(String number);
    Integer countByStatus(Integer status);
    
    List<Orders> findByStatusAndOrderTimeLessThan(Integer status, LocalDateTime orderTime);
}
