package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户下单返回结果")
public class OrderSubmitVO implements Serializable {
    //订单id
    @ApiModelProperty("订单id")
    private Long id;
    //订单号
    @ApiModelProperty("订单号")
    private String orderNumber;
    //订单金额
    @ApiModelProperty("订单金额")
    private BigDecimal orderAmount;
    //下单时间
    @ApiModelProperty("下单时间")
    private LocalDateTime orderTime;
}
