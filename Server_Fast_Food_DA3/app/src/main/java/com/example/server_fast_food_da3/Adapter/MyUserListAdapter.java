package com.example.server_fast_food_da3.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.server_fast_food_da3.Callback.IRecyclerClickListener;
import com.example.server_fast_food_da3.Common.Common;
import com.example.server_fast_food_da3.Model.UserModel;
import com.example.server_fast_food_da3.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyUserListAdapter extends RecyclerView.Adapter<MyUserListAdapter.MyViewHolder> {

    private Context context;
    private List<UserModel> userModelList;

    public MyUserListAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (userModelList.get(position).getProfileImg()!=null){
            Glide.with(context).load(userModelList.get(position).getProfileImg()).into(holder.profile_img_user);
        }else{
            holder.profile_img_user.setImageResource(R.drawable.ic_user);
        }

            holder.stt.setText(String.valueOf(position+1));

//        holder.stt.setText(new StringBuilder("$")
//                .append(userModelList.get(position).getUid()));

        holder.txt_user_item.setText(userModelList.get(position).getName());
        holder.txt_id_item.setText(userModelList.get(position).getUid());
        holder.txt_phone_item.setText(userModelList.get(position).getPhone());
        holder.txt_address_item.setText(userModelList.get(position).getAddress());

        //Event
        holder.setListener((view, pos) -> {
            Common.userSelected= userModelList.get(pos);
            Common.userSelected.setKey(String.valueOf(pos));
        });

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }
    public void removeItem(int pos) {
        userModelList.remove(pos);
    }

    public UserModel getItemAtPosition(int pos) {
        return userModelList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.txt_user_item)
        TextView txt_user_item;
        @BindView(R.id.stt)
        TextView stt;
        @BindView(R.id.profile_img_user)
        ImageView profile_img_user;
        @BindView(R.id.txt_phone_item)
        TextView txt_phone_item;
        @BindView(R.id.txt_address_item)
        TextView txt_address_item;
        @BindView(R.id.txt_id_item)
        TextView txt_id_item;


        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClickListener(view, getAdapterPosition());
        }
    }
}


