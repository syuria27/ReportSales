package com.syuria.android.reportsales.tab_fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.adapter.DailyReportAdapter;
import com.syuria.android.reportsales.adapter.ProductReportAdapter;
import com.syuria.android.reportsales.adapter.SelectedProductAdapter;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.decoration.DividerItemDecoration;
import com.syuria.android.reportsales.helper.SQLiteHandler;
import com.syuria.android.reportsales.model.DailyReport;
import com.syuria.android.reportsales.model.SelectedProduct;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HP on 14/02/2017.
 */

public class HistoryProductReportFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = HistoryProductReportFragment.class.getSimpleName();
    private ImageView imgTanggal;
    private TextView txtTanggal;
    private ProgressDialog pDialog;
    private View rootView;
    private RecyclerView cardProductReport;
    private List<SelectedProduct> selectedProducts;
    private SQLiteHandler db;
    private String uid;

    public HistoryProductReportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history_product_report, container, false);

        db = new SQLiteHandler(getContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        uid = user.get("uid");


        txtTanggal = (TextView) rootView.findViewById(R.id.txtTanggal);
        imgTanggal = (ImageView) rootView.findViewById(R.id.imgTanggal);
        imgTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(HistoryProductReportFragment.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(false);
                dpd.showYearPickerFirst(false);
                dpd.setAccentColor(Color.parseColor("#FF80AB"));
                dpd.setTitle("Select Date");
                dpd.show(getActivity().getFragmentManager(), "startbill");
            }
        });

        cardProductReport = (RecyclerView) rootView.findViewById(R.id.cardProductReport);
        cardProductReport.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        cardProductReport.setLayoutManager(mLayoutManager);
        cardProductReport.setItemAnimator(new DefaultItemAnimator());

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear = monthOfYear + 1;
        String bulan = getMonth(monthOfYear);
        String date = dayOfMonth + " " + bulan + " " + year;
        txtTanggal.setText(date);
        String tanggal = year+"-"+monthOfYear+"-"+dayOfMonth;
        getProductReport(uid,tanggal);
    }

    public void getProductReport(final String uid, final String tanggal){
        String tag_string_req = "req_get_daily_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Get Data ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_HISTORY_PRODUCT_REPORT+"/"+uid+"/"+tanggal, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get data Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String errorMsg = jObj.getString("error_msg");
                    // Check for error node in json
                    if (!error) {
                        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                        selectedProducts = new ArrayList<SelectedProduct>();
                        JSONArray data = jObj.getJSONArray("history");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject objData = data.getJSONObject(i);
                            SelectedProduct selectedProduct = new SelectedProduct();
                            selectedProduct.setKode_product(objData.getString("id"));
                            selectedProduct.setNama_product(objData.getString("nama_product"));
                            selectedProduct.setVolume(objData.getString("volume"));
                            if (objData.getString("tanggal").equals(date)){
                                selectedProduct.setFlag_fokus("1");
                            }else {
                                selectedProduct.setFlag_fokus("0");
                            }

                            selectedProducts.add(selectedProduct);
                        }
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                        cardProductReport.setAdapter(new ProductReportAdapter(selectedProducts, getContext()));
                        //lvDailyReport.setAdapter(new DailyReportAdapter(getContext(),dailyReportList));

                    } else {
                        // Error in login. Get the error message
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                        cardProductReport.setAdapter(null);
                        //lvDailyReport.setAdapter(null);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    viewSnackBar(rootView,e.getMessage(),"DISMIS");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String responseBody = new String( error.networkResponse.data, "utf-8" );
                    JSONObject jsonObject = new JSONObject( responseBody );
                    viewSnackBar(rootView,jsonObject.getString("error_msg"),"DISMIS");
                } catch ( JSONException e ) {
                    viewSnackBar(rootView,"Connection fail..","DISMIS");
                } catch (UnsupportedEncodingException ue_error){
                    viewSnackBar(rootView,"Connection fail..","DISMIS");
                } catch (Exception e){
                    viewSnackBar(rootView,"Connection fail..","DISMIS");
                }
                cardProductReport.setAdapter(null);
                hideDialog();
            }
        });
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
        bar.setActionTextColor(getResources().getColor(R.color.colorAccent));
        bar.show();
    }

    public String getMonth(int bln){
        String month = "";

        switch (bln){
            case 1 :
                month = "JANUARY";
                break;
            case 2 :
                month = "FEBRUARY";
                break;
            case 3 :
                month = "MARCH";
                break;
            case 4 :
                month = "APRIL";
                break;
            case 5 :
                month = "MAY";
                break;
            case 6 :
                month = "JUNE";
                break;
            case 7 :
                month = "JULY";
                break;
            case 8 :
                month = "AUGUST";
                break;
            case 9 :
                month = "SEPTEMBER";
                break;
            case 10 :
                month = "OCTOBER";
                break;
            case 11 :
                month = "NOVEMBER";
                break;
            case 12 :
                month = "DECEMBER";
                break;
            default:
                month = "BULAN";
                break;
        }

        return month;
    }
}
