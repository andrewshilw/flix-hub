package com.fablix.util;

import com.fablix.model.CartItem;

import java.util.LinkedHashMap;
import java.util.Map;

public class RedisSession {
    private String customerEmail;
    private Integer customerId;
    private String customerLoginTime;
    private String employeeEmail;
    private String employeeName;
    private String employeeLoginTime;
    private String lastMovieListUrl;
    private String cartMessage;
    private String cartMessageType;
    private Map<String, CartItem> cart = new LinkedHashMap<>();

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerLoginTime() {
        return customerLoginTime;
    }

    public void setCustomerLoginTime(String customerLoginTime) {
        this.customerLoginTime = customerLoginTime;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeLoginTime() {
        return employeeLoginTime;
    }

    public void setEmployeeLoginTime(String employeeLoginTime) {
        this.employeeLoginTime = employeeLoginTime;
    }

    public String getLastMovieListUrl() {
        return lastMovieListUrl;
    }

    public void setLastMovieListUrl(String lastMovieListUrl) {
        this.lastMovieListUrl = lastMovieListUrl;
    }

    public String getCartMessage() {
        return cartMessage;
    }

    public void setCartMessage(String cartMessage) {
        this.cartMessage = cartMessage;
    }

    public String getCartMessageType() {
        return cartMessageType;
    }

    public void setCartMessageType(String cartMessageType) {
        this.cartMessageType = cartMessageType;
    }

    public Map<String, CartItem> getCart() {
        if (cart == null) {
            cart = new LinkedHashMap<>();
        }
        return cart;
    }

    public void setCart(Map<String, CartItem> cart) {
        this.cart = cart == null ? new LinkedHashMap<>() : cart;
    }
}
