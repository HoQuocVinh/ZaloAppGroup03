package hcmute.spkt.nhom03.finalproject.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import hcmute.spkt.nhom03.finalproject.Models.Contact;
import hcmute.spkt.nhom03.finalproject.Models.User;
import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.RowConverstationContactBinding;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    Context context;
    ArrayList<Contact> contacts;

    public ContactAdapter() {
    }

    @SuppressLint("NotifyDataSetChanged")
    public ContactAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_converstation_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");

        Contact contact = contacts.get(position);
        holder.binding.nameContact.setText(contact.getName());
        holder.binding.phoneContact.setText(contact.getPhoneNo());

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot item) {
                String phoneContact = contact.getPhoneNo();
                boolean isHasUser = false;
                for (DataSnapshot dataSnapshot : item.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);  /*Sử dụng model User để nhận dữ liệu từ realtime database*/
                    String phoneAuth = Objects.requireNonNull(user).getPhoneNumber();
                    if (phoneAuth.equals(phoneContact)) {
                        isHasUser = true;
                        break;
                    }
                }
                if (isHasUser) {
                    holder.binding.btnAddFriend.setVisibility(View.GONE);
                    holder.binding.added.setVisibility(View.VISIBLE);
                    holder.binding.added.setText("Added");
                } else {
                    holder.binding.btnAddFriend.setVisibility(View.GONE);
                    holder.binding.added.setVisibility(View.VISIBLE);
                    holder.binding.added.setText("No register");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        RowConverstationContactBinding binding;

        //        TextView txtName, textNumber;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConverstationContactBinding.bind(itemView);
//            txtName = binding.nameContact;
//            textNumber = binding.phoneContact;
        }
    }
}
