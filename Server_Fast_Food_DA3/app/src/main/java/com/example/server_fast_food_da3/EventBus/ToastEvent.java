package com.example.server_fast_food_da3.EventBus;

public class ToastEvent {
    private boolean isUpdate;
    private boolean isFromFoodlist;


    public ToastEvent(boolean isUpdate, boolean isFromFoodlist) {
        this.isUpdate = isUpdate;
        this.isFromFoodlist = isFromFoodlist;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public boolean isFromFoodlist() {
        return isFromFoodlist;
    }

    public void setFromFoodlist(boolean fromFoodlist) {
        isFromFoodlist = fromFoodlist;
    }
}
