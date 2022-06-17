package hcmute.spkt.nhom03.finalproject.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Adapters.UsersAdapter;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.FragmentChatBinding;

public class FragmentUsers extends Fragment {
    FragmentChatBinding binding;
    ArrayList<User> users;      //* Khởi tạo users Kiểu ArrrayList --> lưu trữ thông tin từ model user
    UsersAdapter usersAdapter;  //* Khởi tạo userAdapter --> dùng để cập nhật lại sự thay đổi
    RecyclerView myRecyclerView;    //* Khởi tạo myRecyclerView dùng để load user vào trong FragmentUser
    FirebaseDatabase database;      //* Khởi tạo database --> dùng để truy xuất đến dường dẫn trên firebase
    FirebaseMessaging messaging;    //* Khởi tạo messaging --> dùng để put token cho user
    FirebaseAuth auth;  //* Khởi tạo auth
    FirebaseUser userCurrent;   //* Khởi tạo userCurrent

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_chat, container, false);
        //* Mapping lịa RecyclerView
        myRecyclerView = v.findViewById(R.id.recycler_view);
        //* Set layout cho myRecyclerView
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //* setAdapter để cập nhật các sự thay đổi trên myRecyclerView
        myRecyclerView.setAdapter(usersAdapter);
        //* Trả về view
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //* Load hàm mapping đã được tạo bên dưới vòa
        mapping();
        //* Load hàm token vào
//        token(messaging);
        //* Thực hiện load user vào myRecyclerView
        loadUser(userCurrent);
    }


    //* KHởi tạo hàm mapping
    private void mapping() {
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), users);
        messaging = FirebaseMessaging.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userCurrent = auth.getCurrentUser();
    }

    //* Khởi tạo hàm token
//    private void token(FirebaseMessaging messaging) {
//        //* Sử dụng messaging để getToken
//        messaging.getToken().addOnSuccessListener(token -> {
//            //* Tạo map kiểu HashMap
//            HashMap<String, Object> map = new HashMap<>();
//            //* put token vào trong biến "token"
//            map.put("token", token);
//            //* Sử dụng database và truy xuất đến đường dẫn đã được cung cấp bến dưới
//            database.getReference()
//                    .child("users") //* Thông qua nút users
//                    .child(Objects.requireNonNull(auth.getUid())) //* Thông qua uid
//                    .updateChildren(map);   //* Tiến hành update cập nhất các giá trị trong map (map kiểu HashMap)
//        });
//    }

    //* Load user vào myRecyclerView
    private void loadUser(FirebaseUser userCurrent) {
        //* Sử dụng database và truy xuất dến đường dẫn được cung cấp
        database.getReference()
                .child("users") //* Thông quq node users
                .addValueEventListener(new ValueEventListener() { //* Thực hiện lắng nghe
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //* Clear đẻ xóa bỏ hết các dữ liệu chứa trong users
                        users.clear();
                        //* Sử dụng vòng lặp DataSnapshot đế quét dữ liệu
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            //* Sử dụng model user để nhận giá trị của dữ liệu
                            User user = snapshot1.getValue(User.class);
//                            if (!Objects.requireNonNull(userCurrent).getUid().equals(auth.getUid()))
                            //* Nếu tìm thấy một uid mà bằng với currentId thì bỏ qua không load vào myRecyclerView
                            if (!Objects.requireNonNull(user).getUid().equals(auth.getUid()))
                            //* Add model user vào trong ArrayList
                            users.add(user);
                        }
                        //* Sử dụng userAdapter để cập nhật lại sự thay đổi
                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //* Khởi tọa hàm onResum
    @Override
    public void onResume() {
        super.onResume();
        //* Tìm gán giá trị của currentId
        String currentId = auth.getUid();
        //* Thực hiện set giá trị cho node presence với child của nó là currentId và giá trị của currentId là "Online"
        if (currentId != null)
            database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Online");
    }

    @Override
    public void onPause() {
        super.onPause();
        //* Tìm gán giá trị của currentId
        String currentId = auth.getUid();
        //* Thực hiện set giá trị cho node presence với child của nó là currentId và giá trị của currentId là "Offline"
        if (currentId != null)
            database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Offline");
    }
}