package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.databinding.ActivitySetupProfileBinding;

public class SetupProfileActivity extends AppCompatActivity {
    private ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapping();
        clickShowHidePass();
    }

    @SuppressLint("SetTextI18n")
    private void clickShowHidePass() {
        binding.txtShowHide1.setOnClickListener(v -> {
            String flag = binding.txtShowHide1.getText().toString();
            if (flag.equals("SHOW")) {
                binding.edtPassword1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.edtPassword1.setSelection(binding.edtPassword1.getText().length());
                binding.txtShowHide1.setText("HIDE");
            } else {
                binding.edtPassword1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.edtPassword1.setSelection(binding.edtPassword1.getText().length());
                binding.txtShowHide1.setText("SHOW");
            }
        });
        binding.txtShowHide2.setOnClickListener(v -> {
            String flag = binding.txtShowHide2.getText().toString();
            if (flag.equals("SHOW")) {
                binding.edtPassword2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.edtPassword2.setSelection(binding.edtPassword2.getText().length());
                binding.txtShowHide2.setText("HIDE");
            } else {
                binding.edtPassword2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.edtPassword2.setSelection(binding.edtPassword2.getText().length());
                binding.txtShowHide2.setText("SHOW");
            }
        });
    }

    private void mapping() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }


    public void setupProfile(View view) {
        /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtName*/
        String name = binding.edtName.getText().toString().trim();

        /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtPassword1*/
        String password = binding.edtPassword1.getText().toString().trim();

        /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtConfirmPassword*/
        String confirmPassword = binding.edtPassword2.getText().toString().trim();

        /* Kiểm tra xem nếu edtName rỗng thì sẽ trả về error thông báo cho người dùng*/
        if (name.isEmpty()) {
            binding.edtName.setError("Please type a name");
            return;
        /* Kiểm tra xem nếu edtPassword rỗng thì sẽ trả về error thông báo cho người dùng*/
        }if (password.isEmpty()) {
            binding.edtPassword1.setError("Please type a name");
            return;
        }
        /* Kiểm tra xem nếu edtConfirmPassword rỗng thì sẽ trả về error thông báo cho người dùng*/
        if (confirmPassword.isEmpty()) {
            binding.edtPassword2.setError("Please type a name");
            return;
        }

        /* Hiện progress bar khi click vào button Setup Profile*/
        binding.progressBar.setVisibility(View.VISIBLE);

        /* Ẩn button continue đi*/
        binding.btnContinue.setVisibility(View.INVISIBLE);

        /* Kiểm tra xem thông tin setup đưa vào có hình ảnh không*/
        /* Xử lý khi có hình ảnh*/
        if (selectedImage != null) {
            StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
            reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                String uid = auth.getUid();
                                String phone = auth.getCurrentUser().getPhoneNumber();
                                String name = binding.edtName.getText().toString().trim();
                                String password = binding.edtPassword1.getText().toString().trim();
                                User user = new User(uid, name, phone, imageUrl, password);
                                database.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
//                                                dialog.dismiss();
                                                startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        });
                    }
                }
            });
        }
        /*Xử lý khi không có hình ảnh*/
        else {
            String uid = auth.getUid();
            String phone = auth.getCurrentUser().getPhoneNumber();
            User user = new User(uid, name, phone, "No image", password);
            database.getReference()
                    .child("users")
                    .child(uid)
                    .setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
//                            dialog.dismiss();
                            binding.progressBar.setVisibility(View.VISIBLE);
                            binding.btnContinue.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                            finish();
                        }
                    });
        }
    }

    public void addImage(View view) {
        closeKeyboard();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 45);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                binding.imgViewProfile.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}