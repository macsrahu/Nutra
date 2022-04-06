package com.sales.numax.model;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class OrderMain implements Serializable {

    String key;
    String orderno;
    String orderdate;
    String dealerkey;
    String dealer;
    String address;
    String routekey;
    String userid;

    double orderamount;
    int iscancelled;
    int isdelivered;

    long orderdatestamp;

    public OrderMain() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public String getDealerkey() {
        return dealerkey;
    }

    public void setDealerkey(String dealerkey) {
        this.dealerkey = dealerkey;
    }

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getOrderamount() {
        return orderamount;
    }

    public void setOrderamount(double orderamount) {
        this.orderamount = orderamount;
    }

    public int getIscancelled() {
        return iscancelled;
    }

    public void setIscancelled(int iscancelled) {
        this.iscancelled = iscancelled;
    }

    public int getIsdelivered() {
        return isdelivered;
    }

    public void setIsdelivered(int isdelivered) {
        this.isdelivered = isdelivered;
    }

    public long getOrderdatestamp() {
        return orderdatestamp;
    }

    public void setOrderdatestamp(long orderdatestamp) {
        this.orderdatestamp = orderdatestamp;
    }

    public String getRoutekey() {
        return routekey;
    }

    public void setRoutekey(String routekey) {
        this.routekey = routekey;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
