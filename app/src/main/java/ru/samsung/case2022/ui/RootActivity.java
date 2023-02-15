package ru.samsung.case2022.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import ru.samsung.case2022.R;
import ru.samsung.case2022.adapters.ListAdapter;
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;

public class RootActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ActionMenuItemView scan, add;
    private TextView warning, hint;
    private static DBManager manager;
    private ListAdapter adapter;

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);

        ArrayList<Product> products = getProducts();
        if (products.size() == 0) {
            adapter = new ListAdapter(new ArrayList<>());
            warning.setVisibility(View.VISIBLE);
            hint.setVisibility(View.VISIBLE);
            recycler.setAdapter(adapter);
        } else {
            adapter = new ListAdapter(products);
            recycler.setAdapter(adapter);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddProductActivity.class));
            }
        };
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manager.getItemCount() > 0) {
                    startActivity(new Intent(getApplicationContext(), ScanActivity.class));
                } else {
                    Snackbar.make(scan, "Ваш список товаров пуст!", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });
        add.setOnClickListener(onClickListener);
        hint.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Product> list = getProducts();
        if (list.size() != 0) {
            warning.setVisibility(View.INVISIBLE);
            hint.setVisibility(View.INVISIBLE);
            adapter.setProducts(getProducts());
        } else {
            adapter.setProducts(list);
            warning.setVisibility(View.VISIBLE);
            hint.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<Product> getProducts() {
        ArrayList<Product> products = manager.getAllList();
        return products;
    }
 }