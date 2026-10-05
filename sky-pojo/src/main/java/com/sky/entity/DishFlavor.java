package com.sky.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜品口味
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "菜品口味")
public class DishFlavor implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "口味id", example = "1")
    private Long id;
    //菜品id
    @ApiModelProperty(value = "菜品id", example = "1")
    private Long dishId;

    //口味名称
    @ApiModelProperty(value = "口味名称", required = true, example = "辣度")
    private String name;

    //口味数据list
    @ApiModelProperty(value = "口味值", required = true, example = "[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]")
    private String value;

}
