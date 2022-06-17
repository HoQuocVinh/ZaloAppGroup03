package hcmute.spkt.nhom03.finalproject.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OtpView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityVerifyOtpBinding;

public class VerifyOTPActivity extends AppCompatActivity {
    ActivityVerifyOtpBinding binding;   //* Khởi tạo binding cho ActivityVerifyOtp
    private FirebaseAuth auth; //* Khởi tạo auth
    private String verificationId;  //* Khởi tạo verificationId
    OtpView otpView;    //* Khởi tạo otpView --> sử dụng cho việc nhâp mã pin
    ProgressDialog dialog;  //* Khởi tạo ProgressDialog


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        //* Mapping lại auth
        auth = FirebaseAuth.getInstance();
        //* Mapping lại otpView
        otpView = findViewById(R.id.otp_view);
        //* Mapping lại dialog
        dialog = new ProgressDialog(this);
        //* Thực hiện set message cho dialog
        dialog.setMessage("Sending OTP...");
        //* setCancelable cho dialog
        dialog.setCancelable(false);
        //* shơ dialog
        dialog.show();
        //* Gọi hàm để showKeyboard
        showKeyboard();
        //* Sử dụng biến phoen để nhận giá trị sendPhoneToVerify được put từ SendOTPActivity
        String phone = getIntent().getStringExtra("sendPhoneToVerify");
        //* Thực hiênj gọi hàm sendVerificationCodeToUser và truyền giá trị phoen vừa nhận vào
        sendVerificationCodeToUser(phone);
    }

    //* Khởi tạo hàm showKeyboard
    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //* Khởi tạo callNextScreenFromOTP với sự kiện click
    public void callNextScreenFromOTP(View view) {
        //* Khởi tạo biến code để nhận giá trị từ otpView
        String code = Objects.requireNonNull(otpView.getText()).toString();
        //* Nếu code có giá trị null và số lượng kí tự trong code < 6
        if (code.isEmpty() || code.length() < 6) {
            //* Thực hiện setError cho otpView
            otpView.setError("Enter code...");
            //* Thực hiện focus lại vào otpView
            otpView.requestFocus();
            //* Gọi hàm return để kết thúc quá trình
            return;
        }
        //* Gọi hàm verifyCode và truyền vào code đã được tạo phía trên
        verifyCode(code);
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
                        Toast.makeText(VerifyOTPActivity.this, "Verification Completed", Toast.LENGTH_SHORT).show();
                        //* Thực hiện chuyển sang SetupProfileActivity
                        startActivity(new Intent(VerifyOTPActivity.this, SetupProfileActivity.class));
                    } else { //* Ngược lại
                        //* Show thông báo "Verification Not Completed! Try again." --> cho người dùng biết đã nhập sại
                        Toast.makeText(VerifyOTPActivity.this, "Verification Not Completed! Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //* Khởi taoị hàm sendVerificationCodeToUser
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
            ///* Thực hiện dimiss dialog
            dialog.dismiss();
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
                otpView.setText(code);
                //* Goi hàm kiểm tra OTp
                verifyCode(code);
            }
        }
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            //* Thưck hiện show lỗi lên màn hình
            Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            //* dimiss dialog
            dialog.dismiss();
        }
    };
}
