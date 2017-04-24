package com.syuria.android.reportsales.model;

import org.json.JSONObject;

/**
 * Created by HP on 15/02/2017.
 */

public class ProductFocus extends Product {
    private boolean isSelected;

    public ProductFocus(){
        super();
    }

    public ProductFocus(String id, String kode_product, String nama_product, String flag_fokus, boolean isSelected) {
        super(id, kode_product, nama_product, flag_fokus);
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public JSONObject getJSONObject() {
        return super.getJSONObject();
    }
}
