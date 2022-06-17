package hcmute.spkt.nhom03.finalproject.Activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Fragment.FragmentContact;
import hcmute.spkt.nhom03.finalproject.Fragment.FragmentSetting;
import hcmute.spkt.nhom03.finalproject.Fragment.FragmentUsers;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //* Gọi hàm changeColorActionBar() đã được khởi tạo bên
        changeColorActionBar();
        replaceFragment(new FragmentUsers());
        //* Set sẹ kiện click cho từng item
        binding.navBottom.setOnItemSelectedListener(item ->{
            //* Sử dụng switch case
            switch (item.getItemId()){
                //* Nếu click vào item nav_message ở dới navigation bottom
                case R.id.nav_message:  //* Nếu id của item là nav_message
                    //* Thực hiện replace fragmentUser
                    replaceFragment(new FragmentUsers());
                    //* Thoát khỏi switch case
                    break;
                case R.id.contacts: //* Nếu id của item là contacts
                    //* Thực hiện replace fragmentContact
                    replaceFragment(new FragmentContact());
                    //* Thoát khỏi switch case
                    break;
                case R.id.setting:  //* Nếu id của tem là setting
                    //* Thực hiện replace fragmentSetting
                    replaceFragment(new FragmentSetting());
                    //* Thoát khỏi switch case
                    break;
            }
            //* Kết thục sự kiện click
            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        //*Khỏi tạo fragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        //* Khởi tạo fragmentTransaction
        FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
        //* Thực hiện replace fragment "fragment_container --> design trong xml"
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        //* Commit để hoàn thành sự thay đổi
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //* get đến item có id nav_menu
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        //* Thực hiện design search view
        designSearchView(menu);
        //* Trả về onCreateOptionsMenu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //* Tạo biến id để nhận giá trị item.getItemId()
        int id = item.getItemId();
        //* nếu id == R.id.nav_add_friend
        if(id == R.id.nav_add_friend){
            //* StartActivty ContactActivity
            startActivity(new Intent(MainActivity.this, ContactActivity.class));
            //* Trả về giá trị true
            return true;
        }
        //* Trả lại onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item);
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
    }
}