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
import com.syuria.android.reportsales.util.NumberTextWatcherForThousand;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by HP on 26/02/2017.
 */

public class DailyReportAdapter extends RecyclerView.Adapter<DailyReportAdapter.MyViewHolder> {

    private List<DailyReport> dailyReports;
    private Context context;
    AlertDialog.Builder builder;
    View dialogView, itemView;
    private TextInputLayout inputLayoutCCM;
    private EditText input_ccm;
    private ProgressDialog pDialog;

    public DailyReportAdapter(Context context,List<DailyReport> dailyReports) {
        this.dailyReports = dailyReports;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_daily_report, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final DailyReport dailyReport = dailyReports.get(position);
        holder.kode_laporan.setText(dailyReport.getKode_laporan());
        holder.tanggal.setText(dailyReport.getTanggal());
        NumberFormat currencyFormater = NumberFormat.getCurrencyInstance(new Locale("id","id"));
        holder.ccm.setText(currencyFormater.format(dailyReport.getCcm()));
        holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date tanggal = sdf.parse(dailyReport.getTanggal());
                    Date CurrentTime = sdf.parse(sdf.format(new Date()));

                    if(CurrentTime.equals(tanggal)){
                        showAlertDialog(dailyReport);
                    }else{
                        viewSnackBar(itemView, "Cannot be update..","DISMIS");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dailyReports.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView kode_laporan, tanggal, ccm;
        public Button btnUpdate;

        public MyViewHolder(final View itemView) {
            super(itemView);
            kode_laporan = (TextView) itemView.findViewById(R.id.textKodeLaporan);
            tanggal = (TextView) itemView.findViewById(R.id.textTanngal);
            ccm = (TextView) itemView.findViewById(R.id.textCCM);
            btnUpdate = (Button) itemView.findViewById(R.id.btnUpdate);
        }
    }

    private void showAlertDialog(final DailyReport dailyReport){
        TextView title = new TextView(itemView.getContext());
        // You Can Customise your Title here
        title.setText("Update Daily Report");
        title.setBackgroundColor(Color.parseColor("#C51162"));
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder = new AlertDialog.Builder(itemView.getContext());
        dialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_update_daily_report, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setCustomTitle(title);

        inputLayoutCCM = (TextInputLayout) dialogView.findViewById(R.id.input_layout_ccm);
        input_ccm = (EditText) dialogView.findViewById(R.id.input_ccm);
        input_ccm.addTextChangedListener(new NumberTextWatcherForThousand(input_ccm,inputLayoutCCM));
        input_ccm.setText(dailyReport.getCcm().toString());


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
                if (!validateCCM()) {return;}

                updateDailyReport(dailyReport.getKode_laporan(),"",input_ccm.getText().toString().trim().replace(",",""));
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

    private boolean validateCCM() {
        if (input_ccm.getText().toString().trim().isEmpty()) {
            inputLayoutCCM.setError("CCM Must be filed");
            return false;
        } else {
            inputLayoutCCM.setErrorEnabled(false);
        }

        return true;
    }

    private void updateDailyReport(final String kode_laporan, final String tanggal, final String ccm) {
        // Tag used to cancel the request
        String tag_string_req = "req_daily_report";
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Updating ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_UPDATE_DAILY_REPORT, new Response.Listener<String>() {

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
                params.put("kode_laporan", kode_laporan);
                //params.put("tanggal", tanggal);
                params.put("ccm", ccm);

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
}
