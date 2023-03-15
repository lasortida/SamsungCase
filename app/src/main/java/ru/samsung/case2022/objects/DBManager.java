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

    public static DBManager getInstance(Context context, String tableName) {
        dbManager = new DBManager(context, tableName);
        return dbManager;
    }


    public void dropDatabase(String tableName) {
        tableName = tableName.replace(' ', '_');
        db.execSQL("DROP TABLE " + tableName + ";");
    }

    private DBManager(Context context, String tableName) {
        tableName = tableName.replace(' ', '_');
        this.context = context;
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createTablesIfNeedBe(tableName);
    }

    public Product getProduct(String productName, String tableName) {
        tableName = tableName.replace(' ', '_');
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE NAME = '" + productName + "';", null);
        boolean hasMoreData = cursor.moveToFirst();
        while (hasMoreData) {
            String name = cursor.getString(0);
            int count = cursor.getInt(1);
            Product result = new Product(name);
            result.setCount(count);
            return result;
        }
        Product product = null;
        return product;
    }

    public boolean addProduct(Product product, String tableName) {
        tableName = tableName.replace(' ', '_');
        ContentValues values = new ContentValues();
        Product old = getProduct(product.getName(), tableName);
        if (old != null) {
            Log.d("SIGN", String.valueOf(old.getCount()));
            Product newProduct = new Product(product.getName());
            newProduct.setCount(old.getCount() + product.getCount());
            replaceProduct(old, newProduct, tableName);
            return false;
        } else {
            Log.d("SIGN", "YEEEES0");
            values.put("NAME", product.getName());
            values.put("COUNT", product.getCount());
            Log.d("SIGN", "saved " + tableName);
            db.insert(tableName, null, values);
            return true;
        }
    }

    public ArrayList<Product> getAllList(String tableName) {
        tableName = tableName.replace(' ', '_');
        ArrayList<Product> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + ";", null);
        boolean hasMoreData = cursor.moveToFirst();
        while (hasMoreData) {
            String name = cursor.getString(0);
            Product product = new Product(name);
            product.setCount(cursor.getInt(1));
            list.add(product);
            hasMoreData = cursor.moveToNext();
        }
        return list;
    }

    private void createTablesIfNeedBe(String tableName) {
        tableName = tableName.replace(' ', '_');
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + "(NAME TEXT UNIQUE, COUNT INTEGER);");
    }

    public void replaceProduct(Product oldProduct, Product newProduct, String tableName) {
        tableName = tableName.replace(' ', '_');
        String oldName = oldProduct.getName();
        ContentValues values = new ContentValues();
        values.put("NAME", newProduct.getName());
        values.put("COUNT", newProduct.getCount());
        db.update(tableName, values, "NAME = ?", new String[]{oldName});
    }

    public void deleteProduct(Product product, String tableName) {
        tableName = tableName.replace(' ', '_');
        String name = product.getName();
        db.delete(tableName, "NAME = ?", new String[]{name});
    }

    public void deleteProduct(int index, String tableName) {
        tableName = tableName.replace(' ', '_');
        Product product = getAllList(tableName).get(index);
        deleteProduct(product, tableName);
    }

    public Product contains(RecResult result, String tableName) throws Exception {
        tableName = tableName.replace(' ', '_');
        ArrayList<Product> products = getAllList(tableName);
        String[] predict = result.naming;
        ArrayList<String> predictList = new ArrayList<>(Arrays.asList(predict));
        String[] numbers = result.numbers;
        ArrayList<String> numbersList = new ArrayList<>(Arrays.asList(numbers));
        predictList.addAll(numbersList);
        int maxCount = 0;
        ArrayList<Product> productList = new ArrayList<>();
        for (int i = 0; i < products.size(); ++i) {
            String productName = products.get(i).getName();
            int count = getCountWords(productName, predictList);
            if (count > maxCount) {
                maxCount = count;
                productList.clear();
                productList.add(products.get(i));
            }
            if (count == maxCount) {
                productList.add(products.get(i));
            }
        }
        if (maxCount > 0) {
            return productList.get(0);
        } else {
            throw new Exception("element not found");
        }
    }

    private int getCountWords(String productName, ArrayList<String> predicted) {
        int result = 0;
        String[] words = productName.toLowerCase(Locale.ROOT).replace('ё', 'е').split(" ");
        for (int i = 0; i < words.length; ++i) {
            if (predicted.contains(words[i])) {
                result += 1;
            }
        }
        return result;
    }

    public void renameTable(String oldName, String newName) {
        newName = newName.replace(' ', '_');
        oldName = oldName.replace(' ', '_');
        db.execSQL("ALTER TABLE " + oldName + " RENAME TO " + newName + ";");
    }
}
