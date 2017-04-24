package com.syuria.android.reportsales.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.model.SelectedProduct;

import java.util.List;

/**
 * Created by HP on 03/03/2017.
 */

public class SelectedProductAdapter extends RecyclerView.Adapter<SelectedProductAdapter.MyViewHolder> {
    private List<SelectedProduct> selectedProductList;

    public SelectedProductAdapter(List<SelectedProduct> selectedProductList) {
        this.selectedProductList = selectedProductList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_product_report, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        SelectedProduct selectedProduct = selectedProductList.get(position);
        holder.nama_product.setText(selectedProduct.getNama_product());
        holder.volume_product.setText(selectedProduct.getVolume());
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedProductList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,selectedProductList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedProductList.size();
    }

    public List<SelectedProduct> getSelectedProductList() {
        return selectedProductList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nama_product, volume_product;
        public Button btnCancel;
        public MyViewHolder(View itemView) {
            super(itemView);
            nama_product = (TextView) itemView.findViewById(R.id.nama_product);
            volume_product = (TextView) itemView.findViewById(R.id.volume_product);
            btnCancel = (Button) itemView.findViewById(R.id.btnCancel);
        }
    }
}
