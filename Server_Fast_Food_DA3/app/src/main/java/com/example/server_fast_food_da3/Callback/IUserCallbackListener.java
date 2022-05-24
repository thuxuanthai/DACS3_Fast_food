package com.example.server_fast_food_da3.Callback;

import com.example.server_fast_food_da3.Model.UserModel;

import java.util.List;

public interface IUserCallbackListener {
        void onUserLoadSuccess(List<UserModel> userModelList);
        void onUserLoadFailed(String message);
    }
