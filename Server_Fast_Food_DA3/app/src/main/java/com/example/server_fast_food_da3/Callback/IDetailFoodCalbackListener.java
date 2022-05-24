package com.example.server_fast_food_da3.Callback;

import com.example.server_fast_food_da3.Model.FoodModel;
import com.example.server_fast_food_da3.Model.UserModel;

import java.util.List;

public interface IDetailFoodCalbackListener {
    void onDetailFoodLoadSuccess(List<FoodModel> foodModelsList);
    void onDetailFoodLoadFailed(String message);
}
