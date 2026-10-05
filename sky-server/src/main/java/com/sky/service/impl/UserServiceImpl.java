package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }

    /**
     * 调用微信接口，使用临时登录凭证换取openid
     *
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        boolean wechatConfigured = weChatProperties.getAppid() != null
                && !weChatProperties.getAppid().trim().isEmpty()
                && weChatProperties.getSecret() != null
                && !weChatProperties.getSecret().trim().isEmpty();
        if (!wechatConfigured
                && Boolean.TRUE.equals(weChatProperties.getDevLoginEnabled())
                && weChatProperties.getDevOpenid() != null
                && !weChatProperties.getDevOpenid().trim().isEmpty()) {
            log.warn("当前使用开发环境测试用户登录，openid={}", weChatProperties.getDevOpenid());
            return weChatProperties.getDevOpenid();
        }

        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String response = HttpClientUtil.doGet(WX_LOGIN, params);
        if (response == null || response.trim().isEmpty()) {
            return null;
        }

        JSONObject jsonObject = JSON.parseObject(response);
        if (jsonObject == null) {
            return null;
        }

        String openid = jsonObject.getString("openid");
        if (openid == null) {
            log.warn("微信登录凭证校验失败：{}", response);
        }
        return openid;
    }

}
