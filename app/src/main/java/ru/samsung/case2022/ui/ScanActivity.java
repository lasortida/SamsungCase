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
import android.graphics.Color;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

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
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;
import ru.samsung.case2022.objects.RecResult;

public class ScanActivity extends AppCompatActivity {

    private ImageView preview;
    private RecognitionService service;
    private MaterialButton recognize, cancel;
    private TextView message;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private static DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        preview = findViewById(R.id.preview);
        recognize = findViewById(R.id.recognize);
        cancel = findViewById(R.id.cancel);
        message = findViewById(R.id.textViewMessage);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.topAppBar);

        manager = DBManager.getInstance(this);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri path = createImage();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, path);

        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d("SIGN", "work");
                            preview.setImageURI(path);
                            deleteFile(path);
                        } else {
                            onBackPressed();
                        }
                    }
                }
        );

        resultLauncher.launch(intent);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://81.200.145.45:8001/")
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
                File file = getImageFile(drawable);
                MultipartBody.Part body = formPart(file);
                Call<RecResult> call = service.recognize(body);
                call.enqueue(new Callback<RecResult>() {
                    @Override
                    public void onResponse(Call<RecResult> call, Response<RecResult> response) {
                        if (response.isSuccessful()) {
                            RecResult result = response.body();
                            if (result.naming.length == 0) {
                                Snackbar.make(recognize, "Фотография плохого качества!", BaseTransientBottomBar.LENGTH_INDEFINITE)
                                        .setAction("Перефотографировать", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                onBackPressed();
                                                startActivity(new Intent(getApplicationContext(), ScanActivity.class));
                                            }
                                        }).setActionTextColor(Color.parseColor("#3eb489")).show();
                            } else {
                                try {
                                    handleResponse(result);
                                    onBackPressed();
                                } catch (Exception e) {
                                    Snackbar.make(recognize, "Товар не найден в списке!", BaseTransientBottomBar.LENGTH_LONG)
                                            .setAction("Открыть камеру", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    onBackPressed();
                                                    startActivity(new Intent(getApplicationContext(), ScanActivity.class));
                                                }
                                            }).setActionTextColor(Color.parseColor("#3eb489")).show();
                                    Log.d("SIGN", "bad photo!");
                                    if (result.naming.length > 0) {
                                        Log.d("SIGN", result.naming[0]);
                                    } else {
                                        Log.d("SIGN", "size - 0");
                                    }
                                }
                            }
                        } else {
                            Log.d("SIGN", response.message());
                            Snackbar.make(recognize, "Не удалось соединиться с сервером! Попробуйте ещё раз!", BaseTransientBottomBar.LENGTH_SHORT)
                                    .show();
                        }
                        preview.setVisibility(View.VISIBLE);
                        message.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(Call<RecResult> call, Throwable t) {
                        Snackbar.make(recognize, "Не удалось соединиться с сервером! Попробуйте ещё раз!", BaseTransientBottomBar.LENGTH_SHORT)
                                .show();
                        preview.setVisibility(View.VISIBLE);
                        message.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private MultipartBody.Part formPart(File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        return body;
    }

    private File getImageFile(Drawable drawable) {
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
        Uri uri = MediaStore.Files.getContentUri("external");
        String imgName = "recognition.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imgName);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Download/" + "Checklist/");
        ContentResolver resolver = getContentResolver();
        Uri finalUri = resolver.insert(uri, values);
        return finalUri;
    }

    private void deleteFile(Uri uri) {
        ContentResolver resolver = getContentResolver();
        resolver.delete(uri, null, null);
    }

    public void handleResponse(RecResult result) throws Exception {
        Product product = manager.contains(result);
        manager.deleteProduct(product);
    }
}