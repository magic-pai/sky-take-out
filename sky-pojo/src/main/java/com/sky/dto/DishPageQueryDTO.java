package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "菜品分页查询参数")
public class DishPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码", required = true, example = "1")
    private int page;

    @ApiModelProperty(value = "每页记录数", required = true, example = "10")
    private int pageSize;

    @ApiModelProperty(value = "菜品名称", example = "宫保鸡丁")
    private String name;

    //分类id
    @ApiModelProperty(value = "分类id", example = "1")
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    @ApiModelProperty(value = "菜品状态，1为起售，0为停售", example = "1")
    private Integer status;

}
