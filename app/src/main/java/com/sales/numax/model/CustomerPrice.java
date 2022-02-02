package com.sales.numax.model;

import androidx.annotation.Keep;

import java.io.Serializable;
@Keep
public class CustomerPrice implements Serializable {
    String key;
    String productkey;
    String price;

    public CustomerPrice(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getProductkey() {
        return productkey;
    }

    public void setProductkey(String productkey) {
        this.productkey = productkey;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
