package hcmute.spkt.nhom03.finalproject.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import hcmute.spkt.nhom03.finalproject.databinding.ActivitySendOtpBinding;

public class SendOTPActivity extends AppCompatActivity {
    ActivitySendOtpBinding binding;
    private final static String TAG = SendOTPActivity.class.getName();
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone");
        binding.txtPhone.setText(phone);
        binding.btnGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoVerifyOTPActivity(phone);
            }
        });
    }

    private void gotoVerifyOTPActivity(String phoneNumber) {
        Intent intent = new Intent(this, VerifyOTPActivity.class);
        intent.putExtra("sendPhoneToVerify", phoneNumber);
        startActivity(intent);
    }
 }
