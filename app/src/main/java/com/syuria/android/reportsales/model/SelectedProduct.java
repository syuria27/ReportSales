package com.syuria.android.reportsales.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HP on 03/03/2017.
 */

public class SelectedProduct extends Product {
    private String volume;

    public SelectedProduct(String volume) {
        this.volume = volume;
    }

    public SelectedProduct(String id, String kode_product, String nama_product, String flag_fokus, String volume) {
        super(id, kode_product, nama_product, flag_fokus);
        this.volume = volume;
    }

    public SelectedProduct() {

    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = super.getJSONObject();
        try {
            obj.put("volume", Double.parseDouble(volume));
        } catch (JSONException e) {
            Log.d("JSON : ","DefaultListItem.toString JSONException: "+e.getMessage());
        }
        return obj;
    }
}
