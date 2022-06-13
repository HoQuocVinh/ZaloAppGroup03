package hcmute.spkt.nhom03.finalproject.Models;

public class Contact {
    private String name; //* Tên được lưu trong contact của phone
    private String phoneNo; //* Số điện thoại được lưu trong contact của phone

    public String getPhoneNo() {
        //* Khởi tạo biến phoneNoChange = phoneNo;
        String phoneNoChange = phoneNo;
        //* Cắt bỏ kí tự đầu tiên của phoneNoChang (mặc định là số 0)
        phoneNoChange = phoneNoChange.substring(1);
        //* Lây chuỗi "+84" cộng với chuỗi phoneNoChange
        phoneNoChange = "+84" + phoneNoChange;
        //Sử dụng replaceAll để xóa toàn bộ khoảng trấng trong phoneNo
        return phoneNoChange.replaceAll("\\s+", "");
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
