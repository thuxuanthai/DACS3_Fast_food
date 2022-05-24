package com.example.fast_food_da3.EventBus;

public class ProfilePhoneClick {
    private boolean success;

    public ProfilePhoneClick(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
