package com.meru.customer_service.model;

import javax.persistence.Column;

public class Order {

    private int orderId;

    private int customerId;

    private int addressId;

    private int productId;

    private String orderDate;

    private String status;

    public Order(int orderId, int customerId, int addressId, int productId, String orderDate, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.addressId = addressId;
        this.productId = productId;
        this.orderDate = orderDate;
        this.status = status;
    }

    public Order() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", addressId=" + addressId +
                ", productId=" + productId +
                ", orderDate='" + orderDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
