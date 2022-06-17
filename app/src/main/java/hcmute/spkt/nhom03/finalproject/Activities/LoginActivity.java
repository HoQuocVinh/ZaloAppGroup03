package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import hcmute.spkt.nhom03.finalproject.Adapters.UsersAdapter;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;       //* Khởi tạo binding cho ActivityLogin
    ArrayList<User> users;  //* Khởi tạo uses kiểu ArrayList dùng để lưu trữ dữ liệu có trong model User
    FirebaseDatabase database;  //* Khởi tạo database --> dùng để truy xuất đến đường dẫn trên firebase
    FirebaseAuth auth;      //* Khởi tạo auth
    DatabaseReference reference;    //* Khởi tạo reference
    UsersAdapter usersAdapter;  //* Khởi tạo userAdapter dùng để cập nhật lại sự thay đổi
    FirebaseMessaging messaging;    //* Khởi tạo messaging
    String verificationId;  //* Khởi tạo verificationId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        //* setContentView
        setContentView(binding.getRoot());
        mapping();  //* Thực hiện mapping lại các biến đã được khởi taoị cục bộ
        binding.edtPhone.requestFocus(); /*Focus vào edtPhone*/
        showKeyboard(); /*Show bàng phím lên*/
        changeColorActionBar(); /*Thực hiện thay đổi màu của action bar*/
        customActionBar();  /*Tinh chỉnh lại action bar*/
        binding.edtOtp.setEnabled(false);
        binding.edtPhone.addTextChangedListener(textWatcher1);   /*Add sự kiện nghe khi thay đổi văn bản cho edtPhone*/
        binding.edtPassword.addTextChangedListener(textWatcher1);    /*Add sự kiện nghe khi thay đổi văn bản cho edtPhone*/
        binding.btnGetOtp.setOnClickListener(v -> {
            String  phone = binding.edtPhone.getText().toString();
            //* Thực hiện việc thay đổi màu cho btnContinue thành màu gray_ccc --> gray_ccc đã được tạo trong color.xml
            binding.btnGetOtp.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_ccc)));
            //* Thực hiện set sự kiện eanble cho btnContinue để không thể click được
            sendVerificationCodeToUser(phone);
            //* unable cho btnGetOtp
            binding.btnGetOtp.setEnabled(false);
            //* Thực hiện unable cho edtOtp
            binding.edtOtp.setEnabled(true);
            //* Thực hiện forcus cho edtOtp
            binding.edtOtp.requestFocus();
            //* Gọi hàm verifyCode và truyền vào code đã được tạo phía trên
            binding.edtOtp.addTextChangedListener(textWatcher);
        });

    }

    //* Khởi taoị hàm verifyCode
    private void verifyCode(String code) {
        //* Khởi tạo credential để thực hiện việc xác minh mã được gửi đến
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        //* Gọi hàm signInWithPhoneAuthCredential và truyền vào credential đã được tạo
        signInWithPhoneAuthCredential(credential);
    }

    //* Khởi tạo hàm signInWithPhoneAuthCredential
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //* Sử dụng auth để check
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    //* Nếu mã nhập vào hợp lí
                    if (task.isSuccessful()) {
                        //* Show thông báo "Verification Completed"
                        Toast.makeText(LoginActivity.this, "Verification Completed", Toast.LENGTH_SHORT).show();
                        //* Thực hiện chuyển sang SetupProfileActivity
                    } else { //* Ngược lại
                        //* Show thông báo "Verification Not Completed! Try again." --> cho người dùng biết đã nhập sại
                        Toast.makeText(LoginActivity.this, "Verification Not Completed! Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //* Khởi tạo hàm sendVerificationCodeToUser
    private void sendVerificationCodeToUser(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone)       // số điện thaoị để xácminh
                        .setTimeout(60L, TimeUnit.SECONDS) // Thời gian chờ xác minh
                        .setActivity(this)                 // Activity
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    //* Định dạng lại mCallbacks
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            //* verificationId bằng giá trị OTP được gửi đến
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            //* code bằng với OTP được gửi đến
            final String code = phoneAuthCredential.getSmsCode();
            //* Thực hiện đóng keyboard
            showKeyboard();
            //* Nếu code != null nghĩa là otp đã được gửi
            if (code != null) {
                //* Set giá trị nhận được vào otpViwe
                binding.edtOtp.setText(code);
                //* Goi hàm kiểm tra OTp
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            //* Thưck hiện show lỗi lên màn hình
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            //* dimiss dialog
        }
    };

    //* Khởi tạo hàm textWatcher
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //* Khởi tạo otp và gán giá trị của nó bằng edtOtp
            String otp = binding.edtOtp.getText().toString();
            //* Khởi tạo crdential
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            //* Sử dụng auth để kiểm tra việc sigin
            auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                //* Nêu task hoàn thành
                if(task.isSuccessful()){
                    //* Set enabled cho edtPassword
                    binding.edtPassword.setEnabled(true);
                    //* Thực hiện focus vào edtPassword
                    binding.edtPassword.requestFocus();
                } else {
                    //* Set unable cho edtPassword
                    binding.edtPassword.setEnabled(false);
                }
            });
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //* Khưởi tạo hàm mapping
    private void mapping() {
        database = FirebaseDatabase.getInstance();
        messaging = FirebaseMessaging.getInstance();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        reference = database.getReference("users");
        auth = FirebaseAuth.getInstance();
    }

    public void login(View view) {
        /*Khởi tạo biến phone kiểu string để nhận giá trị của edtPhone (edtPhone là EditText mà người dùng nhập kí tự vào)*/
        String phone = binding.edtPhone.getText().toString().trim();
        /*Khởi tạo biến password kiểu string để nhận giá trị của edtPhone (edtPassword là EditText mà người dùng nhập kí tự vào)*/
        String password = binding.edtPassword.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE); /* Hiện progress bar khi click vào button Setup Profile*/
        binding.btnLogin.setVisibility(View.INVISIBLE); /* Ẩn button Login đi*/

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {  /*Lây đữ liệu từ realtime database*/
                    User user = dataSnapshot.getValue(User.class);  /*Sử dụng model User để nhận dữ liệu từ realtime database*/
                    users.add(user);    /*Add dữ liệu nhận từ realtime database vào user (ArrayList)*/
                }

                usersAdapter.notifyDataSetChanged();    //*Commit lại sự thay đổi
                boolean isHasUser = false;
                for (User user : users) {   //* Chạy vòng lăp for trên ArrayList
                    String phoneAuth = user.getPhoneNumber();   //* Tạo một biến phoneAuth kiểu string để nhận giá trị phoneNumber
                    String passwordAuth = user.getPassword();   //* Tạo một biến passwordAuth kiểu string để nhận giá trị password
                    if (phone.equals(phoneAuth) && password.equals(passwordAuth)) {
                        String uid = user.getUid();
                        token(messaging, uid);
                        isHasUser = true;
                        break;
                    }
                }
                if (isHasUser) {
                    binding.progressBar.setVisibility(View.INVISIBLE);  /* Ẩn progress bar đi để show lại button login*/
                    binding.btnLogin.setVisibility(View.VISIBLE);   /* Show button login */
                    /*Gọi hàm startActivity() để chuyển từ trang hiện tại (login) vào trong chủ (main)*/
                    binding.error.setVisibility(View.GONE); /*Ẩn thông báo error đi*/
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    closeKeyboard();
                } else {
                    binding.progressBar.setVisibility(View.INVISIBLE);  /* Ẩn progress bar đi để show lại button login*/
                    binding.btnLogin.setVisibility(View.VISIBLE);   /* Show button login */
                    binding.error.setVisibility(View.VISIBLE);  /*Show thông báo error bằng TextView đã được design trong file activity_login.xml*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void token(FirebaseMessaging messaging, String uid) {
        //* Sử dụng messaging để getToken
        messaging.getToken().addOnSuccessListener(token -> {
            //* Tạo map kiểu HashMap
            HashMap<String, Object> map = new HashMap<>();
            //* put token vào trong biến "token"
            map.put("token", token);
            //* Sử dụng database và truy xuất đến đường dẫn đã được cung cấp bến dưới
            database.getReference()
                    .child("users") //* Thông qua nút users
                    .child(uid) //* Thông qua uid
                    .updateChildren(map);   //* Tiến hành update cập nhất các giá trị trong map (map kiểu HashMap)
        });
    }

    /*Tạo hàm show hoặc hide password*/
    @SuppressLint("SetTextI18n")
    public void showHidePass(View view) {
        /*Tạo biến flag kiểu string để nhận giá trị từ textShowHidePass*/
        String flag = binding.textShowHidePass.getText().toString();
        /*Kiểm tra xem giá trị nhận vào của flag nếu là "SHOW" || "HIDE"*/
        if (flag.equals("SHOW")) {
            /*Thực hiện thay đổi inputType = "textPassword" thành kiểu text để có thể thấy được password*/
            binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            /*Đưa coin trỏ về cuối của edtPassword*/
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
            /*Thay đổi giá trị của textShowHide thành HIDE*/
            binding.textShowHidePass.setText("HIDE");
        }
        /*Kiểm tra ngược lại*/
        else {
            /*Thực hiện thay đổi inputType = "text" thành kiểu textPassword để ẩn password*/
            binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            /*Đưa coin trỏ về cuối của edtPassword*/
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
            /*Thay đổi giá trị của textShowHide thành HIDE*/
            binding.textShowHidePass.setText("SHOW");
        }
    }

    /*Tạo hàm để custom action bar*/
    public void customActionBar() {
        /*Set title cho action bar là "login"*/
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");

        /*Show nut back trên thanh action bar để có thể quay lại trang WelcomeActivity khi không muốn thực hiện thao tác đăng nhập */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /*Tạo hàm textWatcher lắng nghe sự kiện thay của văn bản*/
    TextWatcher textWatcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            /*Ẩn error đi khi thêm hoặc xóa kí tự*/
            binding.error.setVisibility(View.GONE);
        }
    };

    /*Thay đổi màu của action bar*/
    public void changeColorActionBar() {
        ActionBar actionBar = getSupportActionBar();
        /*Thực hiện set mã màu #0091FF cho action bar*/
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0091FF"));

        /*Thay đổi màu cho action bar với mã màu đã được set phía trên*/
        Objects.requireNonNull(actionBar).setBackgroundDrawable(colorDrawable);
    }

    /*Tạo hàm closeKeyboard()*/
    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /*Tạo hàm showKeyboard*/
    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

}