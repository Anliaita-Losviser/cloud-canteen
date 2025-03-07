package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("userOrderController")
@RequestMapping("user/order")
@Slf4j
public class OrderController {
    
    @Resource(name = "orderServiceImpl")
    private OrderService orderService;
    
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        log.info("历史订单查询，页码：{}，每页记录数：{}", page, pageSize);
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }
    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("查询订单详情，订单id：{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }
    
    /**
     * 用户取消订单
     *
     * @return
     */
//    @PutMapping("/cancel/{id}")
//    public Result cancel(@PathVariable("id") Long id) throws Exception {
//        orderService.userCancelById(id);
//        return Result.success();
//    }

}
