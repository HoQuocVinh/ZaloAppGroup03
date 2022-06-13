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
    Context context; //* Context chứa adapter
    ArrayList<Contact> contacts;    //* Danh sách chứa contact

    public ContactAdapter() {
    }

    @SuppressLint("NotifyDataSetChanged")
    public ContactAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    /**=
     * Hàm onCreateViewHolder sẽ tạo ViewHolder để map các dữ liệu từ adapter xuống view
     * @param parent view parent của adapter
     * @param viewType loại view
     * @return
     */
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_converstation_contact, parent, false);
        return new ContactViewHolder(view);
    }

    /**
     * Hàm onBindViewHolder binding các value xuống các view holder
     * @param holder các view trong ContactViewHolder
     * @param position vị trí của từng đối tượng UserModel
     */
    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {
        //* Khởi tạo biến database để nhận dữ liệu từ Firebasedatabase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //* Nhận dữ liệu từ Firebasedatabase với đường dẫn "users" được cung cấp trực tiếp
        DatabaseReference reference = database.getReference("users");

        //* Lây contact ở vị trí position
        Contact contact = contacts.get(position);
        //* Gán contact.getName() vào nameContact trong viewHolder
        holder.binding.nameContact.setText(contact.getName());
        //* Gán contact.getPhoneNo() vào phoneContact trong viewHolder
        holder.binding.phoneContact.setText(contact.getPhoneNo());

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            /**@param item lây từng item trên Realtime Database xuống*/
            public void onDataChange(@NonNull DataSnapshot item) {
                //* Khởi tạo biến phoneContact để nhận dữ  liệu contact từ phone
                String phoneContact = contact.getPhoneNo();
                //* Khởi tạo biến kiểm tra isHasUser
                boolean isHasUser = false;
                //* Chạy vòng lặp trong từ con của nút "users"
                for (DataSnapshot dataSnapshot : item.getChildren()) {
                    //* Sử dụng model User để nhận dữ liệu từ Firebase
                    User user = dataSnapshot.getValue(User.class);
                    //* Khởi tạo biến phoneAuth để nhận giá trị phoneNuber lấy xuống từ Firebase
                    String phoneAuth = Objects.requireNonNull(user).getPhoneNumber();
                    //* Nếu giá trị của phone lấy xuống từ Firebase bằng với giá trị phone từ contact của điện thoại ->
                    if (phoneAuth.equals(phoneContact)) {
                        //* Gán giá trị mới cho isHasUser  = true
                        isHasUser = true;
                        //* Kết thúc quá trình kiểm tra -> thoát hẳn ra khỏi vòng lặp for
                        break;
                    }
                }
                //* Ẩn button add friend đi
                holder.binding.btnAddFriend.setVisibility(View.GONE);
                //* Show TextView
                holder.binding.added.setVisibility(View.VISIBLE);
                //* Nếu giá trị của isHasUser là true ->
                if (isHasUser)
                    //* Gán giá trị cho TextView = "Added"
                    //* --> nghĩa là số điện thoại đã được tạo trên Firebase
                    holder.binding.added.setText("Added");
                else //* Ngược lại
                //* Gán giá trị cho TextView = "No register"
                //* --> Nghĩa là số điện thoại chưa được tạo trên Firebase
                    holder.binding.added.setText("No register");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        //* Trả getItemCount về chiếu dài của list contact
        return contacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        RowConverstationContactBinding binding; //* Create RowConversationContactBinding
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConverstationContactBinding.bind(itemView);
        }
    }
}
