package com.example.stphotozone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PreviewActivity extends AppCompatActivity {
    ImageView imageView;
    Button again, save;
    Bitmap getImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imageView = (ImageView)findViewById(R.id.preview);
        again = (Button) findViewById(R.id.again);
        save = (Button) findViewById(R.id.save);

        // 받은 이미지 처리
        String imagePath = getIntent().getStringExtra("sendImg");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                getImage = BitmapFactory.decodeFile(imagePath);
                imageView.setImageBitmap(getImage);
            }
        }

        again.setOnClickListener(view -> {
            returnResult(true);
        });

        save.setOnClickListener(view -> {
            try {
                saveBitmapToDisk(getImage);
                returnResult(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void returnResult(boolean result) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultKey", result);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void saveBitmapToDisk(Bitmap bitmap) throws IOException {
        String albumName = "STphotozone";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), albumName);

        // 디렉토리가 없으면 생성
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File file = new File(storageDir, "screenshot_" + Long.toHexString(System.currentTimeMillis()) + ".png");

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
        }

        // 미디어 스토어에 파일 등록
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, (path, uri) -> {
            runOnUiThread(() -> Toast.makeText(this, "저장되었습니다: " + albumName, Toast.LENGTH_SHORT).show());
        });
    }

}
