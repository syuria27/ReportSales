package com.syuria.android.reportsales.tab_fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.helper.SQLiteHandler;
import com.syuria.android.reportsales.util.NumberTextWatcherForThousand;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by HP on 14/02/2017.
 */

public class InputDailyReportFragment extends Fragment {
    private static final String TAG = InputDailyReportFragment.class.getSimpleName();
    private TextView textDailyReportTanggal, textDailyReportToko, textDailyReportDepot;
    private TextInputLayout inputLayoutCCM, inputLayoutRM;
    private EditText input_ccm, input_rm;
    private Button btn_submit_daily_report;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    private String uid;
    private View rootView;

    public InputDailyReportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_input_daily_report, container, false);
        textDailyReportTanggal = (TextView) rootView.findViewById(R.id.textDailyReportTanggal);
        textDailyReportToko = (TextView) rootView.findViewById(R.id.textDailyReportToko);
        textDailyReportDepot = (TextView) rootView.findViewById(R.id.textDailyReportDepot);

        // SQLite database handler
        db = new SQLiteHandler(getContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String nama_toko = user.get("nama_toko");
        String depot = user.get("depot");
        uid = user.get("uid");

        textDailyReportTanggal.setText(date);
        textDailyReportToko.setText(nama_toko);
        textDailyReportDepot.setText(depot);

        inputLayoutCCM = (TextInputLayout) rootView.findViewById(R.id.input_layout_ccm);
        input_ccm = (EditText) rootView.findViewById(R.id.input_ccm);
        input_ccm.addTextChangedListener(new NumberTextWatcherForThousand(input_ccm,inputLayoutCCM));

        inputLayoutRM = (TextInputLayout) rootView.findViewById(R.id.input_layout_rm);
        input_rm = (EditText) rootView.findViewById(R.id.input_rm);
        input_rm.addTextChangedListener(new NumberTextWatcherForThousand(input_rm,inputLayoutRM));

        btn_submit_daily_report = (Button) rootView.findViewById(R.id.btn_submit_daily_report);
        btn_submit_daily_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateCCM()) {
                    return;
                }

                if (!validateRM()) {
                    return;
                }
                storeDailyReport(uid,
                        input_ccm.getText().toString().trim().replace(",",""),
                        input_rm.getText().toString().trim().replace(",",""));
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    private boolean validateCCM() {
        if (input_ccm.getText().toString().trim().isEmpty()) {
            inputLayoutCCM.setError("CCM Must be filed");
            requestFocus(input_ccm);
            return false;
        } else {
            inputLayoutCCM.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateRM() {
        if (input_rm.getText().toString().trim().isEmpty()) {
            inputLayoutRM.setError("RM Must be filed");
            requestFocus(input_rm);
            return false;
        } else {
            inputLayoutRM.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void storeDailyReport(final String uid, final String ccm, final String rm) {
        // Tag used to cancel the request
        String tag_string_req = "req_daily_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Submiting ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DAILY_REPORT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Daily Report Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String errorMsg = jObj.getString("error_msg");
                    if (!error) {
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                    }
                } catch (JSONException e) {
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
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("kode_spg", uid);
                params.put("ccm", ccm);
                params.put("rm", rm);
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
