package ru.samsung.case2022.ui;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

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
import android.os.Environment;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private ActionMenuItemView scan, add;
    private RecognitionService service;
    private MaterialButton recognize, cancel;
    private TextView message;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private static DBManager manager;
    private String tableName;

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
        scan = findViewById(R.id.scan);
        add = findViewById(R.id.add);

        tableName = getIntent().getExtras().getString("LIST_NAME");

        manager = DBManager.getInstance(this, tableName);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri path = createImage();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, path);

        scan.setClickable(false);
        add.setClickable(false);
        add.setAlpha(0.2f);

        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && path != null) {
                            Log.d("SIGN", "work");
                            preview.setImageURI(path);
                        } else {
                            onBackPressed();
                        }
                        if (path != null) {
                            deleteFile(path);
                        }
                    }
                }
        );

        resultLauncher.launch(intent);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://87.249.44.16:8001/")
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
                                finishWithError("Фотография плохого качества!");
                            } else {
                                try {
                                    Product buy = handleResponse(result);
                                    if (result.cost.length > 0) {
                                        String cost = result.cost[0].replace('б', '6');
                                        String rubles = cost.substring(0, cost.indexOf('р'));
                                        String copes = cost.substring(cost.indexOf('р') + 1, cost.length() - 1);
                                        int rub = Integer.parseInt(rubles);
                                        int cop = Integer.parseInt(copes);
                                        double cost_cop = (double) cop / 100d;
                                        float cost_r = (float) cost_cop + rub;
                                        buy.setCost(cost_r);
                                    }
                                    getResult(buy);
                                } catch (Exception e) {
                                    finishWithError("Данный товар не найден в списке!");
                                }
                            }
                        } else {
                            finishWithError("Проблема подключения к серверу!");
                        }
                        preview.setVisibility(View.VISIBLE);
                        message.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(Call<RecResult> call, Throwable t) {
                        finishWithError("Проблема подключения к серверу!");
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imgName = "recognition_" + timeStamp + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imgName);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Documents/" + "Checklist/");
        ContentResolver resolver = getContentResolver();
        Uri finalUri = resolver.insert(uri, values);
        return finalUri;
    }

    private void deleteFile(Uri uri) {
        ContentResolver resolver = getContentResolver();
        resolver.delete(uri, null, null);
    }

    public Product handleResponse(RecResult result) throws Exception {
        Log.d("SIGN", "find " + tableName);
        Product product = manager.contains(result, tableName);
        manager.deleteProduct(product, tableName);
        return product;
    }

    public void restart() {
        Intent intent = new Intent();
        intent.putExtra("RESTART", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void getResult(Product buy) {
        Intent intent = new Intent();
        intent.putExtra("RESTART", false);
        intent.putExtra("BUY_PRODUCT", buy);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void finishWithError(String message) {
        Intent intent = new Intent();
        intent.putExtra("MESSAGE", message);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }
}