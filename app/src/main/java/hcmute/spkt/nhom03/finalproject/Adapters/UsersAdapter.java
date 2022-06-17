package hcmute.spkt.nhom03.finalproject.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Activities.ChatActivity;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.RowConversationBinding;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;    //* Khởi tạo contexto
    ArrayList<User> users;  //* Khởi tạo users kiểu ArrayList
    String cUserName;   //* khởi tạo cUserName
    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    //* Khởi tạo hàm create view holder
    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //* Thực hiện load layout row_conversation vào
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_conversation, parent, false);
        //* Trả về ReceiveViewHolder
        return new UsersViewHolder(view);
    }

    /*Hiển thị dữ liệu ở vị trí được chỉ định*/
    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        //* Sử dụng medel user để lây giá trị tại position
        User user = users.get(position);
        //* Khởi tạo senderId để nhận giá trị uid của người gửi
        String senderId = FirebaseAuth.getInstance().getUid();
        //* Khởi tạo senderRoom bằng giá trị uid của người gửi + uid người nhận
        String senderRoom = senderId + user.getUid();
        //* get đến đường dẫn được chỉ định
        FirebaseDatabase.getInstance().getReference()
                .child("chats") //* get đến node cha "chats"
                .child(senderRoom)  //* get đến con của "chats" và node đó có giá trị senderRoom
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //* Kiểm tra xem giá trị tại đó có tồn tại không
                        //* Nếu tồn tại
                        if (snapshot.exists()) {
                            //* Tạo biến lastMsg và nhận giá trị từ lasMsg  --> tin nhắn cuối cùng
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            //* Tạo biến time để nhận giá trị từ lastMsgTime --> thời gian gửi tin nhắn cuối cùng
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            //* Format cho thời gian
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                            //* Gán thời gian nhận tin nhắn cuối cùng vào msgTime
                            holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                            //* Gán tin nhắn cuối cùng vào lastMssg
                            holder.binding.lastMsg.setText(lastMsg);
                        } else {
                            //* Gán giá trị mặc định cho lastMSG
                            holder.binding.lastMsg.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        nameCurrentUser();

        /*Gán name vào TextView trong row_converstation*/
        holder.binding.txtUserName.setText(user.getName());

        /*Gán image vào ImageView trong row_converstation*/
        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.img_avt)
                .into(holder.binding.imgProfile);

        /*Tạo sự kiện click cko mỗi item khi click sẽ chuyển vào khung chat để gửi tin nhắn,
         * đồng thời gửi name, image, uid, cUserName để ChatActivity sử dụng*/
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("image", user.getProfileImage());
            intent.putExtra("uid", user.getUid());
            intent.putExtra("token", user.getToken());
            intent.putExtra("cUserName", cUserName);
            context.startActivity(intent);
        });
    }

    private void nameCurrentUser(){
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        String uidCurrent = Objects.requireNonNull(user1).getUid();
        //* Sử dụng databaseReference và gét giá trị đến đường dẫn trên firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uidCurrent);
        //* Sử dụng databaseReference để lắng nghe sự kiện thay đổi
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //* Sử dụng model user để nhận giá trị từ realtime database
                User user = snapshot.getValue(User.class);
                //* Lấy giá trị name của curent user và gán vào cUserName
                cUserName = Objects.requireNonNull(user).getName();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        //* Trả về độ dài của ArrayList
        return users.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        RowConversationBinding binding; //* Khởi tạo binding cho RowConversation

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
