package com.sky.dao;

import com.sky.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailDAO extends JpaRepository<OrderDetail,Long> {
    List<OrderDetail> findByOrderId(Long orderId);
}
