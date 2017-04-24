package com.syuria.android.reportsales.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.fragment.HomeFragment;
import com.syuria.android.reportsales.model.DailyReport;
import com.syuria.android.reportsales.model.SelectedProduct;
import com.syuria.android.reportsales.util.NumberTextWatcherForThousand;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 03/03/2017.
 */

public class ProductReportAdapter extends RecyclerView.Adapter<ProductReportAdapter.MyViewHolder> {
    private List<SelectedProduct> selectedProductList;
    private Context context;
    AlertDialog.Builder builder;
    View dialogView, itemView;
    private TextView namaPoduct;
    private TextInputLayout input_layout_volume;
    private EditText input_volume;
    private ProgressDialog pDialog;

    public ProductReportAdapter(List<SelectedProduct> selectedProductList, Context context) {
        this.selectedProductList = selectedProductList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_product_update, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final SelectedProduct selectedProduct = selectedProductList.get(position);
        holder.kode_laporan.setText("LPP-"+String.format("%011d",Integer.parseInt(selectedProduct.getKode_product())));
        holder.nama_product.setText(selectedProduct.getNama_product());
        holder.volume_product.setText(selectedProduct.getVolume());
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedProduct.getFlag_fokus().equals("1")) {
                    showAlertDialog(selectedProduct);
                }else {
                    viewSnackBar(itemView, "Can't be update", "DISMIS");
                }
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
        public TextView nama_product, volume_product, kode_laporan;
        public Button btnEdit;
        public MyViewHolder(View itemView) {
            super(itemView);
            kode_laporan = (TextView) itemView.findViewById(R.id.kode_product);
            nama_product = (TextView) itemView.findViewById(R.id.nama_product);
            volume_product = (TextView) itemView.findViewById(R.id.volume_product);
            btnEdit = (Button) itemView.findViewById(R.id.btnEdit);
        }
    }

    private void showAlertDialog(final SelectedProduct selectedProduct){
        final String id = selectedProduct.getId();
        TextView title = new TextView(itemView.getContext());
        // You Can Customise your Title here
        title.setText("Update Poduct Report");
        title.setBackgroundColor(Color.parseColor("#C51162"));
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder = new AlertDialog.Builder(itemView.getContext());
        dialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_update_product_report, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setCustomTitle(title);

        namaPoduct = (TextView) dialogView.findViewById(R.id.nama_product);
        namaPoduct.setText(selectedProduct.getNama_product());
        input_layout_volume = (TextInputLayout) dialogView.findViewById(R.id.input_layout_volume);
        input_volume = (EditText) dialogView.findViewById(R.id.input_volume);
        input_volume.setText(selectedProduct.getVolume());


        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateVolume()) {return;}

                updateProductReport(selectedProduct.getKode_product(),input_volume.getText().toString().trim());
                dialog.dismiss();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private boolean validateVolume() {
        if (input_volume.getText().toString().trim().isEmpty()) {
            input_layout_volume.setError("Volume Must be filed");
            return false;
        } else {
            input_layout_volume.setErrorEnabled(false);
        }

        return true;
    }

    private void viewSnackBar(View view, String message, String action){
        Snackbar bar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //v.setText("You pressed Dismiss!!");
                    }
                });
        bar.setActionTextColor(context.getResources().getColor(R.color.colorAccent));
        bar.show();
    }

    private void updateProductReport(final String id, final String volume) {
        // Tag used to cancel the request
        String tag_string_req = "req_daily_report";
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Updating ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_UPDATE_PRODUCT_REPORT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Daily Report Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String errorMsg = jObj.getString("error_msg");
                    if (!error) {
                        Bundle bundle = new Bundle();
                        bundle.putString("errorMsg",errorMsg);

                        Fragment fragment = new HomeFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.commit();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        viewSnackBar(itemView,errorMsg,"DISMIS");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    viewSnackBar(itemView,e.getMessage(),"DISMIS");
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Daily Report Error: " + error.getMessage());
                viewSnackBar(itemView,"Connection fail..","DISMIS");
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                //params.put("tanggal", tanggal);
                params.put("volume", volume);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
