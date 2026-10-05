package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "分类信息传递的数据模型")
public class CategoryDTO implements Serializable {

    //主键
    @ApiModelProperty(value = "分类id", example = "1")
    private Long id;

    //类型 1 菜品分类 2 套餐分类
    @ApiModelProperty(value = "分类类型，1为菜品分类，2为套餐分类", required = true, example = "1")
    private Integer type;

    //分类名称
    @ApiModelProperty(value = "分类名称", required = true, example = "川菜")
    private String name;

    //排序
    @ApiModelProperty(value = "排序，按升序排列", required = true, example = "1")
    private Integer sort;

}
