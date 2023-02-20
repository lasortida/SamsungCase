package ru.samsung.case2022.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import ru.samsung.case2022.R;
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;

public class EditActivity extends AppCompatActivity {

    private ActionMenuItemView scan, add;
    private MaterialToolbar toolbar;
    private MaterialButton save, remove;
    private TextInputEditText editText, editTextCount;
    private static DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Product oldProduct = (Product) getIntent().getExtras().get("PRODUCT");
        String tableName = getIntent().getExtras().getString("LIST_NAME");

        scan = findViewById(R.id.scan);
        toolbar = findViewById(R.id.topAppBar);
        save = findViewById(R.id.save);
        editText = findViewById(R.id.editProductName);
        editTextCount = findViewById(R.id.editTextCount);
        remove = findViewById(R.id.remove);
        manager = DBManager.getInstance(this, tableName);

        editText.setText(oldProduct.getName());

        editTextCount.setText(String.valueOf(oldProduct.getCount()));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = editText.getText().toString();
                int number = Integer.parseInt(editTextCount.getText().toString());
                if (newName.equals("") || newName.equals(" ")) {
                    Snackbar.make(save, R.string.warning_null, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.save, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Product newProduct = new Product(newName);
                                    newProduct.setCount(number);
                                    finishAndGetResult(oldProduct, newProduct, tableName);
                                }
                            }).show();
                } else {
                    Product newProduct = new Product(newName);
                    newProduct.setCount(number);
                    finishAndGetResult(oldProduct, newProduct, tableName);
                }
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndGetResult(oldProduct, null, tableName);
            }
        });
    }

    public void finishAndGetResult(Product oldProduct, Product newProduct, String tableName) {
        Intent intent = new Intent();
        intent.putExtra("OLD_PRODUCT", oldProduct);
        intent.putExtra("NEW_PRODUCT", newProduct);
        if (newProduct == null) {
            manager.deleteProduct(oldProduct, tableName);
        } else {
            manager.replaceProduct(oldProduct, newProduct, tableName);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

}