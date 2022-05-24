package com.example.fast_food_da3.EventBus;

public class ProfileAddressClick {
    private boolean success;

    public ProfileAddressClick(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
