package ru.samsung.case2022.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import ru.samsung.case2022.R;
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;

public class AddProductActivity extends AppCompatActivity {

    private ActionMenuItemView scan;
    private MaterialToolbar toolbar;
    private MaterialButton save;
    private TextInputEditText editText;
    private static DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        scan = findViewById(R.id.scan);
        toolbar = findViewById(R.id.topAppBar);
        save = findViewById(R.id.buttonSave);
        editText = findViewById(R.id.editText);
        manager = DBManager.getInstance(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if (text.equals("") || text.equals(" ")) {
                    Snackbar.make(save, "Вы ввели пустое значение", Snackbar.LENGTH_SHORT)
                            .setAction("Сохранить", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    createProduct(text);
                                    onBackPressed();
                                }
                            }).show();
                } else {
                    createProduct(text);
                    onBackPressed();
                }
            }
        });
    }

    public void createProduct(String name) {
        Product product = new Product(name);
        manager.addProduct(product);
    }
}