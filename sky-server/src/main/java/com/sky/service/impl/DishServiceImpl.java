package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dao.DishDAO;
import com.sky.dao.DishFlavorDAO;
import com.sky.dao.SetmealDAO;
import com.sky.dao.SetmealDishDAO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapStruct.DishMapStruct;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    
    @Resource(name = "dishMapper")
    private DishMapper dishMapper;
    @Resource(name = "dishDAO")
    private DishDAO dishDAO;
    @Resource(name = "dishFlavorMapper")
    private DishFlavorMapper dishFlavorMapper;
    @Resource(name = "dishFlavorDAO")
    private DishFlavorDAO dishFlavorDAO;
    @Resource(name = "setmealDishDAO")
    private SetmealDishDAO setmealDishDAO;
    @Resource(name = "setmealDAO")
    private SetmealDAO setmealDAO;
    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDTO dishDTO) {
        //1.向菜品表添加数据
        Dish dish = DishMapStruct.instance.convertToDish(dishDTO);
        
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        //添加1条菜品数据
        dishDAO.save(dish);
        //查询刚刚添加的菜品，获取其ID
        Dish getPreId = dishDAO.findByName(dish.getName());
        Long dishId = getPreId.getId();
        
        //2.向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            //3.遍历口味数组，设置菜品id
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insert(flavors);
        }
    }
    
    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
    
    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        //1.判断是否存在起售中的菜品
        for (Long id : ids) {
            Optional<Dish> dish = dishDAO.findById(id);
            if (dish.get().getStatus().equals(StatusConstant.ENABLE)) {
                //起售中的菜品不允许删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.判断菜品是否被套餐关联
        List<Long> setmealIds = setmealDishDAO.findSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //3.删除菜品
        for (Long id : ids) {
            dishDAO.deleteById(id);
            //4.删除对应的口味
            dishFlavorDAO.deleteAllInBatchByDishId(id);
        }
    }
    
    /**
     * 根据ID查询菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdwithFlavor(Long id) {
        //查菜品
        Optional<Dish> dish = dishDAO.findById(id);
        //查口味
        List<DishFlavor> dishFlavors = dishFlavorDAO.findByDishId(id);
        //封装到VO里
        DishVO dishVO = DishMapStruct.instance.convertToDishVO(dish.get());
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }
    
    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithFlavor(DishDTO dishDTO) {
        //1.修改菜品表
        //获得更新后的信息
        Dish updatedDish = DishMapStruct.instance.convertToDish(dishDTO);
        //查找更新前的信息
        Dish previousDish = dishDAO.findById(updatedDish.getId()).get();
        //更新字段
        previousDish.setName(updatedDish.getName());
        previousDish.setCategoryId(updatedDish.getCategoryId());
        previousDish.setPrice(updatedDish.getPrice());
        previousDish.setImage(updatedDish.getImage());
        previousDish.setDescription(updatedDish.getDescription());
        //2.删除口味
        dishFlavorDAO.deleteAllInBatchByDishId(dishDTO.getId());
        //3.重新添加口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            //3.遍历口味数组，设置菜品id
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insert(flavors);
        }
    }
    
    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startOrStop(Integer status, Long id) {
        Dish dish = dishDAO.findById(id).get();
        dish.setStatus(status);
        dishDAO.save(dish);
        // 如果是停售操作，还需要将包含当前菜品的套餐也停售
        if (status.equals(StatusConstant.DISABLE)) {
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            //查询所有关联的套餐ID
            List<Long> setmealIds = setmealDishDAO.findSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && !setmealIds.isEmpty()) {
                //根据套餐ID查套餐
                List<Setmeal> setmeals = setmealDAO.findAllById(setmealIds);
                for (Setmeal setmeal: setmeals) {
                    //逐个停售
                    setmeal.setStatus(StatusConstant.DISABLE);
                    setmealDAO.save(setmeal);
                }
            }
        }
    }
}
