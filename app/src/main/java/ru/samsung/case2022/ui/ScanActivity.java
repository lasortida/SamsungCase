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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import ru.samsung.case2022.R;
import ru.samsung.case2022.objects.RecResult;

public class ScanActivity extends AppCompatActivity {

    private ImageView preview;
    private RecognitionService service;
    private MaterialButton recognize, cancel;
    private TextView message;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        preview = findViewById(R.id.preview);
        recognize = findViewById(R.id.recognize);
        cancel = findViewById(R.id.cancel);
        message = findViewById(R.id.textViewMessage);
        progressBar = findViewById(R.id.progressBar);

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
                        } else {
                            onBackPressed();
                        }
                    }
                }
        );

        resultLauncher.launch(intent);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.94:80/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(RecognitionService.class);

        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preview.setVisibility(View.INVISIBLE);
                message.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                Drawable drawable = preview.getDrawable();
                File file = getImageFile(drawable, path);
                MultipartBody.Part body = formPart(file);
                Call<RecResult> call = service.recognize(body);
                call.enqueue(new Callback<RecResult>() {
                    @Override
                    public void onResponse(Call<RecResult> call, Response<RecResult> response) {
                        RecResult result = response.body();
                        Log.d("SIGN", result.naming[0]);
                        deleteFile(path);
                    }

                    @Override
                    public void onFailure(Call<RecResult> call, Throwable t) {
                        Log.d("SIGN", t.getLocalizedMessage());
                        deleteFile(path);
                    }
                });
            }
        });
    }

    private MultipartBody.Part formPart(File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        return body;
    }

    private File getImageFile(Drawable drawable, Uri uri) {
        BitmapDrawable bdrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bdrawable.getBitmap();
        File file = getApplicationContext().getFileStreamPath("recognition.jpg");
        try {
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
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