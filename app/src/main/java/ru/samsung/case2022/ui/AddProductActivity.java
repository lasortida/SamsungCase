package ru.samsung.case2022.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import ru.samsung.case2022.R;
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;

public class AddProductActivity extends AppCompatActivity {

    private ActionMenuItemView scan, add;
    private MaterialToolbar toolbar;
    private MaterialButton save;
    private TextInputEditText editTextCount;
    private EditText editText;
    private static DBManager manager;
    private String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        tableName = getIntent().getExtras().getString("LIST_NAME");
        if (tableName.equals("non")) {
            tableName = "Новый список";
        }

        scan = findViewById(R.id.scan);
        add = findViewById(R.id.add);
        toolbar = findViewById(R.id.topAppBar);
        save = findViewById(R.id.save);
        editText = findViewById(R.id.editProductName);
        editTextCount = findViewById(R.id.editTextCount);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SIGN", "table: " + tableName);
                manager = DBManager.getInstance(getApplicationContext(), tableName);
                String text = editText.getText().toString();
                int number = Integer.parseInt(editTextCount.getText().toString());
                if (text.equals("") || text.equals(" ")) {
                    Snackbar.make(save, R.string.warning_null, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.save, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finishAndGetResult(text, number);
                                }
                            }).show();
                } else {
                    finishAndGetResult(text, number);
                }
            }
        });
        scan.setClickable(false);
        add.setClickable(false);
        scan.setAlpha(0.2f);
    }

    public void finishAndGetResult(String productName, int number) {
        Product product = new Product(productName);
        product.setCount(number);
        Product oldProduct = manager.getProduct(product.getName(), tableName);
        boolean add = manager.addProduct(product, tableName);
        Intent intent = new Intent();
        intent.putExtra("LIST_NAME", tableName);
        intent.putExtra("ADD", add);
        if (oldProduct != null) {
            Log.d("SIGN", String.valueOf(oldProduct.getCount()) + " YES");
            intent.putExtra("OLD", oldProduct);
            product.setCount(product.getCount() + oldProduct.getCount());
        }
        intent.putExtra("PRODUCT", product);
        setResult(RESULT_OK, intent);
        finish();
    }
}