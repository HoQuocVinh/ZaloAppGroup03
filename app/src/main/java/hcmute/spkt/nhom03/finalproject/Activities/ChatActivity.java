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
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

import hcmute.spkt.nhom03.finalproject.Adapters.MessageAdapter;
import hcmute.spkt.nhom03.finalproject.Controller.AudioRecorder;
import hcmute.spkt.nhom03.finalproject.Models.Message;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityChatBinding;


public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessageAdapter adapter; //* Khởi tạo adapter
    ArrayList<Message> messages; //* Khởi tạo messages
    String senderRoom, receiverRoom; //* Khởi tạo senderRoom, receiverRoom
    FirebaseDatabase database;  //* Khởi tạo database
    FirebaseStorage storage;    //* Khởi tạo storage
    FirebaseUser user;  //* Khởi tạo user
    ProgressDialog dialog;  //* Khởi taoị dialog
    String senderUid;   //* Khởi tạo senderUdi

    String name, token, profile, uid, nameCurrentUser;  //* Khởi tạo name, token, profile, udi
//    String mFileName = null;    //* Khởi tạo mFileName

    private StorageReference mStorage;  //* Khởi tạo mStarage
    private AudioRecorder audioRecord;  //* Khởi tạo audioRecord
    private File recordFile;    //* Khởi taoị recordFile


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //* Mapping lại database --> database sẽ nhận dường dẫn mặc định
        database = FirebaseDatabase.getInstance();
        //* Mapping lại storage --> storage sẽ nhận dường dẫn mặc định
        storage = FirebaseStorage.getInstance();
        //* Mapping lại user --> getCurrentUser() có tác dụng kiểm tra user hiện tại là gì
        user = FirebaseAuth.getInstance().getCurrentUser();
        //* Mapping lại mStorage --> mStorage sẽ nhận đường dẫn mặc định
        mStorage = FirebaseStorage.getInstance().getReference();
        //* mapping lại mFileName và nhận đường dẫn từ file
