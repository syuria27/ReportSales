package com.syuria.android.reportsales.tab_fragment;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.adapter.DailyReportAdapter;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.decoration.DividerItemDecoration;
import com.syuria.android.reportsales.helper.SQLiteHandler;
import com.syuria.android.reportsales.model.DailyReport;
import com.syuria.android.reportsales.util.DateDisplayUtils;
import com.syuria.android.reportsales.widget.SimpleDatePickerDialog;
import com.syuria.android.reportsales.widget.SimpleDatePickerDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by HP on 14/02/2017.
 */

public class HistoryDailyReportFragment extends Fragment implements SimpleDatePickerDialog.OnDateSetListener {
    private static final String TAG = HistoryDailyReportFragment.class.getSimpleName();
    private ImageView imgTanggal;
    private TextView txtTanggal;
//    private Button btn_get_daily_report;
    private SQLiteHandler db;
    private String kode_sales;
    private List<DailyReport> dailyReportList;
    private ProgressDialog pDialog;
    private RecyclerView lvDailyReport;
    private View rootView;
//    private DailyReportAdapter dailyReportAdapter;

    public HistoryDailyReportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history_daily_report, container, false);

        lvDailyReport = (RecyclerView) rootView.findViewById(R.id.lvDailyReport);
        lvDailyReport.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        lvDailyReport.setLayoutManager(mLayoutManager);
        lvDailyReport.setItemAnimator(new DefaultItemAnimator());

        db = new SQLiteHandler(getContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        kode_sales = user.get("uid");

        txtTanggal = (TextView) rootView.findViewById(R.id.txtTanggal);
        imgTanggal = (ImageView) rootView.findViewById(R.id.imgTanggal);
        imgTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySimpleDatePickerDialogFragment();

            }
        });

        return rootView;
    }

    private void getHistory(){
        String bulanTahun = txtTanggal.getText().toString();
        int posSpace = bulanTahun.indexOf(" ");
        int tahun = Integer.parseInt(bulanTahun.substring(posSpace+1, posSpace+5));
        String bulan = bulanTahun.substring(0,posSpace);
        int bulani = getMonthNumber(bulan);
        getDailyReport(kode_sales,bulani,tahun);
    }

    @Override
    public void onDateSet(int year, int monthOfYear) {
        txtTanggal.setText(DateDisplayUtils.formatMonthYear(year, monthOfYear));
        getHistory();
    }

    private void displaySimpleDatePickerDialogFragment() {
        SimpleDatePickerDialogFragment datePickerDialogFragment;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        datePickerDialogFragment = SimpleDatePickerDialogFragment.getInstance(
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        datePickerDialogFragment.setOnDateSetListener(this);
        datePickerDialogFragment.show(getChildFragmentManager(), null);
    }

    public void getDailyReport(final String uid, final int bulan, final int tahun){
        String tag_string_req = "req_get_daily_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Get Data ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_DAILY_REPORT+"/"+uid+"/"+bulan+"/"+tahun, new Response.Listener<String>() {
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
                        dailyReportList = new ArrayList<DailyReport>();
                        JSONArray data = jObj.getJSONArray("history");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject objData = data.getJSONObject(i);
                            DailyReport dr = new DailyReport();
                            dr.setKode_laporan(objData.getString("kode_laporan"));
                            dr.setTanggal(objData.getString("tanggal"));
                            dr.setCcm(new BigDecimal(objData.getString("ccm")));
                            dr.setRm(new BigDecimal(objData.getString("rm")));
                            dailyReportList.add(dr);
                        }
                        viewSnackBar(rootView,errorMsg,"DISMIS");

                        lvDailyReport.setAdapter(new DailyReportAdapter(getContext(),dailyReportList));

                    } else {
                        // Error in login. Get the error message
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                        lvDailyReport.setAdapter(null);
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
                lvDailyReport.setAdapter(null);
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

    public static int getMonthNumber(String month) {
        int monthNumber = 0;
        if (month == null) {
            return monthNumber;
        }

        switch (month) {
            case "JANUARY":
                monthNumber = 1;
                break;
            case "JANUARI":
                monthNumber = 1;
                break;
            case "FEBRUARY":
                monthNumber = 2;
                break;
            case "FEBRUARI":
                monthNumber = 2;
                break;
            case "MARCH":
                monthNumber = 3;
                break;
            case "MARET":
                monthNumber = 3;
                break;
            case "APRIL":
                monthNumber = 4;
                break;
            case "MAY":
                monthNumber = 5;
                break;
            case "MEI":
                monthNumber = 5;
                break;
            case "JUNE":
                monthNumber = 6;
                break;
            case "JUNI":
                monthNumber = 6;
                break;
            case "JULY":
                monthNumber = 7;
                break;
            case "JULI":
                monthNumber = 7;
                break;
            case "AUGUST":
                monthNumber = 8;
                break;
            case "AGUSTUS":
                monthNumber = 8;
                break;
            case "SEPTEMBER":
                monthNumber = 9;
                break;
            case "OCTOBER":
                monthNumber = 10;
                break;
            case "OKTOBER":
                monthNumber = 10;
                break;
            case "NOVEMBER":
                monthNumber = 11;
                break;
            case "DECEMBER":
                monthNumber = 12;
                break;
            case "DESEMBER":
                monthNumber = 12;
                break;
            default:
                monthNumber = 0;
                break;
        }
        return monthNumber;
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
