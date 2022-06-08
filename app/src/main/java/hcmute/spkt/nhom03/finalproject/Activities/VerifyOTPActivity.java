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
    ActivityVerifyOtpBinding binding;
    private static final String TAG = VerifyOTPActivity.class.getName();
    private FirebaseAuth auth;
    private String verificationId;
    OtpView otpView;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        auth = FirebaseAuth.getInstance();
        otpView = findViewById(R.id.otp_view);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        showKeyboard();

        String phone = getIntent().getStringExtra("sendPhoneToVerify");
//        FirebaseAuth.getInstance().getFirebaseAuthSettings()
//                .setAppVerificationDisabledForTesting(true);
        sendVerificationCodeToUser(phone);
//        OTP();
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void callNextScreenFromOTP(View view) {
        String code = Objects.requireNonNull(otpView.getText()).toString();
        if (code.isEmpty() || code.length() < 6) {
            otpView.setError("Enter code...");
            otpView.requestFocus();
            return;
        }
        verifyCode(code);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyOTPActivity.this, "Verification Completed", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifyOTPActivity.this, SetupProfileActivity.class));
                    } else {
                        Toast.makeText(VerifyOTPActivity.this, "Verification Not Completed! Try again.", Toast.LENGTH_SHORT).show();
//                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                        }
                    }
                });
    }

    private void sendVerificationCodeToUser(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            dialog.dismiss();
                verificationId = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
           final String code = phoneAuthCredential.getSmsCode();
            showKeyboard();
            if (code != null) {
                otpView.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    };

//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null) {
//            startActivity(new Intent(VerifyOTPActivity.this, SetupProfileActivity.class));
//            finish();
//        }
//    }


   //    }
//    private void OTP() {
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//        Intent intent = getIntent();
//        String phone = getIntent().getStringExtra("sendPhoneToVerify");
//        auth = FirebaseAuth.getInstance();
//        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
//                .setPhoneNumber(phone)
//                .setTimeout(90L, TimeUnit.SECONDS)
//                .setActivity(VerifyOTPActivity.this)
//                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                    @Override
//                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                        String code = phoneAuthCredential.getSmsCode();
//                        if (code != null) {
//                            otpView.setText(code);
//                            verifyCode(code);
//                        }
//                    }
//
//                    @Override
//                    public void onVerificationFailed(@NonNull FirebaseException e) {
//
//                    }
//
//                    @Override
//                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                        super.onCodeSent(s, forceResendingToken);
//                        dialog.dismiss();
//                        verificationId = s;
//                    }
//                }).build();
//
//
//        auth.setLanguageCode("fr");
//
//        PhoneAuthProvider.verifyPhoneNumber(options);
//        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
//            @Override
//            public void onOtpCompleted(String otp) {
//                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
//
//                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            startActivity(new Intent(VerifyOTPActivity.this, SetupProfileActivity.class));
//                            finishAffinity();
//                        } else {
//                            Toast.makeText(VerifyOTPActivity.this, "Failed in", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        });
//    }
}
