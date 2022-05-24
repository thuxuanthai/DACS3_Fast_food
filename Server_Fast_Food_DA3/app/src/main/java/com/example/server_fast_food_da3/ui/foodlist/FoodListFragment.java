package com.example.server_fast_food_da3.ui.foodlist;

import android.app.Activity;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.server_fast_food_da3.Adapter.MyFoodListAdapter;
import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.Common.MySwipeHelper;
import com.example.server_fast_food_da3.EventBus.AddonSizeEditEvent;
import com.example.server_fast_food_da3.EventBus.ChangeMenuClick;
import com.example.server_fast_food_da3.EventBus.MenuItemBack;
import com.example.server_fast_food_da3.EventBus.ToastEvent;
import com.example.server_fast_food_da3.Model.FoodModel;
import com.example.server_fast_food_da3.R;
import com.example.server_fast_food_da3.SizeAddonEditActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {

    //Image upload
    private static final int PICK_IMAGE_REQUEST = 1234;
    private static final int PICK_INSERT_IMAGE_REQUEST = 56789;
    private ImageView img_food,img_insert_food;
    private Integer i = 0;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;
    FirebaseAuth auth;
    FirebaseDatabase database;

    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList = new ArrayList<>();
    private DatabaseReference categoryRef;


    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;
    @BindView(R.id.txt_empty_food_list)
    TextView txt_empty_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri = null;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu, menu);

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
            EditText ed = (EditText)searchView.findViewById(R.id.search_src_text);
            //Clear Text
            ed.setText("");
            //Clear Query
            searchView.setQuery("",false);
            //Collapse the ation view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to origin
            foodListViewModel.getMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());
        });
    }

    private void startSearchFood(String query) {
        List<FoodModel> resultFood = new ArrayList<>();
        for(int i=0;i<Common.categorySelected.getFoods().size(); i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(query.toLowerCase()))
            {
                foodModel.setPositionInList(i);
                resultFood.add(foodModel);

            }
        }


        foodListViewModel.getMutableLiveDataFoodList().setValue(resultFood); // setting search result

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);

        unbinder = ButterKnife.bind(this, root);
        initView();
        foodListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {
            if(foodModels.size() > 0 || !foodModels.isEmpty()) {
                recycler_food_list.setVisibility(View.VISIBLE);
                txt_empty_food_list.setVisibility(View.GONE);
                foodModelList = foodModels;
                adapter = new MyFoodListAdapter(getContext(), foodModels);
                recycler_food_list.setAdapter(adapter);
                recycler_food_list.setLayoutAnimation(layoutAnimationController);
                i = adapter.getItemCount();
            }else{
                recycler_food_list.setVisibility(View.GONE);
                txt_empty_food_list.setVisibility(View.VISIBLE);
            }
        });
        return root;
    }

    private void initView() {

        setHasOptionsMenu(true); // this enable menu icon in fragment

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        categoryRef= FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());

        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        // Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;


        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_food_list, width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                            builder.setTitle("DELETE")
                                    .setMessage("Do you want to delete this food?")
                                    .setNegativeButton("CANCEL", ((dialogInterface, which) -> dialogInterface.dismiss()))
                                    .setPositiveButton("DELETE", ((dialogInterface, which) -> {
                                        FoodModel foodModel = foodModelList.get(pos);
                                        if (!TextUtils.isEmpty(foodModel.getId()))
                                        {
                                            FirebaseDatabase.getInstance()
                                                    .getReference(Common.CATEGORY_REF)
                                                    .child(Common.categorySelected.getMenu_id())
                                                    .child("foods")
                                                    .child(foodModel.getId())
                                                    .removeValue()
                                                    .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            adapter.removeItem(pos);
                                                            adapter.notifyItemRemoved(pos);
                                                            foodListViewModel.loadFood();
                                                            Toast.makeText(getContext(), "Order has been deleted successfully!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                        else
                                        {
                                            Toast.makeText(getContext(), "Order number must not be null or empty!", Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                            android.app.AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();

                        }));

                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                            FoodModel foodModel = adapter.getItemAtPosition(pos); // Get item in adapter
                            if(foodModel.getPositionInList() == -1) // if == +1 default, do nothing
                                showUpdateDialog(pos, foodModel);
                            else
                                showUpdateDialog(foodModel.getPositionInList(), foodModel);

                        }));

                buf.add(new MyButton(getContext(), "Size", 30, 0, Color.parseColor("#12005e"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pos); // Get item in adapter
                            if(foodModel.getPositionInList() == -1) // if == +1 default, do nothing
                                Common.selectedFood = foodModelList.get(pos);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            //Change pos
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, foodModel.getPositionInList()));
                        }));

                buf.add(new MySwipeHelper.MyButton(getContext(), "Addon", 30, 0, Color.parseColor("#336699"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pos); // Get item in adapter
                            if(foodModel.getPositionInList() == -1) // if == +1 default, do nothing
                                Common.selectedFood = foodModelList.get(pos);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            //Change pos
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true, pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true, foodModel.getPositionInList()));

                        }));
            }
        };


    }

    private void showUpdateDialog(int pos, FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);
        EditText edt_food_name = (EditText) itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText) itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText) itemView.findViewById(R.id.edt_food_description);

        img_food = (ImageView) itemView.findViewById(R.id.img_food_image);

        //Set data
        edt_food_name.setText(new StringBuilder("")
                .append(foodModel.getName()));

        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));

        edt_food_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);


        //Set Event
        img_food.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.setPositiveButton("UPDATE", (dialogInterface, which) -> {

            FoodModel updateFood = foodModel;
            updateFood.setName(edt_food_name.getText().toString());
            updateFood.setDescription(edt_food_description.getText().toString());
            updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                    Long.parseLong(edt_food_price.getText().toString()));

            if(imageUri != null)
            {
                // HAve image
                //In this we will use fire base storage to upload image
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString(); //unique_name == image name
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateFood.setImage(uri.toString());
                        Common.categorySelected.getFoods().set(pos, updateFood);
                        updateFood(Common.categorySelected.getFoods(), false);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }
            else
            {
                Common.categorySelected.getFoods().set(pos, updateFood);
                updateFood(Common.categorySelected.getFoods(), false);
            }

        });

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_insert)
        {
            showInsertDialog();

            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }

    }

    private void showInsertDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Insert");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_insert_detail_food, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_price = (EditText) itemView.findViewById(R.id.edt_price);
        EditText edt_description = (EditText) itemView.findViewById(R.id.edt_description);
        img_insert_food = (ImageView) itemView.findViewById(R.id.img_i_food);

        //Set Event
        img_insert_food.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Picture"), PICK_INSERT_IMAGE_REQUEST);
        });


        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());

        builder.setPositiveButton("Insert", (dialogInterface, which) -> {
            if(imageUri != null && edt_name.getText().toString() != null ) {
                //In this we will use fire base storage to upload image
//                if(foodModelList.size() > 0 || !foodModelList.isEmpty()) {
//                    i = adapter.getItemCount();
//                }
                Integer dem = (Integer) i + 1;
                String x = null;
                if(dem<10){
                    x = "_0" + dem;
                }
                else {
                    x = "_" + dem;
                }

                FoodModel foodModel = new FoodModel();
                String[] test = TextUtils.split(Common.categorySelected.getName().toLowerCase()," +");
                foodModel.setId(test[0]+x);
                foodModel.setName(edt_name.getText().toString());
                foodModel.setPrice(Long.parseLong(edt_price.getText().toString()));
                foodModel.setDescription(edt_description.getText().toString());
                foodModel.setImage(imageUri.toString());

                categoryRef.child(Common.categorySelected.getMenu_id()).child("foods").child(i.toString()).setValue(foodModel).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialogInterface.dismiss();
                    }
                });

                dialog.setMessage("Uploading...");
                dialog.show();
                Map<String, Object> updateData = new HashMap<>();
                String unique_name = UUID.randomUUID().toString(); //unique_name == image name
                StorageReference imageFolder = storageReference.child("image/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateData.put("image", uri.toString());
                        updateCategory(updateData);
                        Toast.makeText(getContext(), "Insert Success", Toast.LENGTH_SHORT).show();
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });

            }
            else {
                Toast.makeText(getContext(), "Image or Name isEmpty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void updateCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .child("foods").child(i.toString())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    foodListViewModel.loadFood();
                });
    }



    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null  &&  data.getData() != null )
            {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
        if(requestCode == PICK_INSERT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null  &&  data.getData() != null )
            {
                imageUri = data.getData();
                img_insert_food.setImageURI(imageUri);
            }
        }
    }


    private void updateFood(List<FoodModel> foods, boolean isDelete) {

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("foods", foods);

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        foodListViewModel.getMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky(new ToastEvent(!isDelete, true));

                    }
                });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}