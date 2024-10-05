package com.example.imagerecognitionmlkit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Khai báo các biến cho Camera Launcher, Image Labeler và các thành phần giao diện
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ImageLabeler imageLabeler;
    private TextView labelText;
    private ImageView objectImage;

    @SuppressLint({"SetTextI18n", "QueryPermissionsNeeded"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần giao diện
        objectImage = findViewById(R.id.objectImage);
        labelText = findViewById(R.id.labelText);
        Button captureButton = findViewById(R.id.captureButton);

        // Kiểm tra quyền truy cập Camera
        checkCameraPermission();

        // Tạo một Image Labeler sử dụng các tùy chọn mặc định
        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        // Đăng ký kết quả trả về từ Activity để chụp ảnh từ Camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Nếu kết quả từ Camera là thành công
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle extras = null;
                        // Lấy dữ liệu từ Intent kết quả
                        if (result.getData() != null) {
                            extras = result.getData().getExtras();
                        }
                        Bitmap imageBitmap = null;
                        // Lấy Bitmap từ dữ liệu trả về
                        if (extras != null) {
                            imageBitmap = (Bitmap) extras.get("data");
                            labelImage(imageBitmap); // Gọi hàm để gắn nhãn cho ảnh
                        }
                        // Hiển thị ảnh đã chụp lên ImageView
                        if (imageBitmap != null) {
                            objectImage.setImageBitmap(imageBitmap);
                        } else {
                            labelText.setText("No image captured"); // Thông báo nếu không có ảnh
                        }
                    }
                }
        );

        // Gắn sự kiện cho nút "Capture"
        captureButton.setOnClickListener(v -> {
            // Kiểm tra quyền truy cập camera trước khi mở
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Tạo một Intent để mở camera
                Intent clickPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Kiểm tra xem có ứng dụng nào có thể xử lý Intent này không
                if (clickPicture.resolveActivity(getPackageManager()) != null) {
                    cameraLauncher.launch(clickPicture); // Mở camera
                }
            } else {
                // Nếu không có quyền, thông báo và yêu cầu cấp quyền
                Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_SHORT).show();
                checkCameraPermission();
            }
        });
    }

    // Hàm để gắn nhãn cho ảnh chụp từ camera
    @SuppressLint("SetTextI18n")
    private void labelImage(Bitmap bitmap) {
        // Tạo đối tượng InputImage từ Bitmap
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        // Sử dụng Image Labeler để xử lý ảnh
        imageLabeler.process(inputImage)
                .addOnSuccessListener(this::displayLabel) // Nếu thành công, hiển thị nhãn
                .addOnFailureListener(e -> labelText.setText("Error: " + e.getMessage())); // Nếu thất bại, hiển thị lỗi
    }

    // Hàm hiển thị kết quả gắn nhãn
    @SuppressLint("SetTextI18n")
    private void displayLabel(List<ImageLabel> labels) {
        if (!labels.isEmpty()) {
            // Lấy nhãn có độ tin cậy cao nhất
            ImageLabel mostConfidentLabel = labels.get(0);
            // Hiển thị nhãn lên TextView
            labelText.setText(mostConfidentLabel.getText());
        } else {
            labelText.setText("No object detected"); // Nếu không có nhãn nào, thông báo không có đối tượng nào được phát hiện
        }
    }

    // Hàm kiểm tra và yêu cầu quyền truy cập camera nếu chưa có
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập camera nếu chưa được cấp
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        } else {
            // Thông báo nếu quyền đã được cấp
            Toast.makeText(this, "Permission already granted, you can start using the camera here", Toast.LENGTH_SHORT).show();
        }
    }
}
