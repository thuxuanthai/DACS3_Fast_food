package com.example.fast_food_da3.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "Cart", primaryKeys = {"uid", "foodId", "foodAddon", "foodSize"})
public class CartItem {
    @NonNull
    @ColumnInfo(name = "foodId")
    public  String foodId;

    @ColumnInfo(name = "foodName")
    public  String foodName;

    @ColumnInfo(name = "foodImage")
    public  String foodImage;

    @ColumnInfo(name = "foodPrice")
    public  Double foodPrice;

    @ColumnInfo(name = "foodQuantity")
    public  int  foodQuantity;

    @ColumnInfo(name = "userPhone")
    public  String userPhone;

    @ColumnInfo(name = "foodExtraPrice")
    public  Double  foodExtraPrice;

    @NonNull
    @ColumnInfo(name = "foodAddon")
    public  String foodAddon;

    @NonNull
    @ColumnInfo(name = "foodSize")
    public  String foodSize;

    @NonNull
    @ColumnInfo(name = "uid")
    public  String uid;

    @NonNull
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public int getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(int foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Double getFoodExtraPrice() {
        return foodExtraPrice;
    }

    public void setFoodExtraPrice(Double foodExtraPrice) {
        this.foodExtraPrice = foodExtraPrice;
    }

    public String getFoodAddon() {
        return foodAddon;
    }

    public void setFoodAddon(String foodAddon) {
        this.foodAddon = foodAddon;
    }

    public String getFoodSize() {
        return foodSize;
    }

    public void setFoodSize(String foodSize) {
        this.foodSize = foodSize;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof CartItem))
            return false;
        CartItem cartItem = (CartItem)obj;

        return cartItem.getFoodId().equals(this.foodId) &&
                cartItem.getFoodAddon().equals(this.foodAddon) &&
                cartItem.getFoodSize().equals(this.foodSize);
    }
}
