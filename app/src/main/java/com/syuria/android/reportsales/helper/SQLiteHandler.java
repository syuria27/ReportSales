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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
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

     /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();
        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
