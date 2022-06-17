package hcmute.spkt.nhom03.finalproject.Controller;

import android.media.MediaRecorder;

import java.io.IOException;

public class AudioRecorder {
    private MediaRecorder mediaRecorder;    //* Khưởi tạo mediaRecorder

    //* Khởi tạo hàm initMediaRecorder
    private void initMediaRecorder(){
        //* Mapping lại mediaRecorder
        mediaRecorder = new MediaRecorder();
        //* Set âm thanh cho mediaRecorder lấy âm thanh từ mic
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //* Format kiểu output cho mediaRecorder
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //* Set mã hóa âm thanh cho mediaRecorder
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    //* Khởi tạo hàm start
    public void start(String filePath) throws IOException {
        //* Nếu mediaRecorder == null
        if (mediaRecorder == null) {
            //* Thực hiện gọi lại hàm initMediaRecorder
            initMediaRecorder();
        }
        //* set oupt cho mediaRecorder với đường dẫn
        mediaRecorder.setOutputFile(filePath);
        //* Chuẩn bị record
        mediaRecorder.prepare();
        //* Bắt đầu record
        mediaRecorder.start();
    }

    //* Khởi tạo hàm stop
    public void stop() {
        //* Thực hiện kiểm tra try/catch
        try {
            //* Dừng việc thu âm lại
            mediaRecorder.stop();
            //* destroyMediaRecorder()
            destroyMediaRecorder();
        } catch (Exception e) {
            //* show lỗi trong quá trình đừng
            e.printStackTrace();
        }
    }

    //* Khởi tạo hàm destroyMediaRecorder
    private void destroyMediaRecorder() {
        //* Giải phóng bộ nhớ cho mediaRecorder
        mediaRecorder.release();
        //* Gán giá trị cho mediaRecorder là null
        mediaRecorder = null;
    }
}
