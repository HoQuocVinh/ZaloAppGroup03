package hcmute.spkt.nhom03.finalproject.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import hcmute.spkt.nhom03.finalproject.Adapters.MessageAdapter;
import hcmute.spkt.nhom03.finalproject.Controller.AudioRecorder;
import hcmute.spkt.nhom03.finalproject.Models.Message;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityChatBinding;


public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessageAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseUser user;
    ProgressDialog dialog;
    String senderUid;

    String name, token, profile, uid;
    String mFileName = null;

    private StorageReference mStorage;
    private AudioRecorder audioRecord;
    private File recordFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/recorded_audio.3gp";

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        audioRecord = new AudioRecorder();

        binding.edtChat.addTextChangedListener(textWatcher);
        showLayoutRecordBelow();
        //* Tạo các biến kiểu String để nhận các giá trị được gửi từ UsersAdapter
        name = getIntent().getStringExtra("name");
        profile = getIntent().getStringExtra("image");
        uid = getIntent().getStringExtra("uid");
        token = getIntent().getStringExtra("token");

        //* Tạo action bar, action bar ở đây đã được tạo trong activity_chat.xml
        setSupportActionBar(binding.toolbar);

        //* Tắt title của action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        //* Load name được gửi từ UserAdapter vào TextView (txtNameBar) trên action bar
        binding.txtNameBar.setText(name);

        //* Load image được gửi từ UserAdapter vào ImageView (imgBar) trên action bar
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.img_avt)
                .into(binding.imgBar);
        //* Tạo sự kiện click cho nút back để quay lại trang MainActivity
        binding.back.setOnClickListener(v -> finish());
        chat();
    }

    private void showLayoutRecordBelow() {
        binding.rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            binding.rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = binding.rootView.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                binding.imgRecord1.setOnClickListener(v -> {
                    checkPermission();
                    binding.imgRecord1.setVisibility(View.INVISIBLE);
                    binding.imgRecord2.setVisibility(View.VISIBLE);
                    binding.linearRecord.setVisibility(View.VISIBLE);
                    closeKeyboard();
                });
            } else {
                // keyboard is closed
                binding.edtChat.setOnClickListener(v -> {
                    binding.linearRecord.setVisibility(View.GONE);
                    binding.imgRecord2.setVisibility(View.INVISIBLE);
                    binding.imgRecord1.setVisibility(View.VISIBLE);
                });

                binding.imgRecord1.setOnClickListener(v -> {
                    checkPermission();
                    binding.imgRecord1.setVisibility(View.INVISIBLE);
                    binding.imgRecord2.setVisibility(View.VISIBLE);
                    binding.linearRecord.setVisibility(View.VISIBLE);
                });
                binding.imgRecord2.setOnClickListener(v -> {
                    binding.imgRecord2.setVisibility(View.INVISIBLE);
                    binding.imgRecord1.setVisibility(View.VISIBLE);
                    binding.linearRecord.setVisibility(View.GONE);
                    showKeyboard();
                });
            }
        });
    }

    //* Hiển thị bàng phím
    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //* Đóng bàng phím
    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
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
                            binding.recyclerView.smoothScrollToPosition(Objects.requireNonNull(binding.recyclerView.getAdapter()).getItemCount() - 1);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.imgSend.setOnClickListener(v -> {
            String messageTxt = binding.edtChat.getText().toString();
            Date date = new Date();
            Message message = new Message(messageTxt, senderUid, date.getTime(), senderRoom);
            binding.edtChat.setText("");
            String randomKey = database.getReference().push().getKey();
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", message.getMessage());
            lastMsgObj.put("lastMsgTime", date.getTime());


            String uid = user.getUid();
            DatabaseReference reference = database.getReference("users").child(uid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    HashMap<String, Object> urlImageUserObj = new HashMap<>();
                    String urlImage = Objects.requireNonNull(user).getProfileImage();
                    urlImageUserObj.put("urlImageUser", urlImage);
                    database.getReference().child("chats").child(senderRoom).updateChildren(urlImageUserObj);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

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
            closeKeyboard();
            startActivityForResult(intent, 25);
        });

        setUpRecord();
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


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            //* Khi chưa được cấp quyền
            //* Thực hiện yêu cầu xin cấp quyền
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        else
            //* Khi đã được cấp quyền
            //* Create method
            setUpRecord();
    }

    private void setUpRecord() {
        //* IMPORTANT
        binding.recordButton.setRecordView(binding.recordView);
        //* if you want to click the button (in case if you want to make the record button a Send Button for example..)
        binding.recordView.setLessThanSecondAllowed(false);
        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                recordFile = new File(getFilesDir(), UUID.randomUUID().toString() + ".3gp");
//                recordFile = new File(getFilesDir(), mFileName);
                try {
                    audioRecord.start(recordFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(ChatActivity.this, "OnStartRecord", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                stopRecording(true);
                Toast.makeText(ChatActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                stopRecording(false);
                String time = getHumanTimeText(recordTime);
                Toast.makeText(ChatActivity.this, "onFinishRecord - Recorded Time is: " + time + " File saved at " + recordFile.getPath(), Toast.LENGTH_SHORT).show();
                Log.d("RecordTime", recordFile.getPath());
                uploadRecord();
            }

            @Override
            public void onLessThanSecond() {
                stopRecording(true);

                Toast.makeText(ChatActivity.this, "OnLessThanSecond", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopRecording(boolean deleteFile) {
        audioRecord.stop();
        if (recordFile != null && deleteFile) {
            recordFile.delete();
        }
    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }


    public void sendNotification(String name, String message, String token) {
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

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, notificationData,
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


    private void uploadRecord() {
        Uri selectedVoice = Uri.fromFile(new File(recordFile.getPath()));
        StorageReference reference = mStorage.child("Audio").child(UUID.randomUUID().toString() + ".3gp");

        dialog.setMessage("Upload voice...");
        dialog.show();
        reference.putFile(selectedVoice).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String filePath = uri.toString();
                    String messageTxt = "[voice]";
                    Date date = new Date();
                    Message message = new Message(messageTxt, senderUid, date.getTime(),senderRoom);
                    HashMap<String, Object> lastMsgObj = new HashMap<>();

                    lastMsgObj.put("lastMsg", message.getMessage());
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    message.setMessage("[voice*]");
                    message.setVoiceUrl(filePath);
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
                });
            }
        });
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
                                String messageTxt = "[photo]";
                                Date date = new Date();
                                Message message = new Message(messageTxt, senderUid, date.getTime(),senderRoom);
                                HashMap<String, Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("lastMsg", message.getMessage());
                                lastMsgObj.put("lastMsgTime", date.getTime());

                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                message.setMessage("[photo*]");
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
//                binding.selectSend.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_out));
//                binding.imgPhoto.setVisibility(View.INVISIBLE);
                binding.selectSend.setVisibility(View.INVISIBLE);
                binding.imgSend.setVisibility(View.VISIBLE);
            } else {
//                binding.selectSend.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in));
//                binding.imgPhoto.setVisibility(View.VISIBLE);
                binding.selectSend.setVisibility(View.VISIBLE);
                binding.imgSend.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

}