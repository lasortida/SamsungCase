package ru.samsung.case2022.objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

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
}
