package com.example.server_fast_food_da3.Callback;

import com.example.server_fast_food_da3.Model.ShippingOrderModel;

public interface ISingleShippingOrderCallbackListener {
    void onSingleShippingOrderLoadSuccess(ShippingOrderModel shippingOrderModel);
}
