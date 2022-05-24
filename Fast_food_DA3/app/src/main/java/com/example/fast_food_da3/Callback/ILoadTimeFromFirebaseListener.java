package com.example.fast_food_da3.Callback;

import com.example.fast_food_da3.Model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
