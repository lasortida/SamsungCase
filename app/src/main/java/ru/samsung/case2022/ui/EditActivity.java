package ru.samsung.case2022.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

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
    private TextInputEditText editText;
    private static DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Product oldProduct = (Product) getIntent().getExtras().get("PRODUCT");

        scan = findViewById(R.id.scan);
        toolbar = findViewById(R.id.topAppBar);
        save = findViewById(R.id.save);
        editText = findViewById(R.id.editProductName);
        remove = findViewById(R.id.remove);
        manager = DBManager.getInstance(this);

        editText.setText(oldProduct.getName());

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
                if (newName.equals("") || newName.equals(" ")) {
                    Snackbar.make(save, "Вы ввели пустое значение", Snackbar.LENGTH_SHORT)
                            .setAction("Сохранить", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    manager.replaceProduct(oldProduct, newName);
                                    onBackPressed();
                                }
                            }).show();
                } else {
                    manager.replaceProduct(oldProduct, newName);
                    onBackPressed();
                }
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.deleteProduct(oldProduct);
                onBackPressed();
            }
        });
    }

}