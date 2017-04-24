package com.syuria.android.reportsales.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HP on 27/11/2016.
 */

public class Product {
    private String id;
    private String kode_product;
    private String nama_product;
    private String flag_fokus;

    public Product(){

    }

    public Product(String id, String kode_product, String nama_product, String flag_fokus) {
        this.id = id;
        this.kode_product = kode_product;
        this.nama_product = nama_product;
        this.flag_fokus = flag_fokus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKode_product() {
        return kode_product;
    }

    public void setKode_product(String kode_product) {
        this.kode_product = kode_product;
    }

    public String getNama_product() {
        return nama_product;
    }

    public void setNama_product(String nama_product) {
        this.nama_product = nama_product;
    }

    public String getFlag_fokus() {
        return flag_fokus;
    }

    public void setFlag_fokus(String flag_fokus) {
        this.flag_fokus = flag_fokus;
    }

    @Override
    public String toString() {
        return nama_product;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("kode_product", kode_product);
            //obj.put("nama_product", nama_product);
        } catch (JSONException e) {
            Log.d("JSON : ","DefaultListItem.toString JSONException: "+e.getMessage());
        }
        return obj;
    }
}
