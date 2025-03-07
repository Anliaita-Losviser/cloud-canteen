package com.sky.dao;

import com.sky.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDAO extends JpaRepository<Orders,Long> {
    Orders findByNumber(String number);
}
