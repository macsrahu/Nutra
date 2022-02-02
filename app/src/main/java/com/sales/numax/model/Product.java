package com.sales.numax.model;

import androidx.annotation.Keep;

import java.io.Serializable;
@Keep
public class Product implements Serializable {
    String categorykey;
   String key;
   String hsncode;
   double price;
   String productname;
   String url;
   String uom;
   int isactive;

   public Product(){}

    public String getKey() {
        return key;
    }

    public String getCategorykey() {
        return categorykey;
    }

    public void setCategorykey(String categorykey) {
        this.categorykey = categorykey;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHsncode() {
        return hsncode;
    }

    public void setHsncode(String hsncode) {
        this.hsncode = hsncode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public int getIsactive() {
        return isactive;
    }

    public void setIsactive(int isactive) {
        this.isactive = isactive;
    }
}
