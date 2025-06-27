package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderDetail {
    private String message;
    private Integer code;
    private String status;
    @JsonProperty("user")
    private OrderUser user;
    @JsonProperty("order_amount")
    private Double orderAmount;
    @JsonProperty("order_status")
    private String orderStatus;
    @JsonProperty("order_date")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private String orderDate;
    private String requetes;
    @JsonProperty("order_id")
    private Integer orderId;
    private DeliveryAddress adresse;
    @JsonProperty("product_list")
    private List<ProductOrderDetail> productList;
    private OrderUser buyer;  // Added for the "requetes" field from response
    
    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public OrderUser getUser() { return user; }
    public void setUser(OrderUser user) { this.user = user; }
    
    public Double getOrderAmount() { return orderAmount; }
    public void setOrderAmount(Double orderAmount) { this.orderAmount = orderAmount; }
    
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    
    public String getRequetes() { return requetes; }
    public void setRequetes(String requetes) { this.requetes = requetes; }
    
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    
    public DeliveryAddress getAdresse() { return adresse; }
    public void setAdresse(DeliveryAddress adresse) { this.adresse = adresse; }
    
    public List<ProductOrderDetail> getProductList() { return productList; }
    public void setProductList(List<ProductOrderDetail> productList) { this.productList = productList; }
    
    public OrderUser getBuyer() { return buyer; }
    public void setBuyer(OrderUser buyer) { this.buyer = buyer; }
}
