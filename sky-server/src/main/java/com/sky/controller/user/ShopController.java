package com.sky.controller.user;

import com.sky.constant.RedisConstants;
import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "C端-店铺操作接口")
public class ShopController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus() {
        String status = stringRedisTemplate.opsForValue().get(RedisConstants.SHOP_STATUS);
        Integer shopStatus = status == null ? StatusConstant.ENABLE : Integer.valueOf(status);
        return Result.success(shopStatus);
    }

}
