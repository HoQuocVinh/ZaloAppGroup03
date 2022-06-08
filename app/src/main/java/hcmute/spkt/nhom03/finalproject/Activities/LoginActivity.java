package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Adapters.UsersAdapter;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    ArrayList<User> users;
    FirebaseDatabase database;
    FirebaseAuth auth;
    DatabaseReference reference;
    UsersAdapter usersAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapping();
        binding.edtPhone.requestFocus(); /*Focus vào edtPhone*/
        showKeyboard(); /*Show bàng phím lên*/
        changeColorActionBar(); /*Thực hiện thay đổi màu của action bar*/
        customActionBar();  /*Tinh chỉnh lại action bar*/
        binding.edtPhone.addTextChangedListener(textWatcher);   /*Add sự kiện nghe khi thay đổi văn bản cho edtPhone*/
        binding.edtPassword.addTextChangedListener(textWatcher);    /*Add sự kiện nghe khi thay đổi văn bản cho edtPhone*/
    }

    private void mapping() {
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        database =FirebaseDatabase.getInstance();
        reference = database.getReference("users");
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

                usersAdapter.notifyDataSetChanged();    /*Commit lại sự thay đổi*/
                for (User user : users) {   /*Chạy vòng lăp for trên ArrayList*/
                    String phoneAuth = user.getPhoneNumber();   /*Tạo một biến phoneAuth kiểu string để nhận giá trị phoneNumber*/
                    String passwordAuth = user.getPassword();   /*Tạo một biến passwordAuth kiểu string để nhận giá trị password*/
                    String nameAuth = user.getName();

                    if (phone.equals(phoneAuth) && password.equals(passwordAuth)) {
                        binding.progressBar.setVisibility(View.INVISIBLE);  /* Ẩn progress bar đi để show lại button login*/
                        binding.btnLogin.setVisibility(View.VISIBLE);   /* Show button login */
//                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                        /*Gọi hàm startActivity() để chuyển từ trang hiện tại (login) vào trong chủ (main)*/
                        binding.error.setVisibility(View.GONE); /*Ẩn thông báo error đi*/
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        /*Gọi hàm closeKeyboard() để ẩn bàng phím đi trong quá trình chuyển từ LoginActivity sang MainActivity*/
                        closeKeyboard();
                        finish();
                        break;
                    } else {
                        binding.progressBar.setVisibility(View.INVISIBLE);  /* Ẩn progress bar đi để show lại button login*/
                        binding.btnLogin.setVisibility(View.VISIBLE);   /* Show button login */
                        binding.error.setVisibility(View.VISIBLE);  /*Show thông báo error bằng TextView đã được design trong file activity_login.xml*/
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
    TextWatcher textWatcher = new TextWatcher() {
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