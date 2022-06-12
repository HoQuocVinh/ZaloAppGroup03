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
    ActivityContactBinding binding;
    Contact contact;
    ContactAdapter contactAdapter;
    ArrayList<Contact> contacts;
    FirebaseDatabase database;
    DatabaseReference reference;
    UsersAdapter usersAdapter;
    ArrayList<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.btnBack.setOnClickListener(v -> finish());

        contacts = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);

        checkPermission();
    }

    private void checkPermission() {
        //* Check condition
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            //* When permission is not granted
            //* Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        else
            //* When permission is granted
            //* Create method
            getContactList();
    }

    private void getContactList() {
        //* Initialize uri
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //* Sort by ascending
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "ASC";
        //* Initialize cursor
        @SuppressLint("Recycle") Cursor cursor = getContentResolver()
                .query(uri, null, null, null, sort);
        //* Check condition
        if (cursor.getCount() > 0) {
            //* When count is grater than 0
            while (cursor.moveToNext()) {
                //* Cursor move to next
                //* Get contact id
                @SuppressLint("Range") String id =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //* Get contact name
                @SuppressLint("Range") String name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //* Initialize phone uri
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //* Initialize selection
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                //* Initialize phone cursor
                @SuppressLint("Recycle") Cursor phoneCursor =
                        getContentResolver()
                                .query(uriPhone, null, selection, new String[]{id}, null);
                //* Check condition
                if (phoneCursor.moveToNext()) {
                    //* When phone cursor move to next
                    @SuppressLint("Range") String number = phoneCursor
                            .getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //* Initialize contact model
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
        //* Initialize adapter
        contactAdapter = new ContactAdapter(this, contacts);
        //* Set adapter
        binding.recyclerView.setAdapter(contactAdapter);
    }

}