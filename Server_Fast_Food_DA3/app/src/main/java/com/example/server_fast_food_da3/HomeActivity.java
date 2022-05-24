package com.example.server_fast_food_da3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.EventBus.CategoryClick;
import com.example.server_fast_food_da3.EventBus.ChangeMenuClick;
import com.example.server_fast_food_da3.EventBus.MenuItemBack;
import com.example.server_fast_food_da3.EventBus.ToastEvent;
import com.example.server_fast_food_da3.EventBus.UpdateCategoryClick;
import com.example.server_fast_food_da3.EventBus.UpdateDetailFood;
import com.example.server_fast_food_da3.EventBus.UserClick;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        updateToken();

        subscribeToTopic(Common.creatTopicOrder());

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order,R.id.nav_user)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey, ", Common.currentServerUser.getName(), txt_user);

        menuClick = R.id.nav_category; //Default

        checkIsOpenFromActivity();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder)
        {
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }).addOnCompleteListener(task -> {
            if(!task.isSuccessful())
                Toast.makeText(this, "Failed "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.food_list_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

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
    public void onCategoryClick(CategoryClick event)
    {
        if(event.isSuccess())
        {
            if(menuClick != R.id.nav_food_list)
            {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserClick(UserClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_user);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event)
    {
        if(event.isUpdate())
        {
            Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        }
        else
        {
            Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
        }

        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodlist()));
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event)
    {
        if(event.isFromFoodList())
        {
            //Clear
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);
        }
        else
        {
            //Clear
            navController.popBackStack(R.id.nav_food_list, true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(UpdateCategoryClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_category);
        }
    }

    public void onUpdateDetailFoodClick(UpdateDetailFood event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_food_list);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();

        switch (menuItem.getItemId()){
            case R.id.nav_category:
                if(menuItem.getItemId() != menuClick)
                {
              //       navController.popBackStack(); // this will remove all the items in the stack
                    navController.navigate(R.id.nav_category);
                }
                break;
            case R.id.nav_order:
                if(menuItem.getItemId() != menuClick)
                {
              //      navController.popBackStack(); // this will remove all the items in the stack
                    navController.navigate(R.id.nav_order);
                }
                break;
            case R.id.nav_user:
                if(menuItem.getItemId() != menuClick)
                {
             //       navController.popBackStack(); // this will remove all the items in the stack
                    navController.navigate(R.id.nav_user);
                }
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }
        menuClick = menuItem.getItemId();
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
                Common.currentServerUser = null;
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event)
    {
        menuClick = -1;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }
}