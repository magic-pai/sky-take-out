package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "员工分页查询参数")
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    @ApiModelProperty(value = "员工姓名", example = "张三")
    private String name;

    //页码
    @ApiModelProperty(value = "页码", required = true, example = "1")
    private int page;

    //每页显示记录数
    @ApiModelProperty(value = "每页记录数", required = true, example = "10")
    private int pageSize;

}
