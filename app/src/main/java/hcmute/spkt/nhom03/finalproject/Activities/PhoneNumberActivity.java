package hcmute.spkt.nhom03.finalproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityPhoneNumberBinding;
public class PhoneNumberActivity extends AppCompatActivity {
    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        auth = FirebaseAuth.getInstance();
//
//        if(auth.getCurrentUser() != null){
//            startActivity(new Intent(PhoneNumberActivity.this, MainActivity.class));
//            finish();
//        }

        binding.btnContinue.setEnabled(false);
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
        showKeyboard();
        binding.edtPhone.requestFocus();
        checkEnterView();
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    public void checkEnterView() {
        binding.edtPhone.addTextChangedListener(textWatcher);
    }


    public void switchTabSendOTP(View view) {
        String phoneNumber = binding.edtPhone.getText().toString();
        Intent intent = new Intent(this, SendOTPActivity.class);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
        closeKeyboard();
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String phone = binding.edtPhone.getText().toString().trim();
            if (!phone.isEmpty()) {
                binding.imgButtonClose.setVisibility(View.VISIBLE);
                if (phone.length() == 12) {
                    binding.btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.neon_blue)));
                    binding.btnContinue.setEnabled(true);
                } else {
                    binding.btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_ccc)));
                    binding.btnContinue.setEnabled(false);
                }
            } else {
                binding.imgButtonClose.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void clearText(View view) {
        binding.edtPhone.setText("");
    }
}