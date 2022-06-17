package hcmute.spkt.nhom03.finalproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import hcmute.spkt.nhom03.finalproject.R;
import hcmute.spkt.nhom03.finalproject.databinding.ActivityPhoneNumberBinding;
public class PhoneNumberActivity extends AppCompatActivity {
    ActivityPhoneNumberBinding binding; //* Khởi tạo binding cho ActivityPhoneNumber

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //* Thực hiện gán giá trị setEnabled bằng falss --> để không thể click được btnContinue
        binding.btnContinue.setEnabled(false);
        showKeyboard(); //* Gọi hàm để show keyboard
        binding.edtPhone.requestFocus();    //* Thực hiện đưa con trỏ vào edtPhone
        checkEnterView();   //* Thực hiện kiểm tra việc nhập cho các EditText
    }
    //* Khởi tạo hàm showKeyboard
    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    //* Khởi tạo hàm closeKeyboard
    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    //* Khởi tạo hàm checkEnterView()
    public void checkEnterView() {
        //* addTextChangedListener để kiểm tra sự thay đổi trong edtPhone
        binding.edtPhone.addTextChangedListener(textWatcher);
    }

    //* Khởi tao sự kiện click cho btnContinue --> app:onClick = "switchTabSendOTP"
    public void switchTabSendOTP(View view) {
        String phoneNumber = binding.edtPhone.getText().toString();
        //* gọi intent
        Intent intent = new Intent(this, SendOTPActivity.class);
        //* Thực hiện put giá trị phoneNumber vào "phone"
        intent.putExtra("phone", phoneNumber);
        //* Thưck hiện việc startActivity đồng thời  truyền giá trị sang SendOTPActivity
        startActivity(intent);
        //* Đóng keyboard
        closeKeyboard();
    }
    //* Khởi tạo hàm textWatcher
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //* Gán giá trị cho biến phoen kiểu String vơi giá trị bằng với giá trị trong edtPhone
            String phone = binding.edtPhone.getText().toString().trim();
            //* Néu phone khác rỗng
            if (!phone.isEmpty()) {
                //* Show button close --> có tác dụng xóa hết tất cả các text đã được nhập trong edtphone một cách nhanh chóng
                binding.imgButtonClose.setVisibility(View.VISIBLE);
                //* Nếu chiều dài của phone == 12
                if (phone.length() == 12) {
                    //* Thực hiện việc thay đổi màu cho btnContinue thành màu blue_light --> blue_light đã được tạo trong color.xml
                    binding.btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.neon_blue)));
                    //* Thực hiện set sự kiện eanble cho btnContinue để có thể click được
                    binding.btnContinue.setEnabled(true);
                } else {
                    //* Thực hiện việc thay đổi màu cho btnContinue thành màu gray_ccc --> gray_ccc đã được tạo trong color.xml
                    binding.btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_ccc)));
                    //* Thực hiện set sự kiện eanble cho btnContinue để không thể click được
                    binding.btnContinue.setEnabled(false);
                }
            } else {
                binding.imgButtonClose.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //* Khởi tạo hàm click cho btnClose --> app:onClick = "clearText"
    public void clearText(View view) {
        //* Gán giá trị rỗng cho edtPhone
        binding.edtPhone.setText("");
    }
}