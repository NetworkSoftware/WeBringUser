package pro.network.webring;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import pro.network.webring.app.AppConfig;
import pro.network.webring.app.AppController;
import pro.network.webring.app.BaseActivity;
import pro.network.webring.app.DatabaseHelperYalu;
import pro.network.webring.cart.CartActivity;
import pro.network.webring.chip.CategoryAdapter;
import pro.network.webring.chip.ChipBean;
import pro.network.webring.chip.OnChip;
import pro.network.webring.orders.MyOrderPage;
import pro.network.webring.product.BannerActivity;
import pro.network.webring.product.CategoryBeen;
import pro.network.webring.product.ProductActivity;
import pro.network.webring.product.ProductItemClick;
import pro.network.webring.product.ProductListAdapter;
import pro.network.webring.product.ProductListBean;

import com.github.rubensousa.gravitysnaphelper.GravitySnapRecyclerView;
import com.google.android.material.button.MaterialButton;
import com.network.moeidbannerlibrary.banner.BannerBean;
import com.network.moeidbannerlibrary.banner.BannerLayout;
import com.network.moeidbannerlibrary.banner.BaseBannerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pro.network.webring.app.AppConfig.CATEGORIES_GET_ALL;

public class HomeActivity extends BaseActivity implements ProductItemClick , OnChip {

    ProgressDialog pDialog;
    private String TAG = getClass().getSimpleName();
    private DatabaseHelperYalu db;
    RecyclerView recycler_product;
    private List<CategoryBeen> categoryList = new ArrayList<>();
    CartActivity.OnCartItemChange onCartItemChange;
    BannerLayout banner;
    private List<ProductListBean> productList = new ArrayList<>();
    private MaterialButton viewAllBtn;
    private TextView cart_badge;
    private CategoryAdapter categoryAdapter;
    ProgressBar categoryProgress;
    ArrayList<ChipBean> chipBeans = new ArrayList<>();
    private ArrayList<ChipBean> category = new ArrayList<>();
    ProductListAdapter productListAdapter;
    SharedPreferences sharedpreferences;
    @Override
    protected void startDemo() {
        setContentView(R.layout.activity_home);
        sharedpreferences = getApplicationContext().getSharedPreferences(AppConfig.mypreference, Context.MODE_PRIVATE);

        if (!sharedpreferences.contains(AppConfig.isLogin) || !sharedpreferences.getBoolean(AppConfig.isLogin, false)) {
        }

        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString(AppConfig.userId, sharedpreferences.getString(AppConfig.user_id, "guest"));
        edit.commit();

        if (sharedpreferences.contains(AppConfig.usernameKey)) {
            getSupportActionBar().setSubtitle(sharedpreferences.getString(AppConfig.usernameKey, ""));
        }
        db = new DatabaseHelperYalu(getApplicationContext());
        recycler_product = findViewById(R.id.recycler_product);
        categoryProgress = findViewById(R.id.categoryProgress);
        db = new DatabaseHelperYalu(getApplicationContext());
        recycler_product = findViewById(R.id.recycler_product);
        categoryList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(getApplication(), productList, this, sharedpreferences);
        final GridLayoutManager addManager1 = new GridLayoutManager(getApplication(), 2);
        recycler_product.setLayoutManager(addManager1);
        recycler_product.setAdapter(productListAdapter);
        fetchBanner();
        showCategories();


        banner = findViewById(R.id.Banner);
        viewAllBtn = findViewById(R.id.viewAllBtn);
        viewAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AllProductActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showCategories()
    {
        GravitySnapRecyclerView categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryRecyclerView.setHasFixedSize(true);
        categoryAdapter = new CategoryAdapter(getApplication(), chipBeans, this, "");
        categoryRecyclerView.setAdapter(categoryAdapter);
        getAllCategories();

    }
    private void getAllCategories() {
        String tag_string_req = "req_register";
        categoryProgress.setVisibility(View.VISIBLE);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                CATEGORIES_GET_ALL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                categoryProgress.setVisibility(View.GONE);
                try {
                    JSONObject jObj = new JSONObject(response);
                    int success = jObj.getInt("success");

                    if (success == 1) {
                        JSONArray jsonArray = jObj.getJSONArray("data");
                        chipBeans = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            chipBeans.add(new ChipBean(jsonArray.getJSONObject(i)
                                    .getString("title").toUpperCase(),
                                    jsonArray.getJSONObject(i).getString("image")));
                        }
                        categoryAdapter.notifyData(chipBeans);
                    }
                } catch (JSONException e) {
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                categoryProgress.setVisibility(View.GONE);
            }
        }) {
            protected Map<String, String> getParams() {
                HashMap localHashMap = new HashMap();
                return localHashMap;
            }
        };
        strReq.setRetryPolicy(AppConfig.getPolicy());
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void getFromServer(final String searchKey) {
        String tag_string_req = "req_register";
        // showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.PRODUCT_GET_ALL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Register Response: ", response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    int success = jObj.getInt("success");
                    if (success == 1) {
                        JSONArray jsonArray = jObj.getJSONArray("data");
                        productList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ProductListBean productListBean = new ProductListBean();
                            productListBean.setId(jsonObject.getString("id"));
                            productListBean.setBrand(jsonObject.getString("brand"));
                            productListBean.setPrice(jsonObject.getString("price"));
                            productListBean.setRam(jsonObject.getString("ram"));
                            productListBean.setRom(jsonObject.getString("rom"));
                            productListBean.setModel(jsonObject.getString("model"));
                            productListBean.setImage(jsonObject.getString("image"));
                            productListBean.setDescription(jsonObject.getString("description"));
                            productListBean.setStock_update(jsonObject.getString("stock_update"));
                            productList.add(productListBean);
                            if (i == 5) {
                                break;
                            }
                        }
                        //  productListAdapter.notifyData(productList);
                        viewAllBtn.setText("View " + jsonArray.length() + " Products");
                    } else {
                        Toast.makeText(getApplication(), jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("xxxxxxxxxxx", e.toString());
                    Toast.makeText(getApplication(), "Some Network Error.Try after some time", Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(),
                        "Some Network Error.Try after some time", Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                HashMap localHashMap = new HashMap();
                localHashMap.put("searchKey", searchKey);
                return localHashMap;
            }
        };
        strReq.setRetryPolicy(AppConfig.getPolicy());
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void fetchBanner() {
        String tag_string_req = "req_register";
        // showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.BANNERS_GET_ALL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Register Response: ", response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    int success = jObj.getInt("success");

                    if (success == 1) {
                        JSONArray jsonArray = jObj.getJSONArray("data");
                        List<BannerBean> bannerBeans = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            BannerBean bannerBean = new BannerBean();
                            bannerBean.setImages(jsonObject.getString("banner"));
                            bannerBean.setId(jsonObject.getString("id"));
                            bannerBean.setDescription(jsonObject.getString("description"));
                            bannerBeans.add(bannerBean);
                        }
                        BaseBannerAdapter webBannerAdapter = new BaseBannerAdapter(getApplicationContext(), bannerBeans);
                        webBannerAdapter.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                            @Override
                            public void onItemClick(BannerBean bannerBean) {
                                Intent intent = new Intent(HomeActivity.this, BannerActivity.class);
                                intent.putExtra("description", bannerBean.getDescription());
                                intent.putExtra("image", bannerBean.getImages());
                                startActivityForResult(intent,100);
                            }
                        });
                        banner.setAdapter(webBannerAdapter);

                    } else {
                        Toast.makeText(getApplication(), jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("xxxxxxxxxxx", e.toString());
                    Toast.makeText(getApplication(), "Some Network Error.Try after some time", Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(),
                        "Some Network Error.Try after some time", Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                HashMap localHashMap = new HashMap();
                return localHashMap;
            }
        };
        strReq.setRetryPolicy(AppConfig.getPolicy());
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void addtocart(ProductListBean productListBean) {
        productListBean.setQty("1");
        db.insertMainbeanyalu(productListBean, sharedpreferences.getString(AppConfig.user_id, ""));
        Toast.makeText(getApplication(), "Item added successfully", Toast.LENGTH_SHORT).show();
        if (onCartItemChange != null) {
            onCartItemChange.onCartChange();
        }
        setupBadge();
        productListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onProductClick(ProductListBean productListBean) {

        Intent intent = new Intent(getApplication(), ProductActivity.class);
        intent.putExtra("data", productListBean);
        startActivity(intent);
    }

    @Override
    public void onCartClick(ProductListBean productListBean) {

        addtocart(productListBean);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.front_menu, menu);


        final MenuItem menuItem = menu.findItem(R.id.cart);

        View actionView = menuItem.getActionView();
        cart_badge = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.settings:
                Intent intenti = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intenti);
                return true;


            case R.id.cart:
                Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(intent);
                return true;

            case R.id.order:
                Intent intentk = new Intent(HomeActivity.this, MyOrderPage.class);
                startActivity(intentk);

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBadge() {
        int cartCountYalu = db.getCartCountYalu(sharedpreferences.getString(AppConfig.user_id, ""));
        if (cart_badge != null) {
            if (cartCountYalu == 0) {
                if (cart_badge.getVisibility() != View.GONE) {
                    cart_badge.setVisibility(View.GONE);
                }
            } else {
                cart_badge.setText(String.valueOf(Math.min(cartCountYalu, 99)));
                if (cart_badge.getVisibility() != View.VISIBLE) {
                    cart_badge.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    private void fetchProductList(final String searchKey) {
        String tag_string_req = "req_register";
        // showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.PRODUCT_GET_ALL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Register Response: ", response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    int success = jObj.getInt("success");
                    if (success == 1) {
                        JSONArray jsonArray = jObj.getJSONArray("data");
                        productList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ProductListBean productListBean = new ProductListBean();
                            productListBean.setId(jsonObject.getString("id"));
                            productListBean.setBrand(jsonObject.getString("brand"));
                            productListBean.setPrice(jsonObject.getString("price"));
                            productListBean.setRam(jsonObject.getString("ram"));
                            productListBean.setRom(jsonObject.getString("rom"));
                            productListBean.setModel(jsonObject.getString("model"));
                            productListBean.setImage(jsonObject.getString("image"));
                            productListBean.setDescription(jsonObject.getString("description"));
                            productListBean.setStock_update(jsonObject.getString("stock_update"));
                            productList.add(productListBean);
                            if (i == 5) {
                                break;
                            }
                        }

                        productListAdapter.notifyData(productList);
                        viewAllBtn.setText("View  Products");
                    } else {
                        Toast.makeText(getApplication(), jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("xxxxxxxxxxx", e.toString());
                    Toast.makeText(getApplication(), "Some Network Error.Try after some time", Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(),
                        "Some Network Error.Try after some time", Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                HashMap localHashMap = new HashMap();
                localHashMap.put("searchKey", searchKey);
                return localHashMap;
            }
        };
        strReq.setRetryPolicy(AppConfig.getPolicy());
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    @Override
    protected void onStart() {
        super.onStart();
        /*   setupBadge();*/
        fetchProductList("");
        try {
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString(AppConfig.userId, sharedpreferences.getString(AppConfig.user_id, "guest"));
            edit.commit();
            if (sharedpreferences.contains(AppConfig.usernameKey)) {
                getSupportActionBar().setSubtitle(sharedpreferences.getString(AppConfig.usernameKey, ""));
            }
            setupBadge();
        } catch (Exception e) {

        }
    }

    private void startAllProduct(String view) {
        Intent intent = new Intent(HomeActivity.this, AllProductActivity.class);
        intent.putExtra("type", view);
        startActivity(intent);
    }


    @Override
    public void onItemClick(String type) {
        startAllProduct(type);
    }

}
