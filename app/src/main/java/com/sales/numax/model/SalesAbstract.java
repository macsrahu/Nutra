package com.sales.numax.model;

import androidx.annotation.Keep;

@Keep
public class SalesAbstract {

    String salesdate;
    String productname;
    String totalsales;

    public  SalesAbstract(){}

    public String getSalesdate() {
        return salesdate;
    }

    public void setSalesdate(String salesdate) {
        this.salesdate = salesdate;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getTotalsales() {
        return totalsales;
    }

    public void setTotalsales(String totalsales) {
        this.totalsales = totalsales;
    }
}
