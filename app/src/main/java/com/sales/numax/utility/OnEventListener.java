package com.sales.numax.utility;

/**
 * Created by rahupathi on 5/5/2018.
 */

public interface OnEventListener<T> {
    public void onSuccess(T object);
    public void onFailure(Exception e);
}