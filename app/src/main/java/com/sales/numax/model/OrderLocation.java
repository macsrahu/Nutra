package com.sales.numax.model;

import java.io.Serializable;

public class OrderLocation implements Serializable {


    String orderdate;
    String orderno;
    String routekey;
    String salespersonkey;
    String getSalespersondetail;
    String customerkey;
    String customerdetail;
    String orderkey;
    double longitude;
    double latitude;

    public OrderLocation() {
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getRoutekey() {
        return routekey;
    }

    public void setRoutekey(String routekey) {
        this.routekey = routekey;
    }

    public String getSalespersonkey() {
        return salespersonkey;
    }

    public void setSalespersonkey(String salespersonkey) {
        this.salespersonkey = salespersonkey;
    }

    public String getGetSalespersondetail() {
        return getSalespersondetail;
    }

    public void setGetSalespersondetail(String getSalespersondetail) {
        this.getSalespersondetail = getSalespersondetail;
    }

    public String getCustomerkey() {
        return customerkey;
    }

    public void setCustomerkey(String customerkey) {
        this.customerkey = customerkey;
    }

    public String getCustomerdetail() {
        return customerdetail;
    }

    public void setCustomerdetail(String customerdetail) {
        this.customerdetail = customerdetail;
    }

    public String getOrderkey() {
        return orderkey;
    }

    public void setOrderkey(String orderkey) {
        this.orderkey = orderkey;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
