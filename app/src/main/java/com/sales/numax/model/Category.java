package com.sales.numax.model;

import java.io.Serializable;

/**
 * Created by rahupathi on 10/28/2017.
 */

public class Category implements Serializable {

    String categoryname;
    String key;

    int isactive;
    int productexist;


    String userid;


    public Category() {
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getIsactive() {
        return isactive;
    }

    public void setIsactive(int isactive) {
        this.isactive = isactive;
    }

    @Override
    public String toString() {
        return categoryname;
    }

    public int getProductexist() {
        return productexist;
    }

    public void setProductexist(int productexist) {
        this.productexist = productexist;
    }

    public String getUserid() {
        return userid;
    }


}
