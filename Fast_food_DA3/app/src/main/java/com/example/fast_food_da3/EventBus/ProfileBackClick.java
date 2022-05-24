package com.example.fast_food_da3.EventBus;

public class ProfileBackClick {
    private boolean success;

    public ProfileBackClick(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
