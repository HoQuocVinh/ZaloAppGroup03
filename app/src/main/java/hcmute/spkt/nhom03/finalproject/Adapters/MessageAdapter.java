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
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ItemReceiveBinding;
import hcmute.spkt.nhom03.finalproject.databinding.ItemSentBinding;


public class MessageAdapter extends RecyclerView.Adapter {
    Context context;    //* Khởi tạo context
    ArrayList<Message> messages;    //* Khởi tạo messages kiểu ArrayList
    final int ITEM_SENT = 1;    //* Tạo ITEM_SENT
    final int ITEM_RECEIVE = 2; //* Tạo ITEM_RECEIVE

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }


    //* Khởi tạo hàm create view holder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //* Kiểm tra xem viewType hiện tại là gì?
        //* Nếu là ITEM_SENT
        if (viewType == ITEM_SENT) {
            //* Thực hiện load layout item_sent vào
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            //* Trả về SentViewHolder
            return new SentViewHolder(view);
        } else { //* Nếu là ITEM_RECEIVE
            //* Thực hiện load layout item_receive vào
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            //* Trả về ReceiveViewHolder
            return new ReceiverViewHolder(view);

        }
    }

    //* Tạo hàm getItemView
    @Override
    public int getItemViewType(int position) {
        //* Khởi taoị message để lấy giá trị tại position của nó
        Message message = messages.get(position);
        //* Nếu uid từ firebaseAuth bằng với uid trong message
        //* Có nghĩa là ở người nhận tin nhắn sẽ có id của người gửi
        //* Nếu điều này là đúng
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getUid()).equals(message.getSenderId()))
            //* Trả về ITEM_SENT
            return ITEM_SENT;
            //* Người lại
        else return ITEM_RECEIVE; //* Trả về ITEM_RECEIVE
    }

    /*Hiển thị dữ liệu ở vị trí được chỉ định*/
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //* Sử dụng medel message để lây giá trị tại position
        Message message = messages.get(position);
        //* Nếu dữ liệu đang hiển thị ở SentViewHolder
        if (holder.getClass() == SentViewHolder.class) {
            //* Khởi tạo viewHolder
            SentViewHolder viewHolder = (SentViewHolder) holder;
            //* Nếu giá trị text của message là [photo*]
            if (message.getMessage().equals("[photo*]")) {
                //* show imgView
                viewHolder.binding.imgView.setVisibility(View.VISIBLE);
                //* gone message
                viewHolder.binding.message.setVisibility(View.GONE);
                //* gone voicePlayerVIew
                viewHolder.binding.voicePlayerView.setVisibility(View.GONE);
                //* Thực hiện load image vào imgView
                Glide.with(context).load(message.getImageUrl()).into(viewHolder.binding.imgView);
            } else if (message.getMessage().equals("[voice*]")) { //* Nếu giá trị text của message là [voice*]
                //* gone imgView
                viewHolder.binding.imgView.setVisibility(View.GONE);
                //* gone message
                viewHolder.binding.message.setVisibility(View.GONE);
                //* show voicePlayerView
                viewHolder.binding.voicePlayerView.setVisibility(View.VISIBLE);
                //* Thực hiện load audio vào voicePlayerView
                viewHolder.binding.voicePlayerView.setAudio(message.getVoiceUrl());
            } else
                viewHolder.binding.message.setText(message.getMessage()); //* Thực hiện setText cho message
        } else {
            //* Nếu dữ liệu đang hiển thị ở ReceiverViewHolder
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            //* Khởi tạo receiveRoom để  nhận idRoom
            String receiveRoom = message.getIdRoom();
            //* Khởi tạo database và gán giá trị đường dẫn mặc đinhj
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            //* Gọi hàm setImageForUserSent
            setImageForUserSent(viewHolder, database, receiveRoom);
            //* Nếu giá trị nhận vào là [photo*]
            if (message.getMessage().equals("[photo*]")) {
                //* show imgView
                viewHolder.binding.imgView.setVisibility(View.VISIBLE);
                //* gone message
                viewHolder.binding.message.setVisibility(View.GONE);
                //* gone voicePlayerView
                viewHolder.binding.voicePlayerView.setVisibility(View.GONE);
                //* Load image vào imgView
                Glide.with(context).load(message.getImageUrl()).into(viewHolder.binding.imgView);
            } else if (message.getMessage().equals("[voice*]")) { //* Nếu giá trị text của message là [voice*]
                //* gone imgView
                viewHolder.binding.imgView.setVisibility(View.GONE);
                //* gone message
                viewHolder.binding.message.setVisibility(View.GONE);
                //* show voicePlayerView
                viewHolder.binding.voicePlayerView.setVisibility(View.VISIBLE);
                //* load audi vào voicePlayerVIew
                viewHolder.binding.voicePlayerView.setAudio(message.getVoiceUrl());
            } else
                viewHolder.binding.message.setText(message.getMessage());    //* Thực hiện setText cho message
        }
    }

    //* Khởi tạo hàm setImageForUserSent
    private void setImageForUserSent(RecyclerView.ViewHolder holder, FirebaseDatabase database, String receiveRoom) {
        //* Khởi tạo viewHolder từ ReceiverViewHolder
        ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
        //* Sử dụng database để get đến đường dẫn được chỉ định
        database.getReference()
                .child("chats") //* get đến node cha "chats"
                .child(receiveRoom) //* get đến con của "chat"
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //* Sử dụng urlImage để lưu giá trị tại node urlImageUser
                        String urlImage = snapshot.child("urlImageUser").getValue(String.class);
                        //* Thực hiện load urlImage vào trong imgAvt
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
        //* trả về độ dài của ArrayList
        return messages.size();
    }

    //* Khởi SentViewHolder
    public static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;  //* Khởi tạo binding cho ItemSent

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }
    //* Khởi ReceiverViewHolder
    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding; //* Khởi tạo binding cho itemReceive

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
