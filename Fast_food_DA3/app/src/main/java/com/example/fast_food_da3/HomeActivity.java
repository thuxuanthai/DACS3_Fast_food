package com.example.fast_food_da3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.example.fast_food_da3.Common.Common;
import com.example.fast_food_da3.Database.CartDataSource;
import com.example.fast_food_da3.Database.CartDatabase;
import com.example.fast_food_da3.Database.LocalCartDataSource;
import com.example.fast_food_da3.EventBus.BestDealItemClick;
import com.example.fast_food_da3.EventBus.CategoryClick;
import com.example.fast_food_da3.EventBus.CounterCartEvent;
import com.example.fast_food_da3.EventBus.FoodItemClick;
import com.example.fast_food_da3.EventBus.HideFABCart;
import com.example.fast_food_da3.EventBus.MenuItemBack;
import com.example.fast_food_da3.EventBus.PopularCategoryClick;
import com.example.fast_food_da3.EventBus.ProfileBackClick;
import com.example.fast_food_da3.EventBus.ProfileLoadImageClick;
import com.example.fast_food_da3.EventBus.ProfilePhoneClick;
import com.example.fast_food_da3.EventBus.ProfileUserNameClick;
import com.example.fast_food_da3.Model.CategoryModel;
import com.example.fast_food_da3.Model.FoodModel;
import com.example.fast_food_da3.Model.UserModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private CartDataSource cartDataSource;
    private DrawerLayout drawer;
    NavigationView navigationView;

    FirebaseDatabase database;
    View headerView;
    TextView txt_user;
    CircleImageView img;


    private int menuClickId = -1;

    android.app.AlertDialog dialog;

    @BindView(R.id.fab)
    CounterFab fab;
    @Override
    protected void onResume()
    {
        super.onResume();
        countCartItem();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
         navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_list,R.id.nav_food_detail,
                R.id.nav_cart,R.id.nav_view_orders,R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront(); // Fixed
        hearder();

        countCartItem();
    }

    public void hearder(){
        headerView = navigationView.getHeaderView(0);
        txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        img =  headerView.findViewById(R.id.profile_img) ;

        database = FirebaseDatabase.getInstance();
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        txt_user.setText(userModel.getName());
                        if (userModel.getProfileImg()!=null) {
                            Glide.with(HomeActivity.this).load(userModel.getProfileImg()).into(img);
                        }else {
                            img.setImageResource(R.drawable.ic_account);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        txt_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new ProfileBackClick(true));
                menuClickId=-1;
                drawer.closeDrawers();
            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new ProfileBackClick(true));
                drawer.closeDrawers();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(true);
        drawer.closeDrawers();
        switch (item.getItemId()){
            case R.id.nav_home:
                if (item.getItemId() != menuClickId)
                navController.navigate(R.id.nav_home);
                break;
            case R.id.nav_menu:
                if (item.getItemId() != menuClickId)
                navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_cart:
                if (item.getItemId() != menuClickId)
                navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_view_orders:
                if (item.getItemId() != menuClickId)
                navController.navigate(R.id.nav_view_orders);
                break;
            case R.id.nav_profile:
                if (item.getItemId() != menuClickId)
                navController.navigate(R.id.nav_profile);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }
        menuClickId = item.getItemId();
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("Are you sure you want to sing out?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent( HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home, menu);
//        return true;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    //event bus

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_food_list);
//            Toast.makeText(this, "You Clicked to:"+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_food_detail);
//            Toast.makeText(this, "thành công", Toast.LENGTH_SHORT).show();
        }
    }

//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onProfileUserNamelick(ProfileUserNameClick event)
//    {
//        if (event.isSuccess())
//        {
//            navController.navigate(R.id.nav_username_profile);
//        }
//    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onProfilePhoneClick(ProfilePhoneClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_phone_profile);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onProfileClick(ProfileBackClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_profile);
            menuClickId=-1;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onProfileLoadImageClick(ProfileLoadImageClick event)
    {
        if (event.isSuccess())
        {
            hearder();
//            img.setImageResource(R.drawable.ic_account);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABCart(HideFABCart event)
    {
        if (event.isHidden())
        {
            fab.hide();
        }
        else
            fab.show();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event)
    {
        if (event.isSuccess())
        {
            countCartItem();
        }
    }

    //Video 19
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if (event.getBestDealModel() != null)
        {
            dialog.show();

            com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){

                            if(dataSnapshot.exists())
                            {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());
                                //Load Food
                                com.google.firebase.database.FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    for(DataSnapshot itemSnapShot : dataSnapshot.getChildren())
                                                    {
                                                        Common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapShot.getKey());
                                                    }

                                                    navController.navigate(R.id.nav_food_detail );
                                                }
                                                else
                                                {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exists!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError){
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                            }

                                        });
                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exists!", Toast.LENGTH_SHORT).show();

                            }


                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError){

                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null)
        {
            dialog.show();

            com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){

                            if(dataSnapshot.exists())
                            {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                //Load Food
                                com.google.firebase.database.FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    for(DataSnapshot itemSnapShot : dataSnapshot.getChildren())
                                                    {
                                                        Common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapShot.getKey());
                                                    }

                                                    navController.navigate(R.id.nav_food_detail );
                                                }
                                                else
                                                {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exists!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exists!", Toast.LENGTH_SHORT).show();

                            }


                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError){

                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty"))
                        {
                            Toast.makeText(HomeActivity.this, "[COUNT CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else
                            fab.setCount(0);
                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event)
    {
        menuClickId = -1;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }
}