package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCartQuery = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCartQuery);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(addressBook.getId())
                .orderTime(LocalDateTime.now())
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .amount(ordersSubmitDTO.getAmount())
                .remark(ordersSubmitDTO.getRemark())
                .phone(addressBook.getPhone())
                .consignee(addressBook.getConsignee())
                .address(addressBook.getDetail())
                .estimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime())
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())
                .packAmount(ordersSubmitDTO.getPackAmount() == null ? 0 : ordersSubmitDTO.getPackAmount())
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber() == null ? 0 : ordersSubmitDTO.getTablewareNumber())
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .build();
        if (orders.getAmount() == null) {
            orders.setAmount(calculateCartAmount(shoppingCartList));
        }
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(cart.getName());
            orderDetail.setImage(cart.getImage());
            orderDetail.setDishId(cart.getDishId());
            orderDetail.setSetmealId(cart.getSetmealId());
            orderDetail.setDishFlavor(cart.getDishFlavor());
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setAmount(cart.getAmount());
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 扫码支付，订单金额统一为0.10元
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        BigDecimal payAmount = new BigDecimal("0.10");
        Orders updateOrder = Orders.builder()
                .id(orders.getId())
                .amount(payAmount)
                .payMethod(1)
                .payStatus(Orders.PAID)
                .status(Orders.TO_BE_CONFIRMED)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);

        Map<String, Object> message = new HashMap<>();
        message.put("type", 1);
        message.put("orderId", orders.getId());
        message.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(message));

        return OrderPaymentVO.builder()
                .amount(payAmount)
                .qrCodeUrl("/static/pay_qr.png")
                .paymentStatus("PAID")
                .build();
    }

    /**
     * 用户历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
        queryDTO.setUserId(BaseContext.getCurrentId());
        queryDTO.setStatus(status);
        Page<Orders> pageResult = orderMapper.pageQuery(queryDTO);

        List<OrderVO> records = new ArrayList<>();
        if (pageResult != null && pageResult.getTotal() > 0) {
            for (Orders orders : pageResult) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orders.getId()));
                records.add(orderVO);
            }
        }
        return new PageResult(pageResult == null ? 0 : pageResult.getTotal(), records);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orders.getId()));
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    public void userCancelById(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        Long userId = BaseContext.getCurrentId();
        if (ordersDB == null || !Objects.equals(ordersDB.getUserId(), userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(ordersDB.getStatus())
                && !Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now())
                .build();
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Transactional
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = orderMapper.getById(id);
        if (orders == null || !Objects.equals(orders.getUserId(), userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(detail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(detail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 管理端订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> records = new ArrayList<>();
        if (page != null && !CollectionUtils.isEmpty(page.getResult())) {
            records = page.getResult().stream().map(orders -> {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDishes(getOrderDishesStr(orders));
                return orderVO;
            }).collect(Collectors.toList());
        }
        return new PageResult(page == null ? 0 : page.getTotal(), records);
    }

    /**
     * 订单状态数量统计
     *
     * @return
     */
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO statisticsVO = new OrderStatisticsVO();
        statisticsVO.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        statisticsVO.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        statisticsVO.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return statisticsVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders ordersDB = orderMapper.getById(ordersConfirmDTO.getId());
        if (ordersDB == null || !Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        if (ordersDB == null || !Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (Orders.CANCELLED.equals(ordersDB.getStatus()) || Orders.COMPLETED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !Orders.CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !Orders.DELIVERY_IN_PROGRESS.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 客户催单
     *
     * @param id
     */
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        Long userId = BaseContext.getCurrentId();
        if (ordersDB == null || !Objects.equals(ordersDB.getUserId(), userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())
                && !Orders.CONFIRMED.equals(ordersDB.getStatus())
                && !Orders.DELIVERY_IN_PROGRESS.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", 2);
        message.put("orderId", id);
        message.put("content", "订单号：" + ordersDB.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(message));
    }

    /**
     * 获取订单菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        if (CollectionUtils.isEmpty(orderDetailList)) {
            return "";
        }
        return orderDetailList.stream()
                .map(detail -> detail.getName() + "*" + detail.getNumber() + ";")
                .collect(Collectors.joining());
    }

    /**
     * 根据购物车计算订单金额
     *
     * @param shoppingCartList
     * @return
     */
    private BigDecimal calculateCartAmount(List<ShoppingCart> shoppingCartList) {
        BigDecimal amount = BigDecimal.ZERO;
        for (ShoppingCart cart : shoppingCartList) {
            amount = amount.add(cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())));
        }
        return amount;
    }

}
