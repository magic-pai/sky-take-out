package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel(description = "购物车商品参数")
public class ShoppingCartDTO implements Serializable {

    @ApiModelProperty(value = "菜品id", example = "1")
    private Long dishId;

    @ApiModelProperty(value = "套餐id", example = "1")
    private Long setmealId;

    @ApiModelProperty(value = "菜品口味", example = "微辣")
    private String dishFlavor;

}
