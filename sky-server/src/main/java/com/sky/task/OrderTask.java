package com.sky.task;

import com.sky.dao.OrderDAO;
import com.sky.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    
    @Resource(name = "orderDAO")
    private OrderDAO orderDAO;
    
    /**
     * 处理超时订单，每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    //@Scheduled(cron = "1/5 * * * * ?")
    public void processTimeoutOrder() {
        log.info("订单超时检查: {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderDAO.findByStatusAndOrderTimeLessThan(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            log.info("超时订单数：{}", ordersList.size());
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderDAO.save(orders);
            }
        }
    }
    
    /**
     * 处理一直处于派送中的订单，每天触发一次
     */
    
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "0/5 * * * * ?")
    public void processDeliveryOrder() {
        log.info("派送订单检查: {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderDAO.findByStatusAndOrderTimeLessThan(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            log.info("派送中订单数：{}", ordersList.size());
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderDAO.save(orders);
            }
        }
    }
}
