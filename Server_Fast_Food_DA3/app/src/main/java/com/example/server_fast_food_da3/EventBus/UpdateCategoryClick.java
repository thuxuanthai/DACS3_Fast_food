package com.example.server_fast_food_da3.EventBus;

public class UpdateCategoryClick {
    private boolean success;

    public UpdateCategoryClick(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
