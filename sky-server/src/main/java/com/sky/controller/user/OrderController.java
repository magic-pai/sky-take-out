package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(
            @ApiParam(value = "提交订单参数", required = true)
            @RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        return Result.success(orderService.submitOrder(ordersSubmitDTO));
    }

    /**
     * 订单扫码支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单扫码支付")
    public Result<OrderPaymentVO> payment(
            @ApiParam(value = "支付参数", required = true)
            @RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("订单扫码支付：{}", ordersPaymentDTO);
        return Result.success(orderService.payment(ordersPaymentDTO));
    }

    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(
            @ApiParam(value = "页码", required = true, example = "1")
            int page,
            @ApiParam(value = "每页记录数", required = true, example = "10")
            int pageSize,
            @ApiParam(value = "订单状态：1待付款，2待接单，3已接单，4派送中，5已完成，6已取消")
            Integer status) {
        log.info("历史订单查询：page={}, pageSize={}, status={}", page, pageSize, status);
        return Result.success(orderService.pageQuery4User(page, pageSize, status));
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(
            @ApiParam(value = "订单id", required = true, example = "1")
            @PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        return Result.success(orderService.details(id));
    }

    /**
     * 用户取消订单
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(
            @ApiParam(value = "订单id", required = true, example = "1")
            @PathVariable Long id) {
        log.info("用户取消订单：{}", id);
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(
            @ApiParam(value = "订单id", required = true, example = "1")
            @PathVariable Long id) {
        log.info("再来一单：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 客户催单
     *
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("客户催单")
    public Result reminder(
            @ApiParam(value = "订单id", required = true, example = "1")
            @PathVariable Long id) {
        log.info("客户催单：{}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
