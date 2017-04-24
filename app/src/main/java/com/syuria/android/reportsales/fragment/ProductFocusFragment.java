package com.syuria.android.reportsales.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.adapter.ProductFocusAdapter;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.model.Product;
import com.syuria.android.reportsales.model.ProductFocus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 14/02/2017.
 */

public class ProductFocusFragment extends Fragment {
    private static String TAG = "ProductFocusFragment";
    ProgressDialog pDialog;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private View rootView;

    private List<ProductFocus> productFocusList;
    private Button btnSubmit;

    public ProductFocusFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_product_focus, container, false);

        getProductFocus("SPG-0046");

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rc_product_focus);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        btnSubmit = (Button) rootView.findViewById(R.id.btn_submit_product_focus);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ProductFocus> productFocuses = ((ProductFocusAdapter) mAdapter)
                        .getProductFocusList();
                List<ProductFocus> selectedProductFocuses = new ArrayList<ProductFocus>();

                for (int i = 0; i < productFocuses.size(); i++) {
                    ProductFocus productFocus = productFocuses.get(i);
                    if (productFocus.isSelected() == true) {
                        selectedProductFocuses.add(productFocus);
                    }
                }

                if (selectedProductFocuses.size() > 0) {

                    /*for (int i=0; i<selectedProductFocuses.size(); i++){
                        ProductFocus productFocus = new ProductFocus();
                        productFocus = selectedProductFocuses.get(i);
                        ((ProductFocusAdapter) mAdapter).remove(productFocus);
                    }*/

                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < selectedProductFocuses.size(); i++){
                        jsonArray.put(selectedProductFocuses.get(i).getJSONObject());
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        //jsonObject.put("uid",uid);
                        jsonObject.put("focuses", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    storeProductFocus("SPG-0046",jsonObject.toString());
                }else {
                    viewSnackBar(rootView,"Choose product first..","DISMIS");
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public void getProductFocus(final String uid){
        String tag_string_req = "req_get_daily_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Get Products ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_PRODUCT_FOCUS+"/"+uid, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get data Response: " + response.toString());
                hideDialog();
                try {
                    //products =
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String errorMsg = jObj.getString("error_msg");
                    // Check for error node in json
                    if (!error) {
                        JSONArray data = jObj.getJSONArray("focuses");
                        productFocusList = new ArrayList<ProductFocus>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject objData = data.getJSONObject(i);
                            ProductFocus product = new ProductFocus();
                            product.setId(objData.getString("id"));
                            product.setKode_product(objData.getString("kode_product"));
                            product.setNama_product(objData.getString("nama_product"));
                            product.setFlag_fokus(objData.getString("status"));
                            product.setSelected(false);
                            productFocusList.add(product);
                        }
                        mAdapter = new ProductFocusAdapter(productFocusList);
                        // set the adapter object to the Recyclerview
                        mRecyclerView.setAdapter(mAdapter);
                    } else {
                        // Error in login. Get the error message
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                        mRecyclerView.setAdapter(null);
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
                Log.e(TAG, "Data error: " + error.getMessage());
                viewSnackBar(rootView,"Connection fail..","DISMIS");
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

    private void storeProductFocus(final String uid, final String focuses) {
        // Tag used to cancel the request
        String tag_string_req = "req_product_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Submiting ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PRODUCT_FOCUS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Daily Report Response: " + response.toString());
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
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.commit();
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
                Log.e(TAG, "Daily Report Error: " + error.getMessage());
                viewSnackBar(rootView,"Connection fail..","DISMIS");
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", uid);
                params.put("focuses", focuses);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
