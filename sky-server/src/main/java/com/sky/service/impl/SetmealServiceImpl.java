package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dao.SetmealDAO;
import com.sky.dao.SetmealDishDAO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapStruct.SetmealMapStruct;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Resource(name = "setmealDishDAO")
    private SetmealDishDAO setmealDishDAO;
    @Resource(name = "setmealDAO")
    private SetmealDAO setmealDAO;
    @Resource(name = "setmealMapper")
    private SetmealMapper setmealMapper;
    @Resource(name = "dishMapper")
    private DishMapper dishMapper;
    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithDish(SetmealDTO setmealDTO) {
        //保存套餐
        Setmeal setmeal = SetmealMapStruct.instance.convertToSetmeal(setmealDTO);
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealDAO.save(setmeal);
        //获取套餐ID
        Setmeal savedSetmeal = setmealDAO.findByName(setmeal.getName());
        Long setmealId = savedSetmeal.getId();
        //保存套餐和菜品的关联关系
        //先获取菜品数组
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            //设置套餐ID
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishDAO.saveAll(setmealDishes);
    }
    
    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
    
    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealDAO.findById(id).get();
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        ids.forEach(setmealId -> {
            //删除套餐表中的数据
            setmealDAO.deleteById(setmealId);
            //删除套餐菜品关系表中的数据
            setmealDishDAO.deleteAllInBatchBySetmealId(setmealId);
        });
    }
    
    /**
     * 根据id查询套餐，用于修改页面回显数据
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //查套餐
        Setmeal setmeal = setmealDAO.findById(id).get();
        //查套餐与菜品关系
        List<SetmealDish> setmealDishes = setmealDishDAO.findBySetmealId(id);
        //封装到VO里面
        SetmealVO setmealVO = SetmealMapStruct.instance.convertToSetmealVO(setmeal);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }
    
    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        //1、修改套餐表，执行update
        //获得更新后的信息
        Setmeal updatedSetmeal = SetmealMapStruct.instance.convertToSetmeal(setmealDTO);
        //查找更新前的信息
        Setmeal previousSetmeal = setmealDAO.findById(updatedSetmeal.getId()).get();
        //设置字段
        previousSetmeal.setCategoryId(updatedSetmeal.getCategoryId());
        previousSetmeal.setName(updatedSetmeal.getName());
        previousSetmeal.setPrice(updatedSetmeal.getPrice());
        previousSetmeal.setDescription(updatedSetmeal.getDescription());
        previousSetmeal.setImage(updatedSetmeal.getImage());
        previousSetmeal.setUpdateUser(BaseContext.getCurrentId());
        //获取套餐id
        Long setmealId = setmealDTO.getId();
        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishDAO.deleteAllInBatchBySetmealId(setmealId);
        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishDAO.saveAll(setmealDishes);
    }
    
    /**
     * 套餐起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if (status.equals(StatusConstant.ENABLE)) {
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && !dishList.isEmpty()) {
                dishList.forEach(dish -> {
                    if (StatusConstant.DISABLE.equals(dish.getStatus())) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = setmealDAO.findById(id).get();
        setmeal.setStatus(status);
        setmealDAO.save(setmeal);
    }
}
