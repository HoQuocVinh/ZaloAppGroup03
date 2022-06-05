package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        binding.edtPhone.requestFocus();
        showKeyboard();
        setContentView(binding.getRoot());
        changeColorActionBar();
        customActionBar();
//        mapping();
//        blurBackground();
//        checkEnterTextView();
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void customActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void changeColorActionBar() {
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0091FF"));
        Objects.requireNonNull(actionBar).setBackgroundDrawable(colorDrawable);
    }

//
//    public void hidePass(View view) {
//        edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
//        edtPassword.setSelection(edtPassword.getText().length());
//        imgButtonShow.setVisibility(view.VISIBLE);
//        imgButtonHide.setVisibility(view.INVISIBLE);
//    }
//
//    public void blurBackground() {
//        Glide.with(this).load(R.drawable.bg_chery_blossoms)
//                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
//                .into(imgViewBackground);
//    }
//
//    public void checkEnterTextView() {
//        edtPhone.addTextChangedListener(textWatcher);
//        edtPassword.addTextChangedListener(textWatcher);
//    }
//
//    private final TextWatcher textWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            String phone = edtPhone.getText().toString();
//            String password = edtPassword.getText().toString();
//            if (!phone.isEmpty() && !password.isEmpty()) {
//                btnLogin.setEnabled(true);
//                btnLogin.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_53)));
//            } else {
//                btnLogin.setEnabled(false);
//                btnLogin.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_ccc)));
//            }
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//
//        }
//    };

//    public void mapping() {
//        imgButtonShow = (ImageButton) findViewById(R.id.imgButtonShow_login);
//        imgButtonHide = (ImageButton) findViewById(R.id.imgButtonHide_login);
//
//        edtPassword = (EditText) findViewById(R.id.edtPassword_login);
//        edtPhone = (EditText) findViewById(R.id.edtPhoneNumber_login);
//
//        btnLogin = (Button) findViewById(R.id.btnLogin);
//
//        imgViewBackground = (ImageView) findViewById(R.id.imgViewBackground_login);
//    }

    public void home(View view) {
        startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
    }

    @SuppressLint("SetTextI18n")
    public void showHidePass(View view) {
        String flag = binding.textShowHidePass.getText().toString();
        if (flag.equals("SHOW")) {
            binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
            binding.textShowHidePass.setText("HIDE");
        } else {
            binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
            binding.textShowHidePass.setText("SHOW");
        }
    }
}