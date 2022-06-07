package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Adapters.MessageAdapter;
import hcmute.spkt.nhom03.finalproject.Models.Message;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessageAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;
    String senderUid;

    String name, token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        binding.edtChat.addTextChangedListener(textWatcher);
        /*Tạo các biến kiểu String để nhận các giá trị được gửi từ UsersAdapter*/
        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");
        String uid = getIntent().getStringExtra("uid");
        String token = getIntent().getStringExtra("token");
//        Toast.makeText(this, token, Toast.LENGTH_SHORT).show();

        /*Tạo action bar, action bar ở đây đã được tạo trong activity_chat.xml*/
        setSupportActionBar(binding.toolbar);

        /*Tắt title của action bar*/
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        /*Load name được gửi từ UserAdapter vào TextView (txtNameBar) trên action bar*/
        binding.txtNameBar.setText(name);

        /*Load image được gửi từ UserAdapter vào ImageView (imgBar) trên action bar*/
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.img_avt)
                .into(binding.imgBar);
        /*Tạo sự kiện click cho nút back để quay lại trang MainActivity*/
        binding.back.setOnClickListener(v -> finish());
        chat();
    }

    private void setListenersRecyclerView() {
        binding.recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom)
                if (messages.size() == 0) {
                    binding.recyclerView.postDelayed(() -> binding.recyclerView.smoothScrollToPosition(messages.size()), 100);
                } else {
                    binding.recyclerView.postDelayed(() -> binding.recyclerView.smoothScrollToPosition(messages.size() - 1), 100);
                }
        });
    }


    public void chat() {

        messages = new ArrayList<>();
        adapter = new MessageAdapter(this, messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        String receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        setListenersRecyclerView();
        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (!Objects.requireNonNull(status).isEmpty()) {
                        if (status.equals("Offline")) {
                            binding.txtStatusBar.setVisibility(View.GONE);

                        } else {
                            binding.txtStatusBar.setText(status);
                            binding.txtStatusBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                            binding.recyclerView.smoothScrollToPosition(Objects.requireNonNull(binding.recyclerView.getAdapter()).getItemCount());
                        }
                        adapter.notifyDataSetChanged();
//                        scrollRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.imgSend.setOnClickListener(v -> {
            String messageTxt = binding.edtChat.getText().toString();
            Date date = new Date();
            Message message = new Message(messageTxt, senderUid, date.getTime());
            binding.edtChat.setText("");
            String randomKey = database.getReference().push().getKey();
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", message.getMessage());
            lastMsgObj.put("lastMsgTime", date.getTime());

            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
            assert randomKey != null;
            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey)
                    .setValue(message).addOnSuccessListener(avoid -> database.getReference().child("chats")

                    .child(receiverRoom)
                    .child("messages")
                    .child(randomKey)
                    .setValue(message).addOnSuccessListener(avoid1 -> {
                        sendNotification(name, message.getMessage(), token);
                    }));
        });
        binding.imgPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });
        final Handler handler = new Handler();
        binding.edtChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
    }


    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";
            /*
                "message":{
                    "to": "token"
                    "notification": {
                        "title": "sender's name",
                        "body": "message"
                    }
                }
            */

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, notificationData,
                    response -> Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(ChatActivity.this, error.toString(), Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "key=AAAA7YJ_jVk:APA91bHrWCjgoRzey8VSPD57QV8c-zNPRyY4aS4tRcO6zKxPS8qmSkkHvsJu8eNah-HpdhBzLobVDL43WAry5mJ4iqb57q7LazEefqhtGTveM0t8JYKSb5YbVTX5LODie56OCA1IfMXJ";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);
                    return map;
                }
            };
            queue.add(request);
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if (task.isSuccessful())
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String filePath = uri.toString();
                                String messageTxt = binding.edtChat.getText().toString();
                                Date date = new Date();
                                Message message = new Message(messageTxt, senderUid, date.getTime());
                                message.setMessage("photo");
                                message.setImageUrl(filePath);
                                binding.edtChat.setText("");
                                database.getReference().child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .push()
                                        .setValue(message).addOnSuccessListener(avoid -> database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .push()
                                        .setValue(message).addOnSuccessListener(avoid1 -> {

                                        }));
                                Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                            });
                    });
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String textChat = binding.edtChat.getText().toString();
            if (!textChat.isEmpty()) {
                binding.imgPhoto.setVisibility(View.INVISIBLE);
                binding.imgSend.setVisibility(View.VISIBLE);
            } else {
                binding.imgPhoto.setVisibility(View.VISIBLE);
                binding.imgSend.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}