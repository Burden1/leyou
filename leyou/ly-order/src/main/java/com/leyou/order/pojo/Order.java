package com.leyou.order.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_order")
public class Order {
    @Id
    private Long orderId;
    private Long totalPay;
    private Long actualPay;
    private Integer paymentType;
    private String promotionIds;
    private Long postFee = 0L;
    private Date createTime;
    private String shippingName;
    private String shippingCode;
    private Long userId;
    private String buyerMessage;
    private String buyerNick;
    private Boolean buyerRate;
    private String receiver;
    private String receiverMobile;
    private String receiverState;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String receiverZip;
    private Integer invoiceType = 0; // 发票类型
    private Integer sourceType = 1;

    @Transient
    private OrderStatus orderStatus;

    @Transient
    private List<OrderDetail> orderDetails;
}