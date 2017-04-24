package com.syuria.android.reportsales.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.model.ProductFocus;

import java.util.List;

/**
 * Created by HP on 15/02/2017.
 */

public class ProductFocusAdapter extends RecyclerView.Adapter<ProductFocusAdapter.ViewHolder> {
    private List<ProductFocus> productFocusList;

    public ProductFocusAdapter(List<ProductFocus> productFocusList) {
        this.productFocusList = productFocusList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_product_focus, null);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;

        holder.kode_product.setText(productFocusList.get(position).getKode_product());
        holder.nama_product.setText(productFocusList.get(position).getNama_product());
        holder.chkSelected.setChecked(productFocusList.get(position).isSelected());
        holder.chkSelected.setTag(productFocusList.get(position));

        holder.chkSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                ProductFocus productFocus = (ProductFocus) cb.getTag();

                productFocus.setSelected(cb.isChecked());
                productFocusList.get(pos).setSelected(cb.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productFocusList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView kode_product;
        public TextView nama_product;
        public CheckBox chkSelected;
        public ProductFocus productFocus;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            kode_product = (TextView) itemLayoutView.findViewById(R.id.kode_product);
            nama_product = (TextView) itemLayoutView.findViewById(R.id.nama_product);
            chkSelected = (CheckBox) itemLayoutView.findViewById(R.id.chkSelected);

        }

    }

    // method to access in activity after updating selection
    public List<ProductFocus> getProductFocusList() {
        return productFocusList;
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(ProductFocus data) {
        int position = productFocusList.indexOf(data);
        productFocusList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, productFocusList.size());
    }
}
