package com.example.server_fast_food_da3.ui.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.server_fast_food_da3.Adapter.MyUserListAdapter;
import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.Common.MySwipeHelper;
import com.example.server_fast_food_da3.EventBus.MenuItemBack;
import com.example.server_fast_food_da3.EventBus.UserClick;
import com.example.server_fast_food_da3.Model.UserModel;
import com.example.server_fast_food_da3.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class UserFragment extends Fragment {

    private List<AuthUI.IdpConfig> providers;
    private static int APP_REQUEST_CODE = 7170;  //any number

    //Image upload
    private static final int PICK_IMAGE_REQUEST = 1234;
    private static final int PICK_INSERT_IMAGE_REQUEST = 5678;
    private ImageView img_food;

    EditText edt_name , edt_address_detail;
    TextView edt_phone;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;
    View root;
    private DatabaseReference userRef;
    private FirebaseAuth firebaseAuth;
    ImageView img_insert_food;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser user;


    private UserViewModel userViewModel;

    private List<UserModel> userModelList;
    private FirebaseAuth.AuthStateListener listener;
    androidx.appcompat.app.AlertDialog.Builder builderInsert;
    androidx.appcompat.app.AlertDialog dialogm;


    Unbinder unbinder;
    @BindView(R.id.recycler_user)
    RecyclerView recycler_user;
    @BindView(R.id.txt_empty_user)
    TextView txt_empty_user;
//    @BindView(R.id.btn_insert)
//    TextView btn_insert;


    LayoutAnimationController layoutAnimationController;
    MyUserListAdapter adapter;
    private Uri imageUri = null;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        userViewModel =
                ViewModelProviders.of(this).get(UserViewModel.class);
         root = inflater.inflate(R.layout.fragment_user, container, false);
        unbinder = ButterKnife.bind(this, root);
        initView();

//        btn_insert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                check();
//            }
//        });

        userViewModel.getUserListMultable().observe(getViewLifecycleOwner(), userModels -> {
            if(userModels.size() > 0 || !userModels.isEmpty()) {
                recycler_user.setVisibility(View.VISIBLE);
                txt_empty_user.setVisibility(View.GONE);
                userModelList= userModels;
                adapter = new MyUserListAdapter(getContext(),userModels);
                recycler_user.setAdapter(adapter);
                recycler_user.setLayoutAnimation(layoutAnimationController);

                TextView txt_status = (TextView) root.findViewById(R.id.txt_user_filter);
                //Set data
                txt_status.setText(new StringBuilder("User (")
                    .append(adapter.getItemCount())
                    .append(")"));
            }
            else {
                recycler_user.setVisibility(View.GONE);
                txt_empty_user.setVisibility(View.VISIBLE);
            }
        });
        return root;
    }

    private void initView() {

        setHasOptionsMenu(true); // this enable menu icon in fragment

//        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recycler_user.setHasFixedSize(true);
        recycler_user.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        del();

    }

    public void del(){
        // Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_user, width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("DELETE")
                                    .setMessage("Do you want to delete this food?")
                                    .setNegativeButton("CANCEL", ((dialogInterface, which) -> dialogInterface.dismiss()))
                                    .setPositiveButton("DELETE", ((dialogInterface, which) -> {
                                        UserModel userModel = userModelList.get(pos);
                                        if (!TextUtils.isEmpty(userModel.getUid()))
                                        {
                                            FirebaseDatabase.getInstance()
                                                    .getReference(Common.USER_REFERENCES)
                                                    .child(userModel.getUid())
                                                    .removeValue()
                                                    .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            adapter.removeItem(pos);
                                                            adapter.notifyItemRemoved(pos);
                                                            goToHomeActivity();
                                                            Toast.makeText(getContext(), "Order has been deleted successfully!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                        else
                                        {
                                            Toast.makeText(getContext(), "Order number must not be null or empty!", Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();

                        }));
            }
        };
    }


    private void check(){
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        userRef= FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        phoneLogin();

    }

    private void checkUserFromFirebase(FirebaseUser user) {

        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
//                           UserModel userModel = snapshot.getValue(UserModel.class);
                            goToHomeActivity();
                            Toast.makeText(getContext(), "ton tai", Toast.LENGTH_SHORT).show();

                        }else {
                            showRegisterDialog(user);
                        }
                        dialog.dismiss();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        builderInsert = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//        builder.setTitle("Register");
//        builder.setMessage("Please Fill Information");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_insert_user,null);
        edt_name= view.findViewById(R.id.edtNameU);
        edt_address_detail = view.findViewById(R.id.edtAddressU);
        edt_phone= view.findViewById(R.id.edtPhoneU);
        img_insert_food = view.findViewById(R.id.img_insert_food);

        auth = FirebaseAuth.getInstance();      //Truy cập thông tin người dùng
        database =FirebaseDatabase.getInstance();    // đọc ghi csdl
        storage = FirebaseStorage.getInstance();        //Thiết lập bộ nhớ đám mây


        //Set Event
        img_insert_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_INSERT_IMAGE_REQUEST);
        });


        //set
        edt_phone.setText(user.getPhoneNumber());

        builderInsert.setView(view);
        builderInsert.setNegativeButton("CANCEL", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        });

        builderInsert.setPositiveButton("Insert", (dialogInterface, which) -> {
            if(imageUri != null && edt_name.getText().toString()!= null
                    && edt_address_detail.getText().toString()!= null) {

                UserModel userModel = new UserModel();
                userModel.setUid(user.getUid());
                userModel.setName(edt_name.getText().toString());
                userModel.setAddress(edt_address_detail.getText().toString());
                userModel.setPhone(edt_phone.getText().toString());
                userModel.setProfileImg(imageUri.toString());

                userRef.child(user.getUid()).setValue(userModel).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialogInterface.dismiss();
                    }
                });

                final ProgressDialog mDialog = new ProgressDialog(getActivity());
                mDialog.setMessage("Uploading...");
                mDialog.show();

                StorageReference reference = storage.getReference().child("profile_picture")
                        .child(user.getUid());

                reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(user.getUid())
                                        .child("profileImg").setValue(uri.toString());
                                goToHomeActivity();
                                Toast.makeText(getContext(), "Insert Success", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

//                        dialog.setMessage("Uploading...");
//                        dialog.show();
//                        Map<String, Object> updateData = new HashMap<>();
//                        String unique_name = UUID.randomUUID().toString(); //unique_name == image name
//                        StorageReference imageFolder = storageReference.child("profileImg/"+unique_name);
//
//                        imageFolder.putFile(imageUri)
//                                .addOnFailureListener(e -> {
//                                    dialog.dismiss();
//                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                }).addOnCompleteListener(task -> {
//                            dialog.dismiss();
//                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
//                                updateData.put("profileImg", uri.toString());
//                                updateCategory(updateData,user);
//                                Toast.makeText(getContext(), "Insert Success", Toast.LENGTH_SHORT).show();
//
//                            });
//                        }).addOnProgressListener(taskSnapshot -> {
//                            double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                            dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
//                        });


            }
            else {
                Toast.makeText(getContext(), "Please Your Name or Address or Image isEmpty", Toast.LENGTH_SHORT).show();

            }
        });

        dialogm = builderInsert.create();
        dialogm.show();

    }

    private void updateCategory(Map<String, Object> updateData, FirebaseUser user) {
        FirebaseDatabase.getInstance()
                .getReference(Common.USER_REFERENCES)
                .child(user.getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    goToHomeActivity();
                });
    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),APP_REQUEST_CODE);
    }

    private void goToHomeActivity() {
        EventBus.getDefault().postSticky(new UserClick(true));
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.insert_user, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Clear text when click to Clear button on Search View
        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(v -> {
            EditText ed = (EditText) searchView.findViewById(R.id.search_src_text);
            //Clear Text
            ed.setText("");
            //Clear Query
            searchView.setQuery("", false);
            //Collapse the ation view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to origin
            userViewModel.loadUser();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_insert) {
            check();
            return true;
        }
         else
        {
            return super.onOptionsItemSelected(item);
        }

    }

    private void startSearchFood(String query) {
        List<UserModel> resultUser = new ArrayList<>();
        for(int i=0;i<userModelList.size(); i++) {
            UserModel userModel = userModelList.get(i);
            if (userModel.getName().toLowerCase().contains(query))
            {
//                userModel.setPositionInList(i);
                resultUser.add(userModel);

            }
        }


        userViewModel.getUserListMultable().setValue(resultUser); // setting search result

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                checkUserFromFirebase(user);

            } else {
                Toast.makeText(getContext(), "Failed to Sign in!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PICK_INSERT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_insert_food.setImageURI(imageUri);
            }

        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}