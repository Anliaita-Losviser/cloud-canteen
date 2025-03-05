package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dao.UserDAO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    
    @Resource(name = "weChatProperties")
    private WeChatProperties weChatProperties;
    @Resource(name = "userDAO")
    private UserDAO userDAO;
    /**
     * 微信用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信API，获得当前微信用户的openid
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",userLoginDTO.getCode());
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN,map);
        
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        
        //判断openid是不是空
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        
        //判断用户是不是新注册的
        User userIfExit = userDAO.findByOpenid(openid);
        
        //是新用户要完成注册
        if(userIfExit == null){
            userIfExit = User.builder()
                    .openid(openid)
                    .build();
            //往用户表中添加新用户
            userDAO.save(userIfExit);
        }
        return userIfExit;
    }
}