//        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFileName += "/recorded_audio.3gp";

        //* Mapping lại dialog
        dialog = new ProgressDialog(this);
        //* Set text cho dialog
        dialog.setMessage("Uploading image...");
        //* Tắt cancel của dialog
        dialog.setCancelable(false);
        //* Mapping lại audioRecord
        audioRecord = new AudioRecorder();

        //* Sử dụng textWatcher để lắng nghe sự kiện trên edtChat
        binding.edtChat.addTextChangedListener(textWatcher);
        //* Show layout để thu record audio
        showLayoutRecordBelow();
        //* Tạo các biến kiểu String để nhận các giá trị được gửi từ UsersAdapter
        nameCurrentUser = getIntent().getStringExtra("cUserName");
        name = getIntent().getStringExtra("name");  //* Nhận giá trị và gán vào name
        profile = getIntent().getStringExtra("image");  //* Nhận giá trị và gán vào image
        uid = getIntent().getStringExtra("uid");    //* Nhận giá trị và gán vào uid
        token = getIntent().getStringExtra("token");    //* Nhận giá trị và gán vào token

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
        //* Gọi hàm chat() được khưởi tạo bên dưới
        chat();
    }

    //* Khởi tạo hàm show layout record --> thua âm thanh record
    private void showLayoutRecordBelow() {
        binding.rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //* Sử dụng Rect để xem sử thay đổi trong layout
            Rect r = new Rect();
            binding.rootView.getWindowVisibleDisplayFrame(r);
            //* Khởi tạo biến screenHeight để nhận giá trị về chiều cao của  màn hình
            int screenHeight = binding.rootView.getRootView().getHeight();

            //* r.bottom là vị trí phía trên bàn phím mềm hoặc nút thiết bị.
            // Nếu bàng phím được mở r.bottom sẽ nhỏ hơn trước
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) { // Sử dụng 0.15 là tỉ lệ để xác định chiêu cao của bàng phím
                // Nếu bàng phím mở
                //* Lắng nghe sự kiện click cho imgRecord1
                binding.imgRecord1.setOnClickListener(v -> {
                    //* Kiểm tra cấp quyền cho nut record
                    checkPermission();
                    //* show imgRecord1
                    binding.imgRecord1.setVisibility(View.INVISIBLE);
                    //* gone imgRecord2
                    binding.imgRecord2.setVisibility(View.VISIBLE);
                    //* show linearRecord --> là nơi record audio
                    binding.linearRecord.setVisibility(View.VISIBLE);
                    //* Đóng bàng phím
                    closeKeyboard();
                });
            } else {
                //* Nếu bàng phím đóng
                //* Lăng nghe sự kiện click cho edtChat
                binding.edtChat.setOnClickListener(v -> {
                    //* gone linearRecord
                    binding.linearRecord.setVisibility(View.GONE);
                    //* show imgRecord2
                    binding.imgRecord2.setVisibility(View.INVISIBLE);
                    //* hide imgRecord1
                    binding.imgRecord1.setVisibility(View.VISIBLE);
                });

                //* Lắng nghe sự kiện lcick cho imgRecord1
                binding.imgRecord1.setOnClickListener(v -> {
                    //* Thực hiện kiểm tra cấp quyền cho micro
                    checkPermission();
                    //* hide imgRecord1
                    binding.imgRecord1.setVisibility(View.INVISIBLE);
                    //* show imgRecord2
                    binding.imgRecord2.setVisibility(View.VISIBLE);
                    //* hide linearRecord
                    binding.linearRecord.setVisibility(View.VISIBLE);
                });
                //* Lắng nghe sẹ kiện click cho imgRecord2
                binding.imgRecord2.setOnClickListener(v -> {
                    //* hide imgRecord2
                    binding.imgRecord2.setVisibility(View.INVISIBLE);
                    //* show imgRecord1
                    binding.imgRecord1.setVisibility(View.VISIBLE);
                    //* show linearRecord
                    binding.linearRecord.setVisibility(View.GONE);
                    //* Thực hiện đóng bàng phím
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

    //* Khởi tạo hàm setListenersRecyclerView --> để scroll recycler view khi có tin nhắn đến
    private void setListenersRecyclerView() {
        binding.recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            //* Nếu giá trị bottom nhro hơn giá trị oldbottom
            if (bottom < oldBottom)
                //* Nếu message.size() --> nghia là không co tin nhắn đến
                if (messages.size() == 0) {
                    //* Không thực hiện việc scroll recyclerview
                    binding.recyclerView.postDelayed(() -> binding.recyclerView.smoothScrollToPosition(messages.size()), 100);
                } else {
                    //* Thực hiện viêc scroll recyclerview
                    binding.recyclerView.postDelayed(() -> binding.recyclerView.smoothScrollToPosition(messages.size() - 1), 100);
                }
        });
    }

    public void chat() {
        //* Mapping lại messages
        messages = new ArrayList<>();
        //* Mapping adapter
        adapter = new MessageAdapter(this, messages);
        //* Set layout cho recyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //* SetAdapter vào recyclerview
        binding.recyclerView.setAdapter(adapter);

        //* lấy giá trị uid được put từ UserAdapter và gán vào rêciverUid
        String receiverUid = getIntent().getStringExtra("uid");
        //* Mapping senderUid
        senderUid = FirebaseAuth.getInstance().getUid();
        //* Gọi hàm scroll cho recyclerview
        setListenersRecyclerView();
        //* sử dụng database và get đến đường dẫn được chỉ định
        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //* snapshot.exists() sử dụng để kiểm tra dữ liệ trong đường dẫn cung cấp có tồn tại không
                if (snapshot.exists()) {
                    //* Gán gán giá trị từ snapshot cko status kiểu string
                    String status = snapshot.getValue(String.class);
                    //* Kiểm tra trạng thái hiện tại có khác rỗng không
                    if (!Objects.requireNonNull(status).isEmpty()) {
                        //* Nếu status là offline
                        if (status.equals("Offline")) {
                            //* Ẩn trạng thái đi
                            binding.txtStatusBar.setVisibility(View.GONE);

                        } else {
                            //* set text cho trạng thái
                            binding.txtStatusBar.setText(status);
                            //* Hiện trạng thái hoạt động lên
                            binding.txtStatusBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //* Gán giá trị cho senderRoom --> tạo id cho phòng chat
        senderRoom = senderUid + receiverUid;
        //* Gán giá trị cho receiverRoom --> tạo id cho phòng gửi
        receiverRoom = receiverUid + senderUid;
        //* Sử dụng database và get đến đường đẫn được chỉ định
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //* Clear dữ liệu trong messages
                        messages.clear();
                        //* Sử dụng vòng lặp để quét hết giá trị từ đường dẫn
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            //* Sử dụng message để lấy giá trị từ firebase
                            Message message = snapshot1.getValue(Message.class);
                            //* Add message vào trong adapter message
                            messages.add(message);
                            //* Thực thiện scrool tin nhắn về dưới dùng
                            binding.recyclerView.smoothScrollToPosition(Objects.requireNonNull(binding.recyclerView.getAdapter()).getItemCount() - 1);
                        }
                        //* update lại sự thay đổi trong adapter
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //* Lắng nghe sự kiện click cho imgsend
        binding.imgSend.setOnClickListener(v -> {
            //* Tạo biến messageTxt và nhận giá trị từ edtChat
            String messageTxt = binding.edtChat.getText().toString();
            //* Khởi tạo biến datá
            Date date = new Date();
            //* Gán giá trị cho model message
            Message message = new Message(messageTxt, senderUid, date.getTime(), senderRoom);
            //* Set giá trị null cho edtChat
            binding.edtChat.setText("");
            //* Tạo randomkey và put lên database
            String randomKey = database.getReference().push().getKey();
            //* Tạo lastMsgObj kiểu HashMap
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            //* Put giá trị message.getMessage() vào trong "lastMsg"
            lastMsgObj.put("lastMsg", message.getMessage());
            //* Put giá trị và date.getTime() vào trong lastMsgTime
            lastMsgObj.put("lastMsgTime", date.getTime());

            //* khởi tạo biến uid kiểu string và gán giá trị user.getUid()
            String uid = user.getUid();
            //* Dử dụng reference để nhận đường dẫn được chỉ định
            DatabaseReference reference = database.getReference("users").child(uid);
            //* Lăng nghe sẹ kiện trên firebase
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //* Sử dụng model user để lưu trữ dữ liệu
                    User user = snapshot.getValue(User.class);
                    //* Khởi tạo urlImageUserObj kiểu HashMap
                    HashMap<String, Object> urlImageUserObj = new HashMap<>();
                    //* Tạo biến urlImage để nhận giá trị user.getProfileImage()
                    String urlImage = Objects.requireNonNull(user).getProfileImage();
                    //* Put giá trị urlImage vào trong "urlImageUser"
                    urlImageUserObj.put("urlImageUser", urlImage);
                    //* Sử dụng database để update giá trị trường đường dẫn được gán
                    database.getReference().child("chats").child(senderRoom).updateChildren(urlImageUserObj);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //* updata giá trị được put trong lastMsgObj vào trong senderRoom
            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
            //* updata giá trị được put trong lastMsgObj vào trong receiverRoom
            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
            assert randomKey != null;
            //* Sử dụng databsse để get đến đường dẫn được chỉ định
            database.getReference().child("chats")
                    .child(senderRoom)  //* get senderRoom
                    .child("messages")  //* get đến messages
                    .child(randomKey)   //* get đến randomKey
                    .setValue(message)  //* set giá message vào firebase với đường dẫn theo tứ tự các child
                    .addOnSuccessListener(avoid -> database.getReference().child("chats")

                            .child(receiverRoom)    //* get receiverRoom
                            .child("messages")  //* get đến messages
                            .child(randomKey)   //* get đến randoomKey
                            .setValue(message)  //* set giá trị message vào firebase với đường dẫn theo tứ tự các child
                            .addOnSuccessListener(avoid1 -> {
                                sendNotification(nameCurrentUser, message.getMessage(), token); //* sent thông báo khi có tin nhắn đến
                            }));
        });
        //* Lắng nghe sự kiện click trên imgPhoto
        binding.imgPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            //* select image từ library
            intent.setAction(Intent.ACTION_GET_CONTENT);
            //* set Type cho image
            intent.setType("image/*");
            //* Đóng bàng phím máy tính
            closeKeyboard();
            startActivityForResult(intent, 25);
        });

        //* Gọi hàm setupRecord();
        setUpRecord();
        //* Khởi tạo biến handler
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
                //* sử dụng database để set giá trị cho senderUid -->
                //* Khi thực hiện nhập tin nhắn thì người nhận sẽ nhận biết được mình đang nhập
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            //* Khởi tạo userStoppedTyping kiểu Runnable
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    //* set giá trị về online
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
    }


    private void checkPermission() {
        //* Khi chưa được cấp quyền
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            //* Thực hiện yêu cầu xin cấp quyền
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            //* Khi đã được cấp quyền
        else
            //* Create method
            setUpRecord();
    }

    //* Khởi tạo hàm setupRecord
    private void setUpRecord() {
        //* IMPORTANT
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setLessThanSecondAllowed(false);
        //* Sử setOnRecordListener -> tồn tại trong libary được add vào gradle
        binding.recordView.setOnRecordListener(new OnRecordListener() {
            //* Khi bắt đầu record
            @Override
            public void onStart() {
                //* Sử dụng recordFile để lưu vào đường dẫn đến file record
                recordFile = new File(getFilesDir(), UUID.randomUUID().toString() + ".3gp");
                try {
                    //* sử dụng audioRecord để start đến đường dẫn của recordFile
                    audioRecord.start(recordFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace(); //* Thực hiện show lỗi
                }
            }

            //* Khi hủy bỏ record
            @Override
            public void onCancel() {
                //* Gán giá trị cho stopRecording là false --> dừng việc record
                stopRecording(true);
            }


            //* Khi hoàn thành record
            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //* Gán gia trị cho stopRecording là false
                stopRecording(false);
                //* Gọi hàm để upload record
                uploadRecord();
            }

            //* Khi record có thời gian = 0
            @Override
            public void onLessThanSecond() {
                //* set stopRecording với giá tị là true
                stopRecording(true);
            }
        });
    }

    //* Tạo hàm stopRecording
    private void stopRecording(boolean deleteFile) {
        //* Dừng việc record lại
        audioRecord.stop();
        //* Nếu đường dẫn đến recordFile != null
        if (recordFile != null && deleteFile) {
            //* Thực hiện delete record
            recordFile.delete();
        }
    }


    //* Khởi tạo hàm sendNotification
    public void sendNotification(String name, String message, String token) {
        try {
            //* Khởi tạo queue
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

            //* Khởi tọa data
            JSONObject data = new JSONObject();
            //* put name vào title
            data.put("title", name);
            //* put message vào body
            data.put("body", message);

            //* Khởi tạo notificationData
            JSONObject notificationData = new JSONObject();
            //* put data vào notification
            notificationData.put("notification", data);
            //* put token vào to
            notificationData.put("to", token);

            //* sử dụng request để kiểm tra respone khi gửi
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, notificationData,
                    response -> {
                    },
                    error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    //* Khởi tạo map kiểu HashMap
                    HashMap<String, String> map = new HashMap<>();
                    //* Khởi tạo biến key và nhận giá trị key từ firebase
                    String key = "key=AAAA7YJ_jVk:APA91bHrWCjgoRzey8VSPD57QV8c-zNPRyY4aS4tRcO6zKxPS8qmSkkHvsJu8eNah-HpdhBzLobVDL43WAry5mJ4iqb57q7LazEefqhtGTveM0t8JYKSb5YbVTX5LODie56OCA1IfMXJ";
                    //* Put application/json vào Content-Type
                    map.put("Content-Type", "application/json");
                    //* put key vào Authorization
                    map.put("Authorization", key);
                    //* Trả về giá trị của map
                    return map;
                }
            };
            //* add request vào queue
            queue.add(request);
        } catch (Exception ex) {

        }
    }

    //* Khởi tạo hàm uploadRecord()
    private void uploadRecord() {
        //* Khởi tạo selectedVoice và nhận giá trị của đường dẫn file đã đựo record
        Uri selectedVoice = Uri.fromFile(new File(recordFile.getPath()));
        //* sử dụng reference và gán đường dẫn của firebase vào
        StorageReference reference = mStorage.child("Audio").child(UUID.randomUUID().toString() + ".3gp");

        //* Set text cho dialog để người dùng có thể biết việc upload voice
        dialog.setMessage("Upload voice...");
        //* show thông báo của dialog
        dialog.show();
        //* Thực hiện putFile với đường dẫn đã được khởi tạo ở trên
        reference.putFile(selectedVoice).addOnCompleteListener(task -> {
            //* Tắt dialig
            dialog.dismiss();
            //* Nếu việc upload hàn thành
            if (task.isSuccessful()) {
                //* Sử dụng reference để get đến đường dẫn của file vừa được updata
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    //* sử dụng filePath kiểu string để nhận giá trị của đường dẫn
                    String filePath = uri.toString();
                    //* Khởi tạo biến messagéTxt và gán giá trị [voice] vào
                    String messageTxt = "[voice]";
                    //* Khởi tạo biến data
                    Date date = new Date();
                    //* Load dữ liệu vào message
                    Message message = new Message(messageTxt, senderUid, date.getTime(), senderRoom);
                    //* Tạo biến lastMsgObj kiểu HashMap
                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    //* put message.getMessage vào trong "lastMsg"
                    lastMsgObj.put("lastMsg", message.getMessage());
                    //* put message.getMessage vào trong "lastMsgTime"
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    //* Updata giá trị cho senderRoom
                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    //* Updât giá trị cho receiverRoom
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    //* set giá trị vào hàm setMessage
                    message.setMessage("[voice*]");
                    //* set giá trị vào setVoiceUrl
                    message.setVoiceUrl(filePath);
                    //* set giá trị cho null edtChat
                    binding.edtChat.setText("");
                    //* Sử dụng database để get đến đường dẫn được chỉ định
                    database.getReference().child("chats")  //* get đến chats
                            .child(senderRoom)  //* get đến senderRoom
                            .child("messages")  //* get đến message
                            .push() //* push()
                            .setValue(message) //* setValue với giá trị  message đã được tạo ở trên
                            .addOnSuccessListener(avoid -> database.getReference().child("chats")
                                    .child(receiverRoom)    //* get đến receiverRoom
                                    .child("messages")  //* get đến message
                                    .push() //* push()
                                    .setValue(message).addOnSuccessListener(avoid1 -> {
                                    }));
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Nếu requestData == 25
        if (requestCode == 25) {
            //* Nếu data != null
            if (data != null) {
                //* Nếu dữ liệu data.getData()!=null
                if (data.getData() != null) {
                    //* Sử dụng selectedImage để lưu trữ  data.getData()
                    Uri selectedImage = data.getData();
                    //* Khởi tạo calendar
                    Calendar calendar = Calendar.getInstance();
                    //* sử dụng referencee để get đến đường dẫn được chỉ định
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    //* show dialog
                    dialog.show();
                    //* Thực hiện putFIle lên firbase
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {
                        //* hide dialog
                        dialog.dismiss();
                        //* Nếu việc upload thành công
                        if (task.isSuccessful())
                            //* sử dụng reference để getDownloadUrl
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                //* Sử dụng filePath để lưu trữ đường dẫn của file vừa được upload lên firebase
                                String filePath = uri.toString();
                                //* gán giá trị cho messageTxt
                                String messageTxt = "[photo]";
                                //* Khởi tạo biến data
                                Date date = new Date();
                                //* Gán giá trị cho model message
                                Message message = new Message(messageTxt, senderUid, date.getTime(), senderRoom);
                                //* Tạo lastMsgObj kiểu HashMap
                                HashMap<String, Object> lastMsgObj = new HashMap<>();
                                //* put message.getMessage() vào lastMsg
                                lastMsgObj.put("lastMsg", message.getMessage());
                                //* put data.getTime() vào lastMsgTime
                                lastMsgObj.put("lastMsgTime", date.getTime());

                                //* Updata giá trị cho senderRoom
                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                //* Updata giá trị cho receiverRoom
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                //* gán giá trị cho setMessage
                                message.setMessage("[photo*]");
                                //* gán giá rtị cho setImageUrl
                                message.setImageUrl(filePath);
                                //* set giá trị null cho edtChat
                                binding.edtChat.setText("");
                                //* sử dụng database get đến đường dẫn được chỉ định
                                database.getReference().child("chats")
                                        .child(senderRoom)  //* get đến senderRoom
                                        .child("messages")  //* get đến message
                                        .push() //* push()
                                        .setValue(message) //* setValue vơi giá trị message được tạo phía trên
                                        .addOnSuccessListener(avoid -> database.getReference().child("chats")
                                                .child(receiverRoom)    //* get đến receiverRoom
                                                .child("messages")  //* get đến message
                                                .push() //* push(0
                                                .setValue(message) //* setValue với giá trị message được tạo phía trên
                                                .addOnSuccessListener(avoid1 -> {

                                                }));
                            });
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //* gán giá trị của currentId
        String currentId = FirebaseAuth.getInstance().getUid();
        //* thực hiện set online cho current user hiện tại
        if (currentId != null)
            database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //* Gán giá trị cho currentId
        String currentId = FirebaseAuth.getInstance().getUid();
        //* Thực hiện set offline cho current user hiện tại
        if (currentId != null)
            database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Offline");
    }

    //* Khởi tạo hàm onCreateOptionsMenu
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
            //* Gán giá trị cho textChat với giá trị được nhận vào từ edtChat
            String textChat = binding.edtChat.getText().toString();
            //* Nếu textChat có giá trị null
            if (!textChat.isEmpty()) {
                //* hide selectSend
                binding.selectSend.setVisibility(View.INVISIBLE);
                //* show imgsend
                binding.imgSend.setVisibility(View.VISIBLE);
            } else {
                //* show selectSend
                binding.selectSend.setVisibility(View.VISIBLE);
                //* hide imgSend
                binding.imgSend.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

}