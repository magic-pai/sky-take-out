package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "员工登录返回的数据格式")
public class EmployeeLoginVO implements Serializable {

    @ApiModelProperty(value = "主键值", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户名", example = "admin")
    private String userName;

    @ApiModelProperty(value = "姓名", example = "管理员")
    private String name;

    @ApiModelProperty(value = "JWT 令牌，用于后续管理端请求身份校验")
    private String token;

}
