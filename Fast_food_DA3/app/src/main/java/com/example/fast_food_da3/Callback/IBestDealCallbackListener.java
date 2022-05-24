package com.example.fast_food_da3.Callback;


import com.example.fast_food_da3.Model.BestDealModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> BestDealModels);
    void onBestDealLoadFailed(String message);
}
