package com.example.server_fast_food_da3.ui.foodlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.server_fast_food_da3.Callback.IDetailFoodCalbackListener;
import com.example.server_fast_food_da3.Callback.IUserCallbackListener;
import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.Model.FoodModel;
import com.example.server_fast_food_da3.Model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FoodListViewModel extends ViewModel implements IDetailFoodCalbackListener {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IDetailFoodCalbackListener detailFoodCalbackListener;

    public FoodListViewModel( ) {
        detailFoodCalbackListener = this;
    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
        if(mutableLiveDataFoodList == null)
        {
            mutableLiveDataFoodList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadFood();
        }
        return mutableLiveDataFoodList;
    }

    public void loadFood() {
        List<FoodModel> tempList = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF).child(Common.categorySelected.getMenu_id()).child("foods");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapShot:dataSnapshot.getChildren())
                {
                    FoodModel foodModel = itemSnapShot.getValue(FoodModel.class);
                    foodModel.setId(itemSnapShot.getKey());
                    tempList.add(foodModel);
                }
                detailFoodCalbackListener.onDetailFoodLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                detailFoodCalbackListener.onDetailFoodLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }


    @Override
    public void onDetailFoodLoadSuccess(List<FoodModel> foodModelList) {
        mutableLiveDataFoodList.setValue(foodModelList);
    }

    @Override
    public void onDetailFoodLoadFailed(String message) {
        messageError.setValue(message);
    }

//    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
//        if (mutableLiveDataFoodList == null)
//            mutableLiveDataFoodList = new MutableLiveData<>();
//        mutableLiveDataFoodList.setValue(Common.categorySelected.getFoods());
//        return mutableLiveDataFoodList;
//    }
//
//    @Override
//    public void onUserLoadSuccess(List<FoodModel> foodModelsList) {
//
//    }
//
//    @Override
//    public void onUserLoadFailed(String message) {
//
//    }
}