package com.sky.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "order_detail")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "snowflake-id",strategy = "com.sky.service.impl.IdGenerateServiceImpl")
    @GeneratedValue(generator = "snowflake-id")
    private Long id;

    //名称
    @Column(length = 32)
    private String name;

    //订单id
    private Long orderId;

    //菜品id
    private Long dishId;

    //套餐id
    private Long setmealId;

    //口味
    @Column(length = 50)
    private String dishFlavor;

    //数量
    private Integer number;

    //金额
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    //图片
    private String image;
}
