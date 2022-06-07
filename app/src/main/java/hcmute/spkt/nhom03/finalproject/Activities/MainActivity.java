package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Adapters.UsersAdapter;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<User> users;
    UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*Gọi hàm changeColorActionBar() đã được khởi tạo bên dưới đ*/
        changeColorActionBar();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            HashMap <String, Object> map = new HashMap<>();
            map.put("token", token);
            database.getReference()
                    .child("users")
                    .child(Objects.requireNonNull(auth.getUid()))
                    .updateChildren(map);
//            Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        });

        users = new ArrayList<>();
        usersAdapter = new

                UsersAdapter(this, users);
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(usersAdapter);

        database.getReference().

                child("users").

                addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            if (!Objects.requireNonNull(user).getUid().equals(FirebaseAuth.getInstance().getUid()))
                                users.add(user);
                        }
                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        designSearchView(menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void designSearchView(Menu menu) {
        /* Mapping menuItem với id là nav_search*/
        MenuItem menuItem = menu.findItem(R.id.nav_search);

        /*Sử dụng SearchManager để tạo thanh search trên action bar*/
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();

        /*Set background cho thanh search thành màu trắng*/
        searchView.setBackgroundColor(Color.WHITE);

        MenuItem menuItem1 = menu.findItem(R.id.nav_add);
        /*Custom lại thanh search (vd: radius)*/
        searchView.setBackgroundResource(R.drawable.custom_search_view);

        /*Tạo hint cho người dùng có thể biết khi sử dụng search view */
        searchView.setQueryHint("Search friends, message");
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        /*Tăng max độ dài cho search view*/
        searchView.setMaxWidth(Integer.MAX_VALUE);

        /*Mapping editText với id search_src_text*/
        EditText editText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        /*Thay đổi mày chữ trong search view thành màu đen*/
        editText.setTextColor(Color.BLACK);

        /*Thay đổi màu của hint trong search view thành màu xám*/
        editText.setHintTextColor(Color.GRAY);

        /*Đưa icon search vào phía bên trái của search view trên action bar*/
        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_gray, 0, 0, 0);

        /*Set pađing cho icon vừa được đưa vào trong search view*/
        editText.setCompoundDrawablePadding(10);
    }

    public void changeColorActionBar() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        /*Thay đổi màu của actionbar vớ mã màu #0091FF*/
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0091FF"));
        Objects.requireNonNull(actionBar).setBackgroundDrawable(colorDrawable);
//        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Online");
    }

//    @Override
//    protected void onStop() {
//        String currentId = FirebaseAuth.getInstance().getUid();
//        database.getReference().child("presence").child(currentId).setValue("Offline");
//        super.onStop();
//    }
}