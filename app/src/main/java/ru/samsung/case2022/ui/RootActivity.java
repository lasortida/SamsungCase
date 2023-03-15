package ru.samsung.case2022.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import ru.samsung.case2022.R;
import ru.samsung.case2022.adapters.ListAdapter;
import ru.samsung.case2022.objects.DBManager;
import ru.samsung.case2022.objects.Product;

public class RootActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ActionMenuItemView scan, add;
    private TextView warning, hint, listName, billing;
    private static DBManager manager;
    private ListAdapter adapter;
    private String tableName;
    private ArrayList<Product> fullList;
    private Intent scanIntent;
    private float sum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        recycler = findViewById(R.id.recycler);
        scan = findViewById(R.id.scan);
        add = findViewById(R.id.add);
        warning = findViewById(R.id.warning);
        hint = findViewById(R.id.hint);
        listName = findViewById(R.id.listName);
        billing = findViewById(R.id.textViewCost);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);

        tableName = getTableName();

        if (tableName == null) {
            warning.setVisibility(View.VISIBLE);
            hint.setVisibility(View.VISIBLE);
            fullList = new ArrayList<>();
            listName.setVisibility(View.INVISIBLE);
            billing.setVisibility(View.INVISIBLE);
        } else {
            manager = DBManager.getInstance(this, tableName);
            listName.setText(tableName);
            fullList = getProducts();
            sum = getSum();
            if (sum > 0) {
                billing.setText("Общая стоимость: " + sum);
            }
        }

        adapter = new ListAdapter(fullList);
        recycler.setAdapter(adapter);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddProductActivity.class);
                if (tableName == null) {
                    intent.putExtra("LIST_NAME", "non");
                } else {
                    intent.putExtra("LIST_NAME", tableName);
                }
                addResultLauncher.launch(intent);
            }
        };

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanIntent = new Intent(getApplicationContext(), ScanActivity.class);
                scanIntent.putExtra("LIST_NAME", tableName);
                if (tableName != null) {
                    scanResultLauncher.launch(scanIntent);
                } else {
                    Snackbar.make(scan, R.string.warning_list_null, BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        add.setOnClickListener(onClickListener);
        hint.setOnClickListener(onClickListener);

        listName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater.from(RootActivity.this).inflate(R.layout.dialog_layout, null);
                TextInputEditText editText = view1.findViewById(R.id.editListName);
                AlertDialog dialog = new MaterialAlertDialogBuilder(RootActivity.this)
                        .setTitle(R.string.change_name)
                        .setView(view1)
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String text = editText.getText().toString();
                                if (!text.equals("")) {
                                    manager.renameTable(tableName, text);
                                    listName.setText(text);
                                    tableName = renameList(text);
                                }
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
    }

    ActivityResultLauncher<Intent> addResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (tableName == null) {
                            warning.setVisibility(View.INVISIBLE);
                            hint.setVisibility(View.INVISIBLE);
                            listName.setVisibility(View.VISIBLE);
                        }
                        Intent intent = result.getData();
                        Product product = (Product) intent.getExtras().get("PRODUCT");
                        boolean add = (boolean) intent.getExtras().get("ADD");
                        String tableNameCur = intent.getExtras().getString("LIST_NAME");
                        if (add) {
                            fullList.add(product);
                            adapter.notifyItemInserted(fullList.size() - 1);
                        } else {
                            Product oldProduct = (Product) intent.getExtras().get("OLD");
                            int index = fullList.indexOf(oldProduct);
                            fullList.remove(index);
                            fullList.add(index, product);
                            adapter.notifyItemChanged(index);
                        }
                        if (tableName == null || !tableName.equals(tableNameCur)) {
                            tableName = renameList(tableNameCur);
                            listName.setText(tableName);
                            manager = DBManager.getInstance(getApplicationContext(), tableName);
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> scanResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        boolean restart = result.getData().getExtras().getBoolean("RESTART");
                        if (restart) {
                            scanResultLauncher.launch(scanIntent);
                        } else {
                            Product buy = (Product) result.getData().getExtras().get("BUY_PRODUCT");
                            int index = fullList.indexOf(buy);
                            fullList.remove(index);
                            adapter.notifyItemRemoved(index);
                            adapter.notifyItemRangeRemoved(index, adapter.getItemCount());
                            Snackbar.make(recycler, "Вычеркнуто: " + buy.getName(), BaseTransientBottomBar.LENGTH_LONG)
                                    .show();
                            if (buy.getCost() > 0) {
                                sum += buy.getCost() * buy.getCount();
                            } else {
                                Snackbar.make(recycler, R.string.price_null, BaseTransientBottomBar.LENGTH_LONG)
                                        .setAction(R.string.change, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                View view1 = LayoutInflater.from(RootActivity.this).inflate(R.layout.cost_dialog_layout, null);
                                                TextInputEditText editText = view1.findViewById(R.id.editCost);
                                                AlertDialog dialog = new MaterialAlertDialogBuilder(RootActivity.this)
                                                        .setTitle(R.string.enter_price)
                                                        .setView(view1)
                                                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                float cost = Float.parseFloat(editText.getText().toString());
                                                                sum += cost * buy.getCount();
                                                                dialogInterface.dismiss();
                                                            }
                                                        }).show();
                                            }
                                        }).setActionTextColor(Color.parseColor("#3eb489")).show();
                            }
                            sum = changeSum(sum);
                            if (sum > 0) {
                                billing.setText("Общая стоимость: " + String.valueOf(sum));
                                billing.setVisibility(View.VISIBLE);
                            }
                            if (fullList.size() == 0) {
                                endOfList();
                            }
                        }
                    } if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        String message = (String) result.getData().getExtras().get("MESSAGE");
                        Snackbar.make(recycler, message, BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            }
    );

    public void endOfList() {
        manager.dropDatabase(tableName);
        listName.setVisibility(View.INVISIBLE);
        tableName = renameList("");
        tableName = null;
        warning.setVisibility(View.VISIBLE);
        warning.setText(R.string.hint_done);
        hint.setText("Общая стоимость: " + sum);
        hint.setClickable(false);
        hint.setVisibility(View.VISIBLE);
        billing.setVisibility(View.INVISIBLE);
        sum = 0;
    }

    public void startEditLauncher(Product product) {
        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        intent.putExtra("PRODUCT", product);
        intent.putExtra("LIST_NAME", tableName);
        editResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> editResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d("SIGN", "yes");
                        Intent intent = result.getData();
                        Product oldProduct = (Product) intent.getExtras().get("OLD_PRODUCT");
                        Product newProduct = (Product) intent.getExtras().get("NEW_PRODUCT");
                        if (newProduct == null) {
                            int index = fullList.indexOf(oldProduct);
                            fullList.remove(index);
                            adapter.notifyItemRemoved(index);
                            adapter.notifyItemRangeRemoved(index, adapter.getItemCount());
                            if (fullList.size() == 0) {
                                tableName = renameList("");
                                tableName = null;
                                listName.setVisibility(View.INVISIBLE);
                                warning.setText("Список товаров пуст!");
                                warning.setVisibility(View.VISIBLE);
                                hint.setClickable(true);
                                hint.setText(R.string.hint);
                                hint.setVisibility(View.VISIBLE);
                                billing.setVisibility(View.INVISIBLE);
                                sum = 0;
                            }
                        } else {
                            int index = fullList.indexOf(oldProduct);
                            fullList.remove(index);
                            fullList.add(index, newProduct);
                            adapter.notifyItemChanged(index);
                        }
                    }
                }
            }
    );

    private ArrayList<Product> getProducts() {
        ArrayList<Product> products = manager.getAllList(tableName);
        return products;
    }

    private String getTableName() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        String tableName = pref.getString("LIST_NAME", null);
        if (tableName != null && tableName.equals("")) {
            tableName = null;
        }
        return tableName;
    }

    private String renameList(String newName) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("LIST_NAME", newName);
        editor.commit();
        return newName;
    }

    private float getSum() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        float sum = pref.getFloat("SUM", 0);
        return sum;
    }

    private float changeSum(float sum) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putFloat("SUM", sum);
        editor.commit();
        return sum;
    }
 }