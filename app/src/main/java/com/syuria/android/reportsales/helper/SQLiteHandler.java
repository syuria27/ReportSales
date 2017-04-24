package com.syuria.android.reportsales.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HP on 19/11/2016.
 */

public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "android_api";
    // Login table name
    private static final String TABLE_USER = "user";
    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_NAMA_TOKO = "nama_toko";
    private static final String KEY_DEPOT = "depot";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";

    public static final String TABLE_NAME = "product";
    public static final String COLUMN_ID_PRODUK = "id";
    public static final String COLUMN_NAME_KODE_PRODUK = "kode_product";
    public static final String COLUMN_NAME_NAMA_PRODUK = "nama_product";
    public static final String COLUMN_NAME_FLAG_FOKUS = "flag_fokus";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_NAMA_TOKO + " TEXT,"
                + KEY_DEPOT + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
        Log.d(TAG, "Database tables created");

        String SQL_CREATE_PRODUCT
                = "create table "+ TABLE_NAME +" (" +
                COLUMN_ID_PRODUK + " TEXT primary key, " +
                COLUMN_NAME_KODE_PRODUK + " TEXT, " +
                COLUMN_NAME_NAMA_PRODUK + " TEXT, " +
                COLUMN_NAME_FLAG_FOKUS + " TEXT " +
                ")";
        db.execSQL(SQL_CREATE_PRODUCT);
        Log.d(TAG, "Database tables Product created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String nama_toko, String depot, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_NAMA_TOKO, nama_toko); //Nama Toko
        values.put(KEY_DEPOT, depot); //Depot
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Uid
        values.put(KEY_CREATED_AT, created_at); // Created At
        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public void insertProduct(Product product) {
        Log.d("START INSERT", "PRODUCT");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID_PRODUK, product.getId());
        values.put(COLUMN_NAME_KODE_PRODUK, product.getKode_product());
        values.put(COLUMN_NAME_NAMA_PRODUK, product.getNama_product());
        values.put(COLUMN_NAME_FLAG_FOKUS, product.getFlag_fokus());

        long id = db.insert(TABLE_NAME, null, values);

        db.close();
        Log.d("END INSERT", " "+id);
    }

    public void getProductServer(){
        Log.d("MASUK FUCTION", "PRODUCT");
        String tag_string_req = "req_get_product";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_PRODUCT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get data Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {

                        JSONArray data = jObj.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++){
                            JSONObject objData = data.getJSONObject(i);
                            Product product = new Product();
                            product.setId(objData.getString("id"));
                            product.setKode_product(objData.getString("kode_product"));
                            product.setNama_product(objData.getString("nama_product"));
                            product.setFlag_fokus(objData.getString("flag_fokus"));
                            //listProduct.add(product);
                            insertProduct(product);
                        }
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Log.d("error",errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

            }
        });
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("nama_toko", cursor.getString(2));
            user.put("depot", cursor.getString(3));
            user.put("email", cursor.getString(4));
            user.put("uid", cursor.getString(5));
            user.put("created_at", cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());
        return user;
    }

    public HashMap<String, String> getProductDetails(String kode_product) {
        HashMap<String, String> product = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE kode_product = " + kode_product;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            product.put("id", cursor.getString(0));
            product.put("kode_product", cursor.getString(1));
            product.put("nama_product", cursor.getString(2));
            product.put("flag_fokus", cursor.getString(3));
            //user.put("uid", cursor.getString(5));
            //user.put("created_at", cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching product from Sqlite: " + product.toString());
        return product;
    }

    public List<Product> getAllProductDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME,
                new String[]{COLUMN_ID_PRODUK,
                        COLUMN_NAME_KODE_PRODUK,
                        COLUMN_NAME_NAMA_PRODUK,
                        COLUMN_NAME_FLAG_FOKUS}, null, null, null, null, null);

        List<Product> listData = new ArrayList<>();

        if(c.moveToFirst()) {
            do {
                Product p = new Product();
                p.setId(c.getString(c.getColumnIndex(COLUMN_ID_PRODUK)));
                p.setKode_product(c.getString(c.getColumnIndex(COLUMN_NAME_KODE_PRODUK)));
                p.setNama_product(c.getString(c.getColumnIndex(COLUMN_NAME_NAMA_PRODUK)));
                p.setFlag_fokus(c.getString(c.getColumnIndex(COLUMN_NAME_FLAG_FOKUS)));

                listData.add(p);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return listData;
    }

     /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.delete(TABLE_NAME,null,null);
        db.close();
        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
