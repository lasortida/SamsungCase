package ru.samsung.case2022.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import ru.samsung.case2022.R;

public class ScanActivity extends AppCompatActivity {

    private ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        preview = findViewById(R.id.preview);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri path = createImage();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, path);

        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            preview.setImageURI(path);
                            deleteFile(path);
                            Drawable drawable = preview.getDrawable();

                        } else {
                            onBackPressed();
                        }
                    }
                }
        );

        resultLauncher.launch(intent);
    }

    private void getImage(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
    }

    private Uri createImage() {
        Uri uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        String imgName = "recognition.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imgName);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + "Checklist/");
        ContentResolver resolver = getContentResolver();
        Uri finalUri = resolver.insert(uri, values);
        return finalUri;
    }

    private void deleteFile(Uri uri) {
        ContentResolver resolver = getContentResolver();
        resolver.delete(uri, null, null);
    }
}