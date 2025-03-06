package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    
    @Resource(name = "dishServiceImpl")
    private DishService dishService;
    //@Resource(name = "redisTemplate")
    //private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "dishCache",key = "#dishDTO.categoryId")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清理缓存
        //String key = "dish_"+dishDTO.getCategoryId();
        //redisTemplate.delete(key);
        
        return Result.success();
    }
    
    /**
     * 菜品分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    
    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        //清理缓存
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);
        return Result.success();
    }
    
    /**
     * 根据ID查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdwithFlavor(id);
        return Result.success(dishVO);
    }
    
    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //清理缓存
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);
        return Result.success();
    }
    
    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售:{},{}",id,status);
        dishService.startOrStop(status, id);
        //清理缓存
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);
        return Result.success();
    }
    
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
}
