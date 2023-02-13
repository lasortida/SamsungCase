package ru.samsung.case2022.objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class DBManager {
    private Context context;
    private String DB_NAME = "products.db";
    private SQLiteDatabase db;
    private static DBManager dbManager;

    public static DBManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = new DBManager(context);
        }
        return dbManager;
    }

    private DBManager(Context context) {
        this.context = context;
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createTablesIfNeedBe();
    }

    public void addProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put("NAME", product.getName());
        db.insert("PRODUCTS", null, values);
    }

    public ArrayList<Product> getAllList() {
        ArrayList<Product> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM PRODUCTS;", null);
        boolean hasMoreData = cursor.moveToFirst();
        while (hasMoreData) {
            String name = cursor.getString(0);
            list.add(new Product(name));
            hasMoreData = cursor.moveToNext();
        }
        return list;
    }

    private void createTablesIfNeedBe() {
        db.execSQL("CREATE TABLE IF NOT EXISTS PRODUCTS (NAME TEXT, COST FLOAT);");
    }

    public void replaceProduct(Product oldProduct, String newName) {
        String oldName = oldProduct.getName();
        ContentValues values = new ContentValues();
        values.put("NAME", newName);
        db.update("PRODUCTS", values, "NAME = ?", new String[]{oldName});
    }

    public void deleteProduct(Product product) {
        String name = product.getName();
        db.delete("PRODUCTS", "NAME = ?", new String[]{name});
    }

    public void deleteProduct(int index) {
        Product product = getAllList().get(index);
        deleteProduct(product);
    }

    public int contains(RecResult result) throws Exception {
        ArrayList<Product> products = getAllList();
        String[] predict = result.naming;
        ArrayList<String> predictList = new ArrayList<>(Arrays.asList(predict));
        float[] probability = new float[products.size()];
        for (int i = 0; i < products.size(); ++i) {
            String productName = products.get(i).getName();
            String[] tables = productName.split(" ");
            probability[i] = getProbability(tables, predictList);
        }
        float max_prob = -1;
        int index = -1;
        for (int i = 0; i < probability.length; ++i) {
            if (probability[i] > max_prob) {
                max_prob = probability[i];
                index = i;
            }
        }
        if (max_prob >= 0.5) {
            return index;
        } else {
            throw new Exception("element not found");
        }
    }

    private float getProbability(String[] tables, ArrayList<String> predict) {
        float probability = 0;
        String word = tables[0].toLowerCase(Locale.ROOT).replace('ё', 'е');
        if (predict.contains(word)) {
            probability += 0.4;
        }
        for (int i = 1; i < tables.length; ++i) {
            word = tables[i].toLowerCase(Locale.ROOT).replace('ё', 'е');
            if (predict.contains(word)) {
                probability += 0.1;
            }
        }
        if (tables.length == 1) {
            if (probability >= 0.4) {
                probability = 1;
            } else {
                probability = 0;
            }
        }
        Log.d("SIGN", tables[0] + "Probability: " + String.valueOf(probability));
        return probability;
    }

    public int getItemCount() {
        return getAllList().size();
    }
}
