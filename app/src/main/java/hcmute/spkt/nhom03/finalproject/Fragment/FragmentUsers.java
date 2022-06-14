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
import hcmute.spkt.nhom03.finalproject.databinding.FragmentChatBinding;

public class FragmentUsers extends Fragment {
    FragmentChatBinding binding;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    RecyclerView myRecyclerView;
    FirebaseDatabase database;
    FirebaseAuth auth;

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_chat, container, false);
//        UsersAdapter usersAdapter = new UsersAdapter(getContext(), users);
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView = v.findViewById(R.id.recycler_view);
//        UsersAdapter usersAdapter = new UsersAdapter(getContext(), users);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(usersAdapter);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", token);
            database.getReference()
                    .child("users")
                    .child(Objects.requireNonNull(auth.getUid()))
                    .updateChildren(map);
        });

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), users);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database.getReference().
                child("users").
                addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            if (!Objects.requireNonNull(user).getUid().equals(auth.getUid()))
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
    public void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Online");
    }
    @Override
    public void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Offline");
    }
}