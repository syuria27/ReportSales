package com.syuria.android.reportsales.tab_fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.syuria.android.reportsales.R;
import com.syuria.android.reportsales.adapter.DailyReportAdapter;
import com.syuria.android.reportsales.adapter.SelectedProductAdapter;
import com.syuria.android.reportsales.app.AppConfig;
import com.syuria.android.reportsales.app.AppController;
import com.syuria.android.reportsales.fragment.DailyReportFragment;
import com.syuria.android.reportsales.fragment.HomeFragment;
import com.syuria.android.reportsales.fragment.ProductReportFragment;
import com.syuria.android.reportsales.helper.SQLiteHandler;
import com.syuria.android.reportsales.model.DailyReport;
import com.syuria.android.reportsales.model.Product;
import com.syuria.android.reportsales.model.ProductFocus;
import com.syuria.android.reportsales.model.SelectedProduct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by HP on 14/02/2017.
 */

public class InputProductReportFragment extends Fragment {
    private static final String TAG = InputProductReportFragment.class.getSimpleName();
    private MaterialSpinner spnProducts;
    private List<Product> products = new ArrayList<Product>();
    private List<SelectedProduct> selectedProductList;
    private Product productSelected;
    private Button btn_submit_product_report, btnAdd;
    private RecyclerView cardProductReport;
    private SelectedProductAdapter selectedProductAdapter;
    private EditText input_volume;
    private ProgressDialog pDialog;
    private View rootView;
    private SQLiteHandler db;
    private String uid;
    private TextInputLayout inputLayoutVolume;

    public InputProductReportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_input_product_report, container, false);

        // SQLite database handler
        db = new SQLiteHandler(getContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        uid = user.get("uid");

        selectedProductList = new ArrayList<SelectedProduct>();
        cardProductReport = (RecyclerView) rootView.findViewById(R.id.cardProductReport);
        selectedProductAdapter = new SelectedProductAdapter(selectedProductList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        cardProductReport.setLayoutManager(mLayoutManager);
        cardProductReport.setItemAnimator(new DefaultItemAnimator());
        cardProductReport.setAdapter(selectedProductAdapter);

        getProducts(uid);
        spnProducts = (MaterialSpinner) rootView.findViewById(R.id.spnProducts);

        inputLayoutVolume = (TextInputLayout) rootView.findViewById(R.id.input_layout_volume);
        input_volume = (EditText) rootView.findViewById(R.id.input_volume);

        btnAdd = (Button) rootView.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateVolume()) {
                    return;
                }

                productSelected = products.get(spnProducts.getSelectedIndex());
                if (selectedProductAdapter.getItemCount() == 0) {
                    addProduct(productSelected);
                }else {
                    if (contains((ArrayList<SelectedProduct>) selectedProductList,
                                    productSelected.getKode_product())) {
                        viewSnackBar(rootView,"Product already add..","DISMIS");
                    } else {
                        addProduct(productSelected);
                    }
                }
            }
        });

        btn_submit_product_report = (Button) rootView.findViewById(R.id.btn_submit_product_report);
        btn_submit_product_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedProductAdapter.getItemCount() > 0){
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < selectedProductList.size(); i++){
                        jsonArray.put(selectedProductList.get(i).getJSONObject());
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        //jsonObject.put("uid",uid);
                        jsonObject.put("products", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("", "onClick: "+jsonObject.toString());
                    storeProductReport(uid,jsonObject.toString());

                }else {
                    viewSnackBar(rootView,"Add product first..","DISMIS");
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    private boolean validateVolume() {
        if (input_volume.getText().toString().trim().isEmpty()) {
            inputLayoutVolume.setError("Volume Must be filed");
            requestFocus(input_volume);
            return false;
        } else {
            inputLayoutVolume.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    void addProduct(Product p){
        SelectedProduct selectedProduct = new SelectedProduct();
        selectedProduct.setKode_product(p.getKode_product());
        selectedProduct.setNama_product(p.getNama_product());
        selectedProduct.setVolume(input_volume.getText().toString());

        selectedProductList.add(0, selectedProduct);
        selectedProductAdapter.notifyItemInserted(0);
        selectedProductAdapter.notifyItemRangeChanged(0, selectedProductList.size());
        cardProductReport.scrollToPosition(0);
    }

    boolean contains(ArrayList<SelectedProduct> list, String kode_product) {
        for (SelectedProduct item : list) {
            if (item.getKode_product().equals(kode_product)) {
                return true;
            }
        }
        return false;
    }

    public void getProducts(final String uid){
        String tag_string_req = "req_get_daily_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Get Products ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_PRODUCT+"/"+uid, new Response.Listener<String>() {
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
                        JSONArray data = jObj.getJSONArray("products");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject objData = data.getJSONObject(i);
                            Product product = new Product();
                            product.setId(String.valueOf(objData.getInt("id")));
                            product.setKode_product(objData.getString("kode_product"));
                            product.setNama_product(objData.getString("nama_product"));
                            product.setFlag_fokus(String.valueOf(objData.getInt("flag_fokus")));
                            products.add(product);
                        }
                        spnProducts.setItems(products);
                    } else {
                        // Error in login. Get the error message
                        viewSnackBar(rootView,errorMsg,"DISMIS");
                        //spnProducts.setItems();
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

    private void storeProductReport(final String uid, final String products) {
        // Tag used to cancel the request
        String tag_string_req = "req_product_report";
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Submiting ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PRODUCT_REPORT, new Response.Listener<String>() {

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
                params.put("products", products);

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
