package com.sky.dto;

import com.sky.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(description = "菜品信息传递的数据模型")
public class DishDTO implements Serializable {

    @ApiModelProperty(value = "菜品id", example = "1")
    private Long id;
    //菜品名称
    @ApiModelProperty(value = "菜品名称", required = true, example = "鱼香肉丝")
    private String name;
    //菜品分类id
    @ApiModelProperty(value = "菜品分类id", required = true, example = "1")
    private Long categoryId;
    //菜品价格
    @ApiModelProperty(value = "菜品价格", required = true, example = "28.00")
    private BigDecimal price;
    //图片
    @ApiModelProperty(value = "菜品图片路径", required = true)
    private String image;
    //描述信息
    @ApiModelProperty(value = "菜品描述")
    private String description;
    //0 停售 1 起售
    @ApiModelProperty(value = "菜品状态，1为起售，0为停售", example = "1")
    private Integer status;
    //口味
    @ApiModelProperty("菜品口味")
    private List<DishFlavor> flavors = new ArrayList<>();

}
