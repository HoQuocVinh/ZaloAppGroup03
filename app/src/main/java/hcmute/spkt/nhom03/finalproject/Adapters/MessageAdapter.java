package hcmute.spkt.nhom03.finalproject.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Models.Message;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ItemReceiveBinding;
import hcmute.spkt.nhom03.finalproject.databinding.ItemSentBinding;


public class MessageAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Message> messages;
    ArrayList<User> users;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);

        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getUid()).equals(message.getSenderId()))
            return ITEM_SENT;
        else return ITEM_RECEIVE;
    }

    /*Hiển thị dữ liệu ở vị trí được chỉ định*/
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            if (message.getMessage().equals("[photo*]")) {
                viewHolder.binding.imgView.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.voicePlayerView.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).into(viewHolder.binding.imgView);
            } else if (message.getMessage().equals("[voice*]")) {
                viewHolder.binding.imgView.setVisibility(View.GONE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.voicePlayerView.setVisibility(View.VISIBLE);
                viewHolder.binding.voicePlayerView.setAudio(message.getVoiceUrl());
            }
            viewHolder.binding.message.setText(message.getMessage());
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            String receiveRoom = message.getIdRoom();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            setImageForUserSent(viewHolder, database,receiveRoom);
            if (message.getMessage().equals("[photo*]")) {
                viewHolder.binding.imgView.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.voicePlayerView.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).into(viewHolder.binding.imgView);
            } else if (message.getMessage().equals("[voice*]")) {
                viewHolder.binding.imgView.setVisibility(View.GONE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.voicePlayerView.setVisibility(View.VISIBLE);
                viewHolder.binding.voicePlayerView.setAudio(message.getVoiceUrl());
            }
            viewHolder.binding.message.setText(message.getMessage());
//            getImageUser(viewHolder, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()));
        }
    }

    private void setImageForUserSent(RecyclerView.ViewHolder holder, FirebaseDatabase database, String receiveRoom){
        ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
        database.getReference().child("chats")
                .child(receiveRoom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String urlImage = snapshot.child("urlImageUser").getValue(String.class);
                        Glide.with(context).load(urlImage)
                                .placeholder(R.drawable.img_avt)
                                .into(viewHolder.binding.imgAvt);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
