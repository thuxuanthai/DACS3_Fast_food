package com.example.fast_food_da3.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fast_food_da3.EventBus.MenuItemBack;
import com.example.fast_food_da3.EventBus.ProfileBackClick;
import com.example.fast_food_da3.EventBus.ProfileLoadImageClick;
import com.example.fast_food_da3.EventBus.ProfilePhoneClick;
import com.example.fast_food_da3.HomeActivity;
import com.example.fast_food_da3.Model.UserModel;
import com.example.fast_food_da3.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {
    ImageView profileImg,edit_img;
    TextView name,nameUser,number,address;
    Uri selectedImageUri;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;

    EditText username,addressuser,phonenumber;
    String userName="",nameU="",addressUser="",addressU="", phoneNumber="",phoneN="";
    Button btn_saveName,btn_saveAddress,btn_savePhone;

    //Update Username
    void onUpdateNameClick(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update Name");


        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_username_profile, null);
        database =FirebaseDatabase.getInstance();    // đọc ghi csdl

        username = view.findViewById(R.id.profile_name);
        btn_saveName= view.findViewById(R.id.btn_save);
        btn_saveName.setEnabled(false);

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        userName = userModel.getName();
                        username.setText(userModel.getName());

                        username.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if(username.getText().toString().equals("") || username.getText().toString().equals(userName)) {
                                    btn_saveName.setEnabled(false);
                                }else {
                                    btn_saveName.setEnabled(true);
                                }
                            }
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });

        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        btn_saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load_username();
                dialog.dismiss();
            }

        });
    }
    public void load_username() {
        nameU = username.getText().toString().trim();
        if (nameU.equals(userName)) {
            btn_saveName.setEnabled(false);
            Toast.makeText(getContext(), "Upload Fail!", Toast.LENGTH_SHORT).show();
        } else {
            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                    .child("name").setValue(nameU);

            EventBus.getDefault().postSticky(new ProfileBackClick(true));
            EventBus.getDefault().postSticky(new ProfileLoadImageClick(true));

            btn_saveName.setEnabled(false);
            Toast.makeText(getContext(), "Upload successfully!", Toast.LENGTH_SHORT).show();
        }
    }


    //Updtae Address
    void onUpdateAddressClick(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update Address");


        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_address_profile, null);
        database =FirebaseDatabase.getInstance();    // đọc ghi csdl

        addressuser = view.findViewById(R.id.profile_address);
        btn_saveAddress= view.findViewById(R.id.btn_save);
        btn_saveAddress.setEnabled(false);

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        addressUser = userModel.getAddress();
                        addressuser.setText(userModel.getAddress());

                        addressuser.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if(addressuser.getText().toString().equals("") || addressuser.getText().toString().equals(addressUser)) {
                                    btn_saveAddress.setEnabled(false);
                                }else {
                                    btn_saveAddress.setEnabled(true);
                                }
                            }
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });

        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        btn_saveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load_address();
                dialog.dismiss();

            }
        });
    }
    public void load_address() {
        addressU = addressuser.getText().toString().trim();
        if (addressU.equals(addressUser)) {
            btn_saveAddress.setEnabled(false);
            Toast.makeText(getContext(), "Upload Fail!", Toast.LENGTH_SHORT).show();
        } else {
            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                    .child("address").setValue(addressU);

            EventBus.getDefault().postSticky(new ProfileBackClick(true));
//            EventBus.getDefault().postSticky(new ProfileLoadImageClick(true));

            btn_saveAddress.setEnabled(false);
            Toast.makeText(getContext(), "Upload successfully!", Toast.LENGTH_SHORT).show();
        }
    }

//    private Unbinder unbinder;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
//        unbinder = ButterKnife.bind(this, root);

        auth = FirebaseAuth.getInstance();      //Truy cập thông tin người dùng
        database =FirebaseDatabase.getInstance();    // đọc ghi csdl
        storage = FirebaseStorage.getInstance();        //Thiết lập bộ nhớ đám mây

        profileImg = root.findViewById(R.id.profile_img);
        edit_img =root.findViewById(R.id.edit_img);
        name = root.findViewById(R.id.profile_name);
        nameUser = root.findViewById(R.id.profile_name_User);
        number = root.findViewById(R.id.profile_number);
        address =root.findViewById(R.id.profile_address);


        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel.getProfileImg()!=null){
                            Glide.with(getContext()).load(userModel.getProfileImg()).into(profileImg);
                        }else{
                            profileImg.setImageResource(R.drawable.ic_account);
                        }
                        name.setText(userModel.getName());
                        nameUser.setText(userModel.getName());
                        number.setText(userModel.getPhone());
                        address.setText(userModel.getAddress());
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });


        edit_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                EventBus.getDefault().postSticky(new ProfileImageClick(true));
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                EventBus.getDefault().postSticky(new ProfileUserNameClick(true));
                onUpdateNameClick();
            }
        });

//        number.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onUpdatePhoneClick();
//            }
//        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateAddressClick();
            }
        });
        return root;
    }

    private void updateUserProfile() {
        String userSdt = number.getText().toString().trim();
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("phone").setValue(userSdt);

        String userAddress = address.getText().toString().trim();
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("address").setValue(userAddress);

        String nameU = name.getText().toString().trim();
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("name").setValue(nameU);

        startActivity(new Intent(getContext(), HomeActivity.class));
        Toast.makeText(getContext(),"Upload successfully!",Toast.LENGTH_SHORT).show();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 33 && resultCode==RESULT_OK){
            selectedImageUri=data.getData();
            if (null != selectedImageUri) {
                final ProgressDialog mDialog = new ProgressDialog(getActivity());
                mDialog.setMessage("Uploading...");
                mDialog.show();

                profileImg.setImageURI(selectedImageUri);
                StorageReference reference = storage.getReference().child("profile_picture")
                        .child(auth.getUid());

                reference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(auth.getUid())
                                        .child("profileImg").setValue(uri.toString());
                                EventBus.getDefault().postSticky(new ProfileLoadImageClick(true));

                            }
                        });
                    }
                });

//                Toast.makeText(getContext(), "Image Selected", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getContext(),"Image Not Selected",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
