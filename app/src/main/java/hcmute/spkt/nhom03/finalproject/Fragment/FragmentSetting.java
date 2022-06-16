package hcmute.spkt.nhom03.finalproject.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.Calendar;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.FragmentSettingBinding;

public class FragmentSetting extends Fragment {
    ProgressDialog dialog;

    FragmentSettingBinding binding;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;
    StorageReference storageReference;
    FirebaseAuth auth;
    FirebaseUser user;

    Calendar calendar;

    Uri imageUri;
    String urlImageProfile, nameProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        mapping();
        checkCurrentUser();
        binding.changeImg.setOnClickListener(v -> addImage());
        return binding.getRoot();
    }


    private void mapping() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        calendar = Calendar.getInstance();
    }

    private void addImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 25);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            binding.imgAvt.setImageURI(data.getData());
            imageUri = data.getData();
        }
    }

    private void uploadProfileImage() {
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        if (imageUri != null) {
            storageReference = firebaseStorage.getReference().child("Profile").child(user.getUid());
        }
    }
    private void checkCurrentUser() {
        if (user != null)
            getUserProfile(user);
    }

    private void getUserProfile(FirebaseUser user) {
        String phoneProfile = user.getPhoneNumber();
        String uid = user.getUid();
        databaseReference = firebaseDatabase.getReference("users").child(uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                nameProfile = Objects.requireNonNull(user).getName();
                urlImageProfile = user.getProfileImage();
                setDataForCurrentUser(urlImageProfile, nameProfile, phoneProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setDataForCurrentUser(String urlImageProfile, String nameProfile, String phoneProfile) {
        Glide.with(FragmentSetting.this)
                .load(urlImageProfile)
                .placeholder(R.drawable.img_avt)
                .into(binding.imgAvt);
        binding.txtName.setText(nameProfile);
        binding.txtPhone.setText(phoneProfile);
    }
}