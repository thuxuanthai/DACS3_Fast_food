package com.example.server_fast_food_da3.ui.category;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.server_fast_food_da3.Adapter.MyCategoriesAdapter;
import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.Common.MySwipeHelper;
import com.example.server_fast_food_da3.EventBus.ChangeMenuClick;
import com.example.server_fast_food_da3.EventBus.MenuItemBack;
import com.example.server_fast_food_da3.EventBus.ToastEvent;
import com.example.server_fast_food_da3.Model.CategoryModel;
import com.example.server_fast_food_da3.R;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class CategoryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private static final int PICK_INSERT_IMAGE_REQUEST = 5678;
    private CategoryViewModel categoryViewModel;
    private DatabaseReference categoryRef;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    List<CategoryModel> categoryModelsList;
    ImageView img_insert_food,img_category;
    private Uri imageUri = null;
    String x = null;
    Integer i = 0;

    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();
        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        categoryViewModel.getCategoryListMultable().observe(getViewLifecycleOwner(), categoryModelList -> {
            dialog.dismiss();
            categoryModelsList = categoryModelList;
            adapter = new MyCategoriesAdapter(getContext(), categoryModelList);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        categoryRef= FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        //dialog.show(); Removing this to fix loading show when resume fragment
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());


        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_menu, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("DELETE")
                                    .setMessage("Do you want to delete this food?")
                                    .setNegativeButton("CANCEL", ((dialogInterface, which) -> dialogInterface.dismiss()))
                                    .setPositiveButton("DELETE", ((dialogInterface, which) -> {
                                        CategoryModel categoryM = categoryModelsList.get(pos);
                                        if (!TextUtils.isEmpty(categoryM.getMenu_id()))
                                        {
                                            FirebaseDatabase.getInstance()
                                                    .getReference(Common.CATEGORY_REF)
                                                    .child(categoryM.getMenu_id())
                                                    .removeValue()
                                                    .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            adapter.removeItem(pos);
                                                            adapter.notifyItemRemoved(pos);
//                                                            categoryViewModel.loadCategories();
                                                            Toast.makeText(getContext(), "Oroder has been deleted successfully!", Toast.LENGTH_SHORT).show();
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

                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"), pos -> {

                    Common.categorySelected = categoryModelsList.get(pos);

                    showUpdateDialog();

                }));
            }
        };


    }


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
            categoryViewModel.loadCategories();
        });
    }

    private void startSearchFood(String query) {
        List<CategoryModel> resultFood = new ArrayList<>();
        for(int i=0;i<categoryModelsList.size(); i++) {
            CategoryModel categoryModel = categoryModelsList.get(i);
            if (categoryModel.getName().toLowerCase().contains(query.toLowerCase()))
            {
                categoryModel.setPositionInList(i);
                resultFood.add(categoryModel);

            }
        }


        categoryViewModel.getCategoryListMultable().setValue(resultFood); // setting search result

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

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_insert_food, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        img_insert_food = (ImageView) itemView.findViewById(R.id.img_insert_food);
//        img_camera  = (ImageView) itemView.findViewById(R.id.img_camera);

//        //Set data
//        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
//        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        //Set Event
        img_insert_food.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_INSERT_IMAGE_REQUEST);
        });

        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());

            builder.setPositiveButton("Insert", (dialogInterface, which) -> {
            if(imageUri != null && edt_name.getText().toString()!= null) {
                //In this we will use fire base storage to upload image

//            else if (TextUtils.isEmpty(img_insert_food..toString())){
//                Toast.makeText(getContext(), "Please Your Address", Toast.LENGTH_SHORT).show();
//                return;
//            }


                if(categoryModelsList != null) {
                    i = adapter.getItemCount();
                }else i=0;
                Integer dem = (Integer) i + 1;

                if(dem<10){
                    x = "menu_0" + dem;
                }
                else {
                    x = "menu_" + dem;
                }

                CategoryModel categoryModel = new CategoryModel();
                categoryModel.setName(edt_name.getText().toString());
                categoryModel.setImage(imageUri.toString());

                categoryRef.child(x).setValue(categoryModel).addOnCompleteListener(task -> {
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
                        updateinsertCategory(updateData);
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
    private void updateinsertCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(x)
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                });
    }



    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        //Set data
        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        //Set Event
        img_category.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.setPositiveButton("UPDATE", (dialogInterface, which) -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", edt_category_name.getText().toString());

            if(imageUri != null)
            {
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
                        updateData.put("image", uri.toString());
                        updateCategory(updateData);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }
            else
            {
                updateCategory(updateData);
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
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(true, false));

                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null  &&  data.getData() != null )
            {
                imageUri = data.getData();
                img_category.setImageURI(imageUri);
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
    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}