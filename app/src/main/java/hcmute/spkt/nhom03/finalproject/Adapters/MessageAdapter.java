package hcmute.spkt.nhom03.finalproject.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            if (message.getMessage().equals("photo")) {
                viewHolder.binding.imgView.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).into(viewHolder.binding.imgView);
            }
            viewHolder.binding.message.setText(message.getMessage());
//            User user = users.get(position);
//            Glide.with(context).load(user.getProfileImage())
//                    .placeholder(R.drawable.img_avt)
//                    .into(viewHolder.binding.imgView);
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());
            Intent intent = new Intent();
            String profile = intent.getStringExtra("image");
            Glide.with(context).load(profile)
                    .placeholder(R.drawable.img_avt)
                    .into(viewHolder.binding.imgAvt);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
