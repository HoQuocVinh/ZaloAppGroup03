package hcmute.spkt.nhom03.finalproject.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.FragmentSettingBinding;

public class FragmentSetting extends Fragment {
    ProgressDialog dialog;

    FragmentSettingBinding binding; //* Khởi tạo binding cho FragmentSetting
    DatabaseReference databaseReference;    //* Khởi tạo databaseReference
    FirebaseStorage firebaseStorage;    //* Khởi tạo firebaseStorage
    FirebaseDatabase firebaseDatabase;  //* Khởi tạo irebaseDatabase
    StorageReference storageReference;  //* Khởi tạo storageReference
    FirebaseAuth auth;  //* Khởi tao auth
    FirebaseUser user;  //* Khởi taoị user

    Uri imageUri; //* Khởi tạo imageUri
    String urlImageProfile, nameProfile; //* Khởi tạo urlImageProfile và nameProfile

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        //* Gọi hàm  mapping() đã được tạo bên dưới
        mapping();
        //* Gọi hàm checkCurrentUser()
        checkCurrentUser();
        //* Tạo sự kiện click cho icon select image
        binding.changeImg.setOnClickListener(v -> addImage());
        //* Tắt trạng thái click cho btnSave
        binding.btnSave.setEnabled(false);
        //* Tạo sự kiện click cho btnSave
        binding.btnSave.setOnClickListener(v -> uploadProfileImage());
        return binding.getRoot();
    }

    //* Mapping lại các biến đã được khởi tạo ở phía trên
    private void mapping() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    private void addImage() {
        Intent intent = new Intent();
        //* Sử dụng để người dùng có thể select image từ library
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //* Set type
        intent.setType("image/*");
        //* Gọ hàm startActivityForResult để gọi máy ảnh
        startActivityForResult(intent, 25);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //* Nếu data && data.getData() != null
        if (data != null && data.getData() != null) {
            //* setImageURI cho imgAvt
            binding.imgAvt.setImageURI(data.getData());
            //* Gán giá trị cho imageUri bằng data.getData()
            imageUri = data.getData();
            //* Gọi hàm setEnableButtonSave()
            setEnableButtonSave();
        }
    }

    //* Tạo hàm setEnableButtonSave() --> sử dụng để thay đổi màu và xét trạng thái cho button save
    private void setEnableButtonSave() {
        //* Set backgroundTint cho btnSave -> màu blue_light được tạo trong color.xml
        binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue_light)));
        //* Bật trạng thái click của btnContinue*
        binding.btnSave.setEnabled(true);
    }
    //* Tạo hàm setDisplayButtonSave() --> sử dụng để thay đổi màu và xét trạng thái cho button save
    private void setDisableButtonSave() {
        //* Set backgroundTint cho btnSave -> màu gray_ccc được tạo trong color.xml
        binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_ccc)));
        //* Tắt trạng thái click của btnContinue*
        binding.btnSave.setEnabled(false);
    }

    //* Khởi tạo hàm uploadProfileImage() --> sử dụng cho việc upload image lên firebase và thay đổi giá trị profileImage trong current user
    private void uploadProfileImage() {
        //* Mapping lại dialog đã được khởi tạo cục bộ
        dialog = new ProgressDialog(getContext());
        //* Set text cho dialog để thông báo
        dialog.setMessage("Uploading image...");
        //* Tắt Cancel
        dialog.setCancelable(false);
        //* Show dialog
        dialog.show();
        //* Nêu imageUri != null
        if (imageUri != null) {
            //* Gán giá đường dẫn cho storageReference
            storageReference = firebaseStorage.getReference().child("Profiles").child(user.getUid());
            //* Thực hiện put hình ảnh lên firebase
            storageReference.putFile(imageUri).addOnCompleteListener(task -> {
                //* Nếu việc upload thành công
                if (task.isSuccessful()) {
                    //* Tắt thông báo show của dialog
                    dialog.dismiss();
                    //* Sử dụng storageReference và getDownloadUrl()
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        //* khởi tạo biến uimageUrl để lấy đường dẫn của hình vừa được upload
                        String imageUrl = uri.toString();
                        //* Sử dụng databaseReference để lấy đường dẫn trên firebase
                        databaseReference = FirebaseDatabase.getInstance().getReference("users");
                        //* Sử dụng databaseReference để tham chiếu đến children trong nut cha "user"
                        databaseReference.child(user.getUid()) //* Lấy giá trị uid của current user
                                .child("profileImage") //* Lấy giá tị của proifleImage
                                .setValue(imageUrl) //* Set giá trị cho imageUrl cho "profileImage"
                                .addOnSuccessListener(unused -> {
                                });
                    });
                    //* Thực hiện setDisableButtonSave() cho btnSave
                    setDisableButtonSave();
                }
            });
        }
    }

    private void checkCurrentUser() {
        //* Kiểm tra current user có tồn tại không
        //* Nếu curent user đã tồn tại
        if (user != null)
            //* Gọi hàm getProfileCurrentUser(userr) đã được khởi tạo bên dưới
            getProfileCurrentUser(user);
    }

    private void getProfileCurrentUser(FirebaseUser user) {
        //* Tạo phoneProfile kiểu string --> để lấy giá trị phone của user current
        String phoneProfile = user.getPhoneNumber();
        //* Tạo uid kiểu string --> để lấy giá trị uid của user current
        String uid = user.getUid();
        //* Sử dụng databaseReference và gét giá trị đến đường dẫn trên firebase
        databaseReference = firebaseDatabase.getReference("users").child(uid);
        //* Sử dụng databaseReference để lắng nghe sự kiện thay đổi
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //* Sử dụng model user để nhận giá trị từ realtime database
                User user = snapshot.getValue(User.class);
                //* Lấy giá trị name của curent user và gán vào nameProfile
                nameProfile = Objects.requireNonNull(user).getName();
                //* Lấy đường dẫn của hình ảnh và gán vào urlImageProfile
                urlImageProfile = user.getProfileImage();
                //* Gọi hàm setDataForCurrentUser và truyền vào các tham số đã được khởi tạo ở trên
                setDataForCurrentUser(urlImageProfile, nameProfile, phoneProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //* Khởi tọa hàm setDataForCurrentUser --> để gán giá trị vào trong fragmentsetting
    private void setDataForCurrentUser(String urlImageProfile, String nameProfile, String
            phoneProfile) {
        //* Sử dụng Glide để add image từ đường dẫn vào trong imgAvt được tạo trong fragment_setting.xml
        Glide.with(FragmentSetting.this)
                .load(urlImageProfile)
                .placeholder(R.drawable.img_avt)
                .into(binding.imgAvt);
        //* Gán giá trị nameProfile cho txtName
        binding.txtName.setText(nameProfile);
        //* Gán giá trị cho phoneProfile cho txtPhone
        binding.txtPhone.setText(phoneProfile);
    }
}