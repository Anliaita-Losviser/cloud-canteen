package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    
    @Resource(name = "redisTemplate")
    private RedisTemplate<Object,Object> redisTemplate;
    
    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态：{}",status == 1?"营业中":"打烊了");
        redisTemplate.opsForValue().set("SHOP_STATUS",status);
        return Result.success();
    }
    
    /**
     * 查询店铺营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("查询店铺营业状态：{}",status == 1?"营业中":"打烊了");
        return Result.success(status);
    }
}
