package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    
    PageResult pageQuery4User(int page, int pageSize, Integer status);
    
    OrderVO details(Long id);
    
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);
    
    void paySuccess(String outTradeNo);
    
    void userCancelById(Long id);
    
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    
    OrderStatisticsVO statistics();
    
    void confirm(OrdersConfirmDTO ordersConfirmDTO);
    
    void rejection(OrdersRejectionDTO ordersRejectionDTO);
    
    void cancel(OrdersCancelDTO ordersCancelDTO);
    
    void delivery(Long id);
    
    void complete(Long id);
    
    void reminder(Long id);
}
