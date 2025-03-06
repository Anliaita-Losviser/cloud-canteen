package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dao.DishDAO;
import com.sky.dao.SetmealDAO;
import com.sky.dao.ShoppingCartDAO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    
    @Resource(name = "shoppingCartMapper")
    private ShoppingCartMapper shoppingCartMapper;
    @Resource(name = "shoppingCartDAO")
    private ShoppingCartDAO shoppingCartDAO;
    @Resource(name = "dishDAO")
    private DishDAO dishDAO;
    @Resource(name = "setmealDAO")
    private SetmealDAO setmealDAO;
    
    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断同款商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //存在则数量加1
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartDAO.save(cart);
        } else {
            //不存在则插入
            //判断提交过来的是菜品还套餐
            if (shoppingCart.getDishId() != null) {
                //提交的是菜品
                Dish dish = dishDAO.findById(shoppingCart.getDishId()).get();
                
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setNumber(1);
                
                shoppingCartDAO.save(shoppingCart);
            } else {
                Setmeal setmeal = setmealDAO.findById(shoppingCart.getSetmealId()).get();
                
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setNumber(1);
                
                shoppingCartDAO.save(shoppingCart);
            }
        }
    }
    
    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartDAO.findByUserId(BaseContext.getCurrentId());
    }
    
    /**
     * 清空购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanShoppingCart() {
        shoppingCartDAO.deleteAllInBatchByUserId(BaseContext.getCurrentId());
    }
    
    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //设置查询条件，查询当前登录用户的购物车数据
        //shoppingCart.setUserId(BaseContext.getCurrentId());
        
        List<ShoppingCart> list = shoppingCartDAO.findByUserId(BaseContext.getCurrentId());
        
        if (list != null && !list.isEmpty()) {
            shoppingCart = list.get(0);
            
            Integer number = shoppingCart.getNumber();
            if (number == 1) {
                //当前商品在购物车中的份数为1，直接删除当前记录
                shoppingCartDAO.deleteById(shoppingCart.getId());
            } else {
                //当前商品在购物车中的份数不为1，修改份数即可
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartDAO.save(shoppingCart);
            }
        }
    }
}
