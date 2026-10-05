package com.sky.controller.admin;

import com.sky.constant.RedisConstants;
import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端店铺管理
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺管理接口")
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

    /**
     * 设置营业状态
     *
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(
            @ApiParam(value = "店铺营业状态，1为营业，0为打烊", required = true, example = "1")
            @PathVariable Integer status) {
        if (!StatusConstant.ENABLE.equals(status) && !StatusConstant.DISABLE.equals(status)) {
            return Result.error("营业状态只能为0或1");
        }

        stringRedisTemplate.opsForValue().set(RedisConstants.SHOP_STATUS, String.valueOf(status));
        log.info("设置店铺营业状态为：{}", StatusConstant.ENABLE.equals(status) ? "营业中" : "打烊中");
        return Result.success();
    }

}
