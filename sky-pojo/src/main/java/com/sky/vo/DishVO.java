package com.sky.vo;

import com.sky.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "菜品查询返回的数据格式")
public class DishVO implements Serializable {

    @ApiModelProperty(value = "菜品id", example = "1")
    private Long id;
    //菜品名称
    @ApiModelProperty(value = "菜品名称", example = "宫保鸡丁")
    private String name;
    //菜品分类id
    @ApiModelProperty(value = "菜品分类id", example = "1")
    private Long categoryId;
    //菜品价格
    @ApiModelProperty(value = "菜品价格", example = "28.00")
    private BigDecimal price;
    //图片
    @ApiModelProperty("菜品图片")
    private String image;
    //描述信息
    @ApiModelProperty("菜品描述")
    private String description;
    //0 停售 1 起售
    @ApiModelProperty(value = "菜品状态，1为起售，0为停售", example = "1")
    private Integer status;
    //更新时间
    @ApiModelProperty("最后操作时间")
    private LocalDateTime updateTime;
    //分类名称
    @ApiModelProperty(value = "分类名称", example = "热菜")
    private String categoryName;
    //菜品关联的口味
    @ApiModelProperty("菜品关联的口味")
    private List<DishFlavor> flavors = new ArrayList<>();

    //private Integer copies;
}
