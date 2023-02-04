package ru.samsung.case2022.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.samsung.case2022.R;
import ru.samsung.case2022.adapters.ListAdapter;
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;

public class RootActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ActionMenuItemView scan, add;
    private TextView warning, hint;
    private static DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        recycler = findViewById(R.id.recycler);
        scan = findViewById(R.id.scan);
        add = findViewById(R.id.add);
        warning = findViewById(R.id.warning);
        hint = findViewById(R.id.hint);
        manager = DBManager.getInstance(this);

        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(manager);
        recycler.setHasFixedSize(true);

        ArrayList<Product> products = getProducts();
        if (products.size() == 0) {
            warning.setVisibility(View.VISIBLE);
            hint.setVisibility(View.VISIBLE);
        } else {
            ListAdapter adapter = new ListAdapter(products);
            recycler.setAdapter(adapter);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddProductActivity.class));
            }
        };
        add.setOnClickListener(onClickListener);
        hint.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Product> list = getProducts();
        if (list.size() != 0) {
            ListAdapter adapter = new ListAdapter(list);
            recycler.swapAdapter(adapter, true);
        }
    }

    private ArrayList<Product> getProducts() {
        ArrayList<Product> products = manager.getAllList();
        return products;
    }
 }