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
    ActivityWelcomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        FirebaseAuth auth= FirebaseAuth.getInstance();
//        if(auth.getCurrentUser()!=null){
//            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
//        }
        setContentView(binding.getRoot());
        mapping();
        closeKeyboard();

    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void mapping() {
//        imageSlider = (ImageSlider) findViewById(R.id.img_slider);
    }

    public void register(View view) {
        startActivity(new Intent(WelcomeActivity.this, PhoneNumberActivity.class));
    }

    public void login(View view) {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
    }
}