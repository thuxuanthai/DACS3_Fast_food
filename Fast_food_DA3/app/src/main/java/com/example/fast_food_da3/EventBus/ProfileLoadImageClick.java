package com.example.fast_food_da3.EventBus;


public class ProfileLoadImageClick {
    private boolean success;

    public ProfileLoadImageClick(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
