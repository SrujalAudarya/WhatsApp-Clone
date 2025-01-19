package com.srujal.whatsappclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.srujal.whatsappclone.ChatDetailsActivity;
import com.srujal.whatsappclone.Models.Users;
import com.srujal.whatsappclone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{

    ArrayList<Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sampleuser, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Users users = list.get(position);

        //Set image using picasso
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.avatar).into(holder.profileImg);

        // Set the text for each TextView
        holder.username.setText(users.getUserName());

        // Set the last message (static text for now)
        holder.lastMsg.setText("Last Message");

        holder.lastMsg.setText(users.getLastMessage());

        // Show status
//        if ("Online".equals(users.getStatus())) {
//            holder.userStatus.setText("Online");
//        } else {
//            try {
//                long lastSeen = Long.parseLong(users.getStatus());
//                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
//                holder.userStatus.setText("Last seen: " + sdf.format(new Date(lastSeen)));
//            } catch (NumberFormatException e) {
//                holder.userStatus.setText("Offline");
//            }
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailsActivity.class);
                intent.putExtra("userid",users.getUserId());
                intent.putExtra("profileImg",users.getProfilePic());
                intent.putExtra("userName",users.getUserName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImg;
        TextView username, lastMsg, userStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.tvUserName);
            lastMsg = itemView.findViewById(R.id.tvLastMsg);
            userStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
