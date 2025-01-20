package com.srujal.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.srujal.whatsappclone.Models.GroupChatModel;
import com.srujal.whatsappclone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GroupChatAdapter extends RecyclerView.Adapter {

    ArrayList<GroupChatModel> GroupChatModel;
    Context context;

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public GroupChatAdapter(ArrayList<GroupChatModel> GroupChatModel, Context context) {
        this.GroupChatModel = GroupChatModel;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_chat_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (GroupChatModel.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupChatModel msg = GroupChatModel.get(position);
        String formattedTime = formatTimestamp(msg.getTimeStamp());

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder senderHolder = (SenderViewHolder) holder;
            senderHolder.senderMsg.setText(msg.getMessage());
            senderHolder.senderTime.setText(formattedTime); // Display time
        } else {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;
            receiverHolder.receiverMsg.setText(msg.getMessage());
            receiverHolder.receiverTime.setText(formattedTime); // Display time
            receiverHolder.receiverName.setText(msg.getSenderName()); // Display sender name
        }
    }

    @Override
    public int getItemCount() {
        return GroupChatModel.size();
    }

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        return format.format(date);
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg, receiverTime, receiverName;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverName = itemView.findViewById(R.id.senderName);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }
}
