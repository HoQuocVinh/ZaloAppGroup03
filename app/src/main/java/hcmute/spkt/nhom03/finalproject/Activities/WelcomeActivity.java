package hcmute.spkt.nhom03.finalproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {
    ActivityWelcomeBinding binding;     //* Khởi tạo binding cho ActivityWelcome

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        //* Khởi tạo auth và get đến đường dẫn mặc định
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //* Kiẻm tra auth.getCurrentUser có != null không
        //* Nếu không khác null nghĩa là tài tài khoản đã được đăng nhập lên điện thoại
        if (auth.getCurrentUser() != null) {
            //* Thực hiện chuyển từ WelcomeActivity sang MainActivity
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            //* Sử dụng finish để không thế quay lại trang trước đó --> vd: trang login
            finish();
        }
        //* SetContentView
        setContentView(binding.getRoot());
        //* Đóng keyboard
        closeKeyboard();

    }

    //* Khởi tạo hàm đóng keyboard
    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    //* Tạo sự kiện click trực tiếp trong button register --> android:onClick="register"
    public void register(View view) {
        //* Thực hiện chuyển từ WelcomeActivity sang PhoneNumberActivity
        startActivity(new Intent(WelcomeActivity.this, PhoneNumberActivity.class));
    }

    //* Tạo sự kiện click trực tiếp trong button register --> android:onClick="login"
    public void login(View view) {
        //* Thực hiện chuyển từ WelcomeActivity sang LoginActivity
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
    }
}