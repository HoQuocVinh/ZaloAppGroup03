package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import hcmute.spkt.nhom03.finalproject.R;
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

        /*Tắt trạng thái click của btnContinue*/
        binding.btnContinue.setEnabled(false);

        /*Lăng nghe sự thay đổi trên edtName*/
        binding.edtName.addTextChangedListener(textWatcher);

        /*Lăng nghe sự thay đổi trên edtPassword1*/
        binding.edtPassword1.addTextChangedListener(textWatcher);

        /*Lăng nghe sự thay đổi trên edtPassword2*/
        binding.edtPassword2.addTextChangedListener(textWatcher);

        /*Gọi hàm clickShowPass() đã được khởi tạo bên dưới*/
        clickShowHidePass();
    }

    @SuppressLint("SetTextI18n")
    private void clickShowHidePass() {

        /*Tạo sự kiện click cho txtShowHide1*/
        binding.txtShowHide1.setOnClickListener(v -> {
            /*Tạo biến flag kiểu string và gán giá trị của nó bằng giá trị của txtShowHide1*/
            String flag = binding.txtShowHide1.getText().toString();

            /*Kiểm tra giá trị của biến fllag nếu kết quá trả về là SHOW*/
            if (flag.equals("SHOW")) {
                /*Thực hiện thay đổi inputType = "textPassword" thành kiểu text để có thể thấy được password*/
                binding.edtPassword1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                /*Đưa coin trỏ về cuối của edtPassword1*/
                binding.edtPassword1.setSelection(binding.edtPassword1.getText().length());
                /*Thay đổi giá trị của txtShowHide1 thành HIDE*/
                binding.txtShowHide1.setText("HIDE");
            }
            /*Kiểm tra ngược lại*/
            else {
                /*Thực hiện thay đổi inputType = "text" thành kiểu textPassword để ẩn password*/
                binding.edtPassword1.setTransformationMethod(PasswordTransformationMethod.getInstance());

                /*Đưa coin trỏ về cuối của edtPassword1*/
                binding.edtPassword1.setSelection(binding.edtPassword1.getText().length());

                /*Thay đổi giá trị của txtShowHide1 thành HIDE*/
                binding.txtShowHide1.setText("SHOW");
            }
        });
        /*Hoạt động tương tự như sự kiện click vào txtShowHid1*/
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

    /*Tạo hàm lắng nghe sự thay đổi môi khi thêm hoặc xóa một kí tự trong name, password, confirmpassword*/
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtName*/
            String name = binding.edtName.getText().toString().trim();

            /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtPassword1*/
            String password = binding.edtPassword1.getText().toString().trim();

            /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtConfirmPassword*/
            String confirmPassword = binding.edtPassword2.getText().toString().trim();

            /*Kiểm tra xem name, password, confirmpassword có rỗng không*/
            if (name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                /*Thực hiện set màu cho btnContinue thành gray_ccc (đã được tạo trong color)*/
                binding.btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_ccc)));

                /*Tắt trạng thái click của btnContinue*/
                binding.btnContinue.setEnabled(false);
            }
            /*Nếu name, passsword, confirmpassword có giá trị*/
            else {
                /*Thực hiện set màu của btnContinue thành neon_blue (đã được tạo trong color)*/
                binding.btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.neon_blue)));

                /*Mở trạng thái click của btnContinue*/
                binding.btnContinue.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            /*Ẩn thông báo error khi xóa hoặc thêm kí tự vào edtPassword || edtConfirmPassword*/
            binding.errorPassword.setVisibility(View.GONE);
        }
    };

    public void setupProfile(View view) {
        /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtName*/
        String name = binding.edtName.getText().toString().trim();

        /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtPassword1*/
        String password = binding.edtPassword1.getText().toString().trim();

        /*Tạo biến name kiểu string và gán gia trị bằng giá trị trong edtPassword2*/
        String confirmPassword = binding.edtPassword2.getText().toString().trim();

        /*Kiểm tra xem password, và conFirmPassword có trả về true hay false*/
        if(!password.equals(confirmPassword)) {
            /*Show thông báo error password*/
            binding.errorPassword.setVisibility(View.VISIBLE);

            /*Thực hiện focus đưa con trỏ vào edtPassword2*/
            binding.edtPassword2.requestFocus();

            /*Thực hiện return để kết thúc hàm setup profile*/
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
                                String phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
                                String name = binding.edtName.getText().toString().trim();
                                String password = binding.edtPassword1.getText().toString().trim();
                                User user = new User(uid, name, phone, imageUrl, password);
                                database.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user)
                                        .addOnSuccessListener(unused -> {
//                                                dialog.dismiss();
                                            binding.progressBar.setVisibility(View.INVISIBLE);
                                            binding.btnContinue.setVisibility(View.VISIBLE);
                                            startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                                            finish();
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
                            binding.progressBar.setVisibility(View.INVISIBLE);
                            binding.btnContinue.setVisibility(View.VISIBLE);
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