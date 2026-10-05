package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel(description = "订单支付参数")
public class OrdersPaymentDTO implements Serializable {
    //订单号
    @ApiModelProperty(value = "订单号", required = true)
    private String orderNumber;

    //付款方式
    @ApiModelProperty(value = "付款方式，1微信支付", example = "1")
    private Integer payMethod;

}
