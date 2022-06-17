package hcmute.spkt.nhom03.finalproject.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import hcmute.spkt.nhom03.finalproject.databinding.ActivitySendOtpBinding;

public class SendOTPActivity extends AppCompatActivity {
    ActivitySendOtpBinding binding; //* Khởi tạo binding cho ActivitySendOTP
    FirebaseAuth auth;  //* Khởi tạo auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //* Mapping lại auth và gán giá trị mặc định cho nó
        auth = FirebaseAuth.getInstance();
        //* Khởi tạo intent để nhận dữ liệu được put từ PhonNumberActivity
        Intent intent = getIntent();
        //* Tạo biến phone kiểu String để nhận giá trị được put
        String phone = intent.getStringExtra("phone");
        //* Gán giá trị được put vào txtPhone
        binding.txtPhone.setText(phone);
        //* Set sự kiện click cho btnGetOTP
        binding.btnGetOTP.setOnClickListener(v -> gotoVerifyOTPActivity(phone));
    }

    //* Khởi tao jhàm gotoVerifyOTPActivity và truyền vào cho nó một chuỗi kiểu String
    private void gotoVerifyOTPActivity(String phoneNumber) {
        //* Khởi tạo intent
        Intent intent = new Intent(this, VerifyOTPActivity.class);
        //* Put giá trị phoneNumber vào "sendPhoneToVerify" để VerifyOTPActivity sử dụng
        intent.putExtra("sendPhoneToVerify", phoneNumber);
        //* Thực hiện startActivity
        startActivity(intent);
    }
}
