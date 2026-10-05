package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户提交订单参数")
public class OrdersSubmitDTO implements Serializable {
    //地址簿id
    @ApiModelProperty(value = "地址簿id", required = true, example = "1")
    private Long addressBookId;
    //付款方式
    @ApiModelProperty(value = "付款方式，1微信，2支付宝", example = "1")
    private int payMethod;
    //备注
    @ApiModelProperty("订单备注")
    private String remark;
    //预计送达时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "预计送达时间", example = "2026-10-04 18:00:00")
    private LocalDateTime estimatedDeliveryTime;
    //配送状态  1立即送出  0选择具体时间
    @ApiModelProperty(value = "配送状态，1立即送出，0选择具体时间", example = "1")
    private Integer deliveryStatus;
    //餐具数量
    @ApiModelProperty(value = "餐具数量", example = "1")
    private Integer tablewareNumber;
    //餐具数量状态  1按餐量提供  0选择具体数量
    @ApiModelProperty(value = "餐具数量状态，1按餐量提供，0选择具体数量", example = "1")
    private Integer tablewareStatus;
    //打包费
    @ApiModelProperty(value = "打包费", example = "1")
    private Integer packAmount;
    //总金额
    @ApiModelProperty(value = "订单总金额", required = true, example = "88.00")
    private BigDecimal amount;
}
