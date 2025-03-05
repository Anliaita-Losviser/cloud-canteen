package com.sky.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "snowflake-id",strategy = "com.sky.service.impl.IdGenerateServiceImpl")
    @GeneratedValue(generator = "snowflake-id")
    private Long id;

    //微信用户唯一标识
    @Column(name = "openid",length = 45)
    private String openid;

    //姓名
    @Column(name = "name", length = 32)
    private String name;

    //手机号
    @Column(name = "phone", length = 11)
    private String phone;

    //性别 0 女 1 男
    @Column(name = "sex", length = 2)
    private String sex;

    //身份证号
    @Column(name = "id_number", length = 18)
    private String idNumber;

    //头像
    @Column(name = "avatar",length = 500)
    private String avatar;

    //注册时间
    @Column(name = "create_time")
    @CreatedDate
    private LocalDateTime createTime;
}
