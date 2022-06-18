package hcmute.spkt.nhom03.finalproject.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import hcmute.spkt.nhom03.finalproject.Adapters.ContactAdapter;
import hcmute.spkt.nhom03.finalproject.Adapters.UsersAdapter;
import hcmute.spkt.nhom03.finalproject.Models.Contact;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityContactBinding;

public class ContactActivity extends AppCompatActivity {
    ActivityContactBinding binding;     //* Khởi tạo binding
    Contact contact;    //* Khởi tạo model Contact --> dùng để lưu trữ contact của điện thaoị
    ContactAdapter contactAdapter;  //* Khởi tạo contactAdapter --> dùng để cập nhật lịa các sự thay đổi
    ArrayList<Contact> contacts;    //* Khởi taoị contacts kiểu arrayList --> dùng để chứa tất cả các dữ liệu đã dược lưu vào model contact
    FirebaseDatabase database;  //* Khởi tạo database --> dùng để truy xuất đến đường dẫn trên firebase
    DatabaseReference reference;    //* Khởi tạo reference
    UsersAdapter usersAdapter;  //* Khỏi tạo userAdapter --> dùng để cập nhật lại các sự thay đổi trong model User
    ArrayList<User> users;  //* Khở tạo users kiêu Array list đùng để chứa dữ liệu được lưu trong model User
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //* Set actionbar với "toolbar", "toolbar" --> được design trong xml
        setSupportActionBar(binding.toolbar);
        //* Set sẹ kiện click cho btnBack
        //* Có tác dụng quay lại MainActivity
        binding.btnBack.setOnClickListener(v -> finish());
        //* Mapping contacts
        contacts = new ArrayList<>();
        //* Mapping lại database và nhận dữ liệu từ Firebase
        database = FirebaseDatabase.getInstance();
        //* Mapping reference và nhận dữ liệu từ Firebase với đường dẫn dược gán "users"
        reference = database.getReference("users");
        //* Mapping user
        users = new ArrayList<>();
        //* Mapping userAdapter
        usersAdapter = new UsersAdapter(this, users);
        //* Gọi hàm kiểm tra cấp quyền
        checkPermission();
    }

    private void checkPermission() {
        //* Kiểm tra tình trạng
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            //* Khi chưa được cấp quyền
            //* Thực hiện yêu cầu xin cấp quyền
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        else
            //* Khi đã được cấp quyền
            //* Create method
            getContactList();
    }

    private void getContactList() {
        //* Khở tạo uri
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //* Sắp xếp các name theo thứ tự tăng dần của bảng mã ASC
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        //* Khởi tạo cursor
        @SuppressLint("Recycle") Cursor cursor = getContentResolver()
                .query(uri, null, null, null, sort);
        //* Kiểm tra tìnfh trạng
        if (cursor.getCount() > 0) {
            //* Khi số lượng lớn hơn 0
            while (cursor.moveToNext()) {
                //* Di chuyển con trỏ đến vị trí tiếp theo
                //* Get contact id
                @SuppressLint("Range") String id =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //* Get contact name
                @SuppressLint("Range") String name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //* Khởi tạo phone uri
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //* Khởi tạo selection
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                //* Khởi tạo phone cursor
                @SuppressLint("Recycle") Cursor phoneCursor =
                        getContentResolver()
                                .query(uriPhone, null, selection, new String[]{id}, null);
                //* Kiểm tra tình trạng
                if (phoneCursor.moveToNext()) {
                    //* Khi phoneCursor di chuyển đến vị trí tiếp theo
                    @SuppressLint("Range") String number = phoneCursor
                            .getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //* khởi tạo contact model
                    contact = new Contact();
                    //* Set name
                    contact.setName(name);
                    //* Set phone number
                    contact.setPhoneNo(number);
                    //* Add model in array list
                    contacts.add(contact);
                    //* Close phone cursor
                    phoneCursor.close();
                }
            }
            //* Close cursor
            cursor.close();
        }
        //* Set layout manager
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //* Khởi tạo adapter
        contactAdapter = new ContactAdapter(this, contacts);
        //* Set adapter
        binding.recyclerView.setAdapter(contactAdapter);
    }
}