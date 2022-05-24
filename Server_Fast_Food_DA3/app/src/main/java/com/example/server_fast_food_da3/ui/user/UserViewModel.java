package com.example.server_fast_food_da3.ui.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.server_fast_food_da3.Callback.IUserCallbackListener;
import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.Model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel implements IUserCallbackListener {

        private MutableLiveData<List<UserModel>> userListMutable;
        private MutableLiveData<String> messageError = new MutableLiveData<>();
        private IUserCallbackListener userCallbackListener;

        public UserViewModel() {
            userCallbackListener = this;
        }

        public MutableLiveData<List<UserModel>> getUserListMultable() {
            if(userListMutable == null)
            {
                userListMutable = new MutableLiveData<>();
                messageError = new MutableLiveData<>();
                loadUser();
            }
            return userListMutable;
        }

        public void loadUser() {
            List<UserModel> tempList = new ArrayList<>();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot itemSnapShot:dataSnapshot.getChildren())
                    {
                        UserModel userModel = itemSnapShot.getValue(UserModel.class);
                        userModel.setMenu_id(itemSnapShot.getKey());
                        tempList.add(userModel);
                    }
                    userCallbackListener.onUserLoadSuccess(tempList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    userCallbackListener.onUserLoadFailed(databaseError.getMessage());
                }
            });
        }

        public MutableLiveData<String> getMessageError() {
            return messageError;
        }


        @Override
        public void onUserLoadSuccess(List<UserModel> userModelList) {
            userListMutable.setValue(userModelList);
        }

        @Override
        public void onUserLoadFailed(String message) {
            messageError.setValue(message);
        }
    }
//    private MutableLiveData<List<UserModel>> mutableLiveDataUserList;
//
//    public UserViewModel() {
//
//    }
//
//    public MutableLiveData<List<UserModel>> getMutableLiveDataUserList() {
//        if (mutableLiveDataUserList == null)
//            mutableLiveDataUserList = new MutableLiveData<>();
//        mutableLiveDataUserList.setValue(Common.userSelected.getUserModels());
//        return mutableLiveDataUserList;
//    }
//}

