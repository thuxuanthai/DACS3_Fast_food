package com.example.server_fast_food_da3.EventBus;

public class UpdateDetailFood {
    private boolean success;

    public UpdateDetailFood(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
