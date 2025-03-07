package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dao.OrderDetailDAO;
import com.sky.dao.OrderDAO;
import com.sky.dao.ShoppingCartDAO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    @Resource(name = "addressBookMapper")
    private AddressBookMapper addressBookMapper;
    @Resource(name = "shoppingCartDAO")
    private ShoppingCartDAO shoppingCartDAO;
    @Resource(name = "orderDAO")
    private OrderDAO orderDAO;
    @Resource(name = "orderMapper")
    private OrderMapper orderMapper;
    @Resource(name = "orderDetailDAO")
    private OrderDetailDAO orderDetailDAO;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常
        //判断地址是否存在
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartDAO.findByUserId(userId);
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        
        orderDAO.save(orders);
        //向订单明细表插入n条数据
        Orders prevOrder = orderDAO.findByNumber(orders.getNumber());
        
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart shoppingCart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setId(null);
            orderDetail.setOrderId(prevOrder.getId());//关联订单ID
            orderDetailList.add(orderDetail);
        }
        orderDetailDAO.saveAll(orderDetailList);
        //清空当前购物车数据
        
        shoppingCartDAO.deleteAllInBatchByUserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(prevOrder.getId())
                .orderNumber(prevOrder.getNumber())
                .orderAmount(prevOrder.getAmount())
                .orderTime(prevOrder.getOrderTime())
                .build();
        
        return orderSubmitVO;
    }
    
    /**
     * 用户端订单分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailDAO.findByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }
    
    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        // 根据id查询订单
        Orders orders = orderDAO.findById(id).get();

        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailDAO.findByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }
    
    /**
     * 用户取消订单
     *
     * @param id
     */
//    @Override
//    public void userCancelById(Long id) throws Exception {
//        // 根据id查询订单
//        Orders ordersDB = orderDAO.findById(id).get();
//
//        // 校验订单是否存在
//        if (ordersDB == null) {
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
//        }
//
//        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
//        if (ordersDB.getStatus() > 2) {
//            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
//        }
//
//        Orders orders = new Orders();
//        orders.setId(ordersDB.getId());
//
//        // 订单处于待接单状态下取消，需要进行退款
//        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//
//            //支付状态修改为 退款
//            orders.setPayStatus(Orders.REFUND);
//        }
//
//        // 更新订单状态、取消原因、取消时间
//        orders.setStatus(Orders.CANCELLED);
//        orders.setCancelReason("用户取消");
//        orders.setCancelTime(LocalDateTime.now());
//        orderMapper.update(orders);
//    }
}
