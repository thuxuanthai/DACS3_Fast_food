package com.example.fast_food_da3.ui.cart;


import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fast_food_da3.Common.Common;
import com.example.fast_food_da3.Database.CartDataSource;
import com.example.fast_food_da3.Database.CartDatabase;
import com.example.fast_food_da3.Database.CartItem;
import com.example.fast_food_da3.Database.LocalCartDataSource;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItem;

    public CartViewModel() {
        compositeDisposable  = new CompositeDisposable();
    }

    public void initCartDataSource(Context context)
    {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public void onStop()
    {
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItem() {
        if(mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartItem;
    }

    private void getAllCartItems() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    mutableLiveDataCartItem.setValue(cartItems);
                }, throwable -> {
                    mutableLiveDataCartItem.setValue(null);
                }));
    }
}
