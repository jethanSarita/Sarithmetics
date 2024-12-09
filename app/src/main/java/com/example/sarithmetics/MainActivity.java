package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sarithmetics.databinding.PopupItemAddBinding;
import com.example.sarithmetics.databinding.PopupItemMaxBinding;
import com.example.sarithmetics.databinding.PopupPremiumBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListAdapterItemFirebase.OnItemClickListener, ListAdapterEmployeeFirebase.OnItemClickListener, ListAdapterRestockFirebase.OnItemClickListener, AdapterView.OnItemSelectedListener, ListAdapterHistoryFirebase.OnItemClickListener, ListAdapterCategoryFirebase.OnItemClickListener {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String TAG = "firebaseDatabase MainAct";

    FloatingActionButton add_button, restock_button;

    String punch_in_code;

    //Toolbar
    Toolbar toolbar;
    ImageView premium_icon;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    ImageView settings, homeIcon, cart_button, eye_open_button, eye_close_button;

    RelativeLayout home_layout, items_layout, insights_layout, restock_layout, history_layout;

    SessionManager sessionManager;
    TextView profileFnLNameBusinessOwner, profileFnLNameEmployee, tv_business_code, maTvStatusNotSync, maTvStatusPending, amTvCurrentPunchInCode, employeeStatus, profileFnLUserType, item_total_sales_vol_tv, item_revenue_tv, item_turnover_rate_tv, top1_tv, top2_tv, top3_tv;

    /*Items*/
    TextView items_counter;
    /*History*/
    TextView history_counter;
    TextView history_current_total;

    ListAdapterItemFirebase listAdapterItemFirebase;
    ListAdapterCategoryFirebase listAdapterCategoryFirebase;
    ListAdapterEmployeeFirebase listAdapterEmployeeFirebase;
    ListAdapterRestockFirebase listAdapterRestockFirebase;
    ListAdapterHistoryFirebase listAdapterHistoryFirebase;
    RecyclerView rvItems, rvEmployees, rvRestocking, rvHistory, rvCategory;
    Spinner insight_item_spinner, insight_context_spinner, category_spinner;
    ArrayList<String> insight_item_list, insight_context_list;
    ArrayAdapter<String> insight_item_adapter, insight_context_adapter;
    LinearLayout boxBusinessCode, llEmployeeLayoutYesSync, llEmployeeLayoutNoSync, llEmployeeLayoutPendingSync;
    androidx.appcompat.widget.SearchView itemSearchBar, restockingSearchBar;
    EditText etBusinessCode, etPunchInCode;
    Button btnEnterBusinessCode, amBtnGeneratePunchInCode, btnEnterPunchInCode;

    //ScrollView maSvItems;
    RandomHelper randomHelper;
    MenuItem nav_insights, nav_restock, nav_services;
    ImageButton category_plus_btn, history_filter_button;

    //Database
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser, lUser;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference user_ref, items_ref, cart_ref, business_ref, business_code_ref, history_ref,
            category_ref, subscription_ref;
    Query item_query, employee_query, restock_query, history_query, category_query, history_filtered_query;

    ArrayAdapter<String> adp;

    //Ads
    AdView mAdView;

    /*Insight*/
    PieChart pieChart;
    ArrayList<PieEntry> entries;
    PieDataSet pieDataSet;
    PieData pieData;

    /*Loading system*/
    SystemLoading systemLoading;
    long TIMEOUT_DURATION;
    Handler time_out_handler;
    Runnable time_out_runnable;

    /*Subscription System*/
    int subscription_type;
    /*Items*/
    boolean is_at_max_free_items = false;
    boolean is_at_max_premium1_items = false;
    boolean is_at_max_premium2_items = false;
    boolean is_at_max_debug_free_items = false;
    boolean is_at_max_debug_premium_items = false;
    /*Transactions*/
    boolean is_at_max_free_transactions = false;
    boolean is_at_max_premium1_transactions = false;
    boolean is_at_max_premium2_transactions = false;
    boolean is_at_max_debug_free_transactions = false;
    boolean is_at_max_debug_premium_transactions = false;
    /*Categories*/
    boolean is_at_max_free_categories = false;
    boolean is_at_max_premium1_categories = false;
    boolean is_at_max_premium2_categories = false;
    boolean is_at_max_debug_free_categories = false;
    boolean is_at_max_debug_premium_items_categories = false;

    boolean lock_marked = false;

    /*New Subscription System*/
    boolean at_max_items = false;
    boolean at_max_transactions = false;
    boolean at_max_categories = false;

    long item_count_general = 0;
    long item_count_marked = 0;
    long item_count_unmarked = 0;
    long transaction_count = 0;
    long category_count = 0;

    String current_checkout_link;

    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
        return new JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put("parameters", new JSONObject()
                        .put("gateway", "example")
                        .put("gatewayMerchantId", "exampleGatewayMerchantId")
                );
    }

    OkHttpClient client;

    /*Haptic Feedback*/
    Vibrator vibrator;
    final int VIB_DURATION = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        randomHelper = new RandomHelper();

        punch_in_code = null;

        /*firebase*/
        firebaseDatabase = FirebaseDatabase.getInstance(DB);
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        /*session*/
        sessionManager = new SessionManager(getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();

        /*general hooks*/
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        premium_icon = findViewById(R.id.premium_icon);
        homeIcon = findViewById(R.id.homeIcon);

        /*navigation*/
        nav_insights = navigationView.getMenu().findItem(R.id.nav_insights);
        nav_restock = navigationView.getMenu().findItem(R.id.nav_restock);
        nav_services = navigationView.getMenu().findItem(R.id.nav_services);

        /*home page layout*/
        profileFnLNameEmployee = findViewById(R.id.home_employee_name_tv);
        profileFnLNameBusinessOwner = findViewById(R.id.home_owner_profile_name);
        tv_business_code = findViewById(R.id.home_owner_business_code_tv);
        rvItems = findViewById(R.id.items_rv);
        rvEmployees = findViewById(R.id.home_owner_employees_rv);
        rvRestocking = findViewById(R.id.recyclerViewRestocking);
        maTvStatusNotSync = findViewById(R.id.items_not_sync_tv);
        maTvStatusPending = findViewById(R.id.items_pending_tv);
        amBtnGeneratePunchInCode = findViewById(R.id.home_owner_punch_in_code_generate_btn);
        amTvCurrentPunchInCode = findViewById(R.id.home_employee_current_punch_in_code_tv);
        etPunchInCode = findViewById(R.id.home_employee_punch_in_code_et);
        btnEnterPunchInCode = findViewById(R.id.home_employee_punch_in_code_enter_btn);
        employeeStatus = findViewById(R.id.main_employee_status);
        profileFnLUserType = findViewById(R.id.home_employee_user_type_tv);

        /*employee session hooks*/
        llEmployeeLayoutYesSync = findViewById(R.id.main_employee_yes_sync_ll);
        llEmployeeLayoutNoSync = findViewById(R.id.home_employee_no_sync_ll);
        llEmployeeLayoutPendingSync = findViewById(R.id.home_employee_pending_sync_ll);
        etBusinessCode = findViewById(R.id.home_employee_no_sync_business_code_et);
        btnEnterBusinessCode = findViewById(R.id.home_employee_no_sync_business_code_btn);

        /*items layout*/
        items_layout = findViewById(R.id.layout_items_rl);
        itemSearchBar = findViewById(R.id.items_search_bar_serv);
        cart_button = findViewById(R.id.items_cart_iv);
        add_button = findViewById(R.id.items_add_button_fab);
        items_counter = findViewById(R.id.items_counter);
        //maSvItems = findViewById(R.id.items_sv);
        /*Category*/
        category_plus_btn = findViewById(R.id.items_category_plus_btn);
        rvCategory = findViewById(R.id.items_category_rv);

        /*restock layout*/
        restock_layout = findViewById(R.id.layout_restock_rl);
        restockingSearchBar = findViewById(R.id.restockingSearchBar);
        restock_button = findViewById(R.id.fabUpdate);

        /*History Layout*/
        history_layout = findViewById(R.id.layout_history_rl);
        rvHistory = findViewById(R.id.history_out_rv);
        history_filter_button = findViewById(R.id.history_filter_btn);
        history_counter = findViewById(R.id.history_counter);
        history_current_total = findViewById(R.id.history_current_total);

        /*insights layout*/
        insights_layout = findViewById(R.id.layout_insight_rl);
        insight_item_list = new ArrayList<>();
        insight_context_list = new ArrayList<>(Arrays.asList("Choose Context", "Today", "Yesterday", "This Week", "This Month"));
        insight_item_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, insight_item_list);
        insight_context_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, insight_context_list);
        item_total_sales_vol_tv = findViewById(R.id.insight_item_total_sales_vol_tv);
        item_revenue_tv = findViewById(R.id.insight_item_revenue_tv);
        pieChart = findViewById(R.id.insight_piechart);
        initializePieChart();

        //item_turnover_rate_tv = findViewById(R.id.item_turnover_rate_tv);
        top1_tv = findViewById(R.id.insight_top1_tv);
        top2_tv = findViewById(R.id.insight_top2_tv);
        top3_tv = findViewById(R.id.insight_top3_tv);

        eye_open_button = findViewById(R.id.home_owner_eye_open_icon_iv);
        eye_close_button = findViewById(R.id.home_owner_eye_close_icon_iv);
        boxBusinessCode = findViewById(R.id.home_owner_business_code_box_ll);

        //haptic feedback
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /*array lists*//*
        currProduct = new ArrayList<>();
        cartedProduct = new ArrayList<>();
        cartedItem = new ArrayList<>();
        listItemID = new ArrayList<>();
        listItemName = new ArrayList<>();
        listItemPrice = new ArrayList<>();
        listItemQty = new ArrayList<>();*/

        /*Loading system*/
        systemLoading = new SystemLoading(MainActivity.this);
        TIMEOUT_DURATION = 10000;
        time_out_handler = new Handler();
        time_out_runnable = new Runnable() {
            @Override
            public void run() {
                //Runs 10 seconds after loading
                //startNoConnectionActivity();
                //Toast.makeText(getApplicationContext(), "Not online", Toast.LENGTH_SHORT).show();
            }
        };
        time_out_handler.postDelayed(time_out_runnable, TIMEOUT_DURATION);
        //Initiate login
        systemLoading.startLoadingDialog();

        /*Insight*/
        insight_item_spinner = findViewById(R.id.insight_item_perf_spinner);
        insight_context_spinner = findViewById(R.id.insight_context_perf_spinner);

        //Check if session exists
        checkSession();

        //Get current user information
        getCurrentUserInformation();

        /*tool bar*/
        setSupportActionBar(toolbar);

        /*navigation drawer menu*/
        setNavigationDrawerMenu();


        //buttons
        eye_close_button.setOnClickListener(view -> {
            vibrate(VIB_DURATION);
            eyeClose();
        });

        eye_open_button.setOnClickListener(view -> {
            vibrate(VIB_DURATION);
            eyeOpen();
        });

        homeIcon.setOnClickListener(view -> {

        });

        cart_button.setOnClickListener(view -> {
            openCart();
        });

        add_button.setOnClickListener(view -> {
            if (at_max_items) {
                itemMaxOpenPopup();
            } else {
                itemAddOpenPopup();
            }
        });

        restock_button.setOnClickListener(view -> {
            restockItems();
        });

        history_filter_button.setOnClickListener(view ->{
            openDatePickerOption();
        });

        category_plus_btn.setOnClickListener(view -> {
            categoryAddOpenPopup();
        });

        toolbar.setNavigationOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
            GeneralHelper.hideKeyboard(view);
        });
    }

    private void vibrate(int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }

    boolean is_transaction_filter_set = false;
    boolean is_start_set = false;
    boolean is_end_set = false;
    Calendar start_date;
    Calendar end_date;
    long[] selected_date_range = new long[2];


    private void openDatePickerOption() {
        LinearLayout history_start_end_option = findViewById(R.id.history_start_end_option);
        TextView history_selected_date = findViewById(R.id.history_selected_date);
        
        if (!is_transaction_filter_set) {
            //Open Options
            is_transaction_filter_set = true;
            history_start_end_option.setVisibility(View.VISIBLE);

            start_date = Calendar.getInstance();
            end_date = Calendar.getInstance();

            TextView history_selected_start = findViewById(R.id.history_selected_start);
            TextView history_selected_end = findViewById(R.id.history_selected_end);

            history_filter_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_filter_clear));

            history_selected_start.setOnClickListener(v -> {
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();

                    calendar.set(year, month, dayOfMonth, 0, 0, 0);
                    selected_date_range[0] = calendar.getTimeInMillis();

                    history_selected_start.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                    start_date.set(year, month, dayOfMonth);

                    is_start_set = true;

                    history_selected_date.setText("Custom");

                    transactionDateFilter();
                }, start_date.get(Calendar.YEAR), start_date.get(Calendar.MONTH), start_date.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            });

            history_selected_end.setOnClickListener(v -> {
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();

                    calendar.set(year, month, dayOfMonth, 23, 59, 59);
                    selected_date_range[1] = calendar.getTimeInMillis();

                    history_selected_end.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                    end_date.set(year, month, dayOfMonth);

                    is_end_set = true;

                    history_selected_date.setText("Custom");

                    transactionDateFilter();
                }, end_date.get(Calendar.YEAR), end_date.get(Calendar.MONTH), end_date.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            });
        } else {
            //Close Options and reset
            is_transaction_filter_set = false;
            history_start_end_option.setVisibility(View.GONE);
            is_start_set = false;
            is_end_set = false;
            history_selected_date.setText("All Transactions");
            history_filter_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_filter));
            setUpHistoryList();
        }
    }

    private void restockItems() {
        String FUNCTION_TAG = "restockItems";

        String key;

        MyTransaction transaction;

        double subtotal;
        int item_count;
        Object transaction_date;
        List<Item> items;

        DatabaseReference ref = history_ref.push();
        key = ref.getKey();

        subtotal = 0.0;
        item_count = 0;
        transaction_date = ServerValue.TIMESTAMP;
        items = new ArrayList<>();

        int restocks_count = 0;
        boolean item_without_cost_price = false;

        /*Get data for each item in adapter*/
        for (int i = 0; i < listAdapterRestockFirebase.getItemCount(); i++) {

            Item item = listAdapterRestockFirebase.getItem(i);

            /*Check if current item is being restocked*/
            if (item.getRestock_quantity() == 0) {
                continue;
            }

            restocks_count++;

            items.add(new Item(item.getName(), item.getCost_price(), item.getRestock_quantity()));

            subtotal += item.getCost_price() * item.getRestock_quantity();
            item_count += item.getRestock_quantity();

            /*Check if item has cost price*/
            if (item.getCost_price() == 0) {
                item_without_cost_price = true;
            }
        }

        if (restocks_count == 0) {
            Toast.makeText(getApplicationContext(), "Please select items to restock", Toast.LENGTH_SHORT).show();
            return;
        }

        transaction = new MyTransaction(0.0, 0.0, subtotal, item_count, false, transaction_date, items, cUser.getFirst_name() + " " + cUser.getLast_name());

        if (item_without_cost_price) {
            restockApprovalOpenPopup(transaction, key, ref);
        } else if (restocks_count > 0) {
            ref.setValue(transaction);
            openReceipt(key);
            processRestock(transaction.getItems());
        }
    }

    private void restockApprovalOpenPopup(MyTransaction transaction, String key, DatabaseReference ref) {
        String FUNCTION_TAG = "restockApprovalOpenPopup";

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_restock_approval, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        Button yes, no;

        yes = popupView.findViewById(R.id.rap_btn_yes);
        no = popupView.findViewById(R.id.rap_btn_no);

        yes.setOnClickListener(view -> {
            Log.e(FUNCTION_TAG, "yes");
            ref.setValue(transaction);
            openReceipt(key);
            processRestock(transaction.getItems());
            popupWindow.dismiss();
        });

        no.setOnClickListener(view -> {
            Log.e(FUNCTION_TAG, "no");
            popupWindow.dismiss();
        });
    }

    private void processRestock(@NonNull List<Item> items) {
        String FUNCTION_TAG = "processRestock";

        for (Item item : items) {
            Log.e(FUNCTION_TAG, "Current Item Restock Quantity: " + item.getQuantity());
            items_ref.child(item.getName()).runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    if (currentData.getValue(Item.class) == null) {
                        Log.e(FUNCTION_TAG, "Error");
                        return Transaction.success(currentData);
                    }
                    Item current_value = currentData.getValue(Item.class);
                    if (current_value.getQuantity() == 0) {
                        current_value.setQuantity(item.getQuantity());
                    } else {
                        current_value.setQuantity(current_value.getQuantity() + item.getQuantity());
                    }
                    current_value.setRestock_quantity(0);
                    currentData.setValue(current_value);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        Log.e(FUNCTION_TAG, "Transaction failed: " + error.getMessage());
                    } else if (committed) {
                        Log.d(FUNCTION_TAG, "Transaction succeeded. New value: " + currentData.getValue());
                    }
                }
            });
        }
    }

    private void initializePieChart() {
        pieChart.setNoDataTextColor(getResources().getColor(R.color.colorTextPrimary));
        pieChart.setNoDataText("Pending context");
        pieChart.setCenterTextSize(20);
        pieChart.setEntryLabelTextSize(15f);
        pieChart.setHoleColor(getResources().getColor(R.color.colorBackground));
        pieChart.setCenterTextColor(getResources().getColor(R.color.colorTextPrimary));
        pieChart.getLegend().setTextColor(getResources().getColor(R.color.colorTextPrimary));
        pieChart.getLegend().setTextSize(15f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.getLegend().setEnabled(false);
    }

    private void initializeAds() {
        MobileAds.initialize(this, initializationStatus -> {

        });

        mAdView = findViewById(R.id.adViewItems);
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void deinitializeAds() {
        mAdView = findViewById(R.id.adViewItems);
        mAdView.setVisibility(View.GONE);
    }

    private void openCart() {
        Intent intent = new Intent(MainActivity.this, CartActivity.class);
        startActivity(intent);
    }

    private void eyeOpen() {
        eye_open_button.setVisibility(View.GONE);
        eye_close_button.setVisibility(View.VISIBLE);
        boxBusinessCode.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
        tv_business_code.setVisibility(View.INVISIBLE);
    }

    private void eyeClose() {
        eye_close_button.setVisibility(View.GONE);
        eye_open_button.setVisibility(View.VISIBLE);
        boxBusinessCode.setBackgroundColor(Color.TRANSPARENT);
        tv_business_code.setVisibility(View.VISIBLE);
    }

    private void setNavigationDrawerMenu() {
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void getCurrentUserInformation() {
        String FUNCTION_TAG = "getCurrentUserInformation";

        /*ATTENTION you might have to change this and put this further up the run order (This may be
        why the system breaks when the user is deleted)*/
        if (firebaseDatabaseHelper.getFirebaseUser() == null) {
            logout();
        }

        user_ref = firebaseDatabaseHelper.getCurrentUserRef();

        user_ref.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(FUNCTION_TAG, "Error getting data", task.getException());
            } else {
                Log.i(FUNCTION_TAG, "Got User object: " + (task.getResult().getValue()));

                /*Disable Loading Timer*/
                time_out_handler.removeCallbacks(time_out_runnable);

                /*Get Current User*/
                cUser = task.getResult().getValue(User.class);
                
                setUserData();

                //Check usertype
                //[Employee, Business Owner, Employee - Inventory Manager]
                switch (cUser.getUser_type()) {
                    case 0:
                    case 2:
                        //Employee / Stock Employee
                        processEmployeeUser();
                        break;
                    case 1:
                        //Business Owner
                        processBusinessOwnerUser();
                        break;
                }
                //Insight
                setUpInsightAdapters();
            }

            //subscription tracking system
            setUpSubscriptionTracker();

            //Sidebar continuity
            sidebarContinuity();

            //Lists
            setUpLists();

            //Search item search bar system
            setUpItemSearchBar();

            //Search restock search bar system
            setUpRestockSearchBar();

            //Dismiss loading popup
            systemLoading.dismissDialog();

            validateCheckoutLink();
        });
    }

    private void setUpLists() {
        //List of Items
        setUpItemList();

        //List of Categories
        setUpCategoryList();

        //List of Employees
        setUpEmployeeList();

        //List of Restock
        setUpRestockList();

        //List of History
        setUpHistoryList();
    }

    private void setUpSubscriptionTracker() {
        String FUNCTION_TAG = "setUpSubscriptionTracking";

        //Check recent payment
        subscription_ref.child("current_checkout_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    return;
                }

                if (snapshot.getValue(String.class) == null) {
                    return;
                }

                /*Id of current checkout*/
                String id = snapshot.getValue(String.class);

                /*Create API request for recent payment*/
                Request request = Subscription.createCheckOutGetRequest(id);

                /*API call to check if recent payments were made*/
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(FUNCTION_TAG, e.toString());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = "Empty";

                        if (response.body() != null) {
                            result = Subscription.parseCheckOutStatus(response.body().string());
                        }

                        /*Payment successful*/
                        if (result.equals("succeeded")) {
                            subscription_ref.child("type").setValue(1);
                            subscription_ref.child("expiration_date").setValue(Subscription.getUnixOneMonthExpiry());
                            subscription_ref.child("current_checkout_id").removeValue();
                            subscription_ref.child("checkout_expiration").removeValue();
                            return;
                        }

                        /*If !successful: check if link is expired*/
                        subscription_ref.child("checkout_expiration").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (!snapshot.exists()) {
                                    return;
                                }

                                if (snapshot.getValue(Long.class) == null) {
                                    return;
                                }

                                long expiration_timestamp = snapshot.getValue(Long.class);

                                long current_timestamp = System.currentTimeMillis() / 1000;

                                if (current_timestamp >= expiration_timestamp) {
                                    /*Expired*/

                                    /*API request to expire the current checkout link*/
                                    Request request2 = Subscription.createCheckOutExpireRequest(id);
                                    client.newCall(request2).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.e(FUNCTION_TAG, e.toString());
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            if (response.body() != null) {
                                                Log.i(FUNCTION_TAG, response.body().string());
                                            }
                                            subscription_ref.child("current_checkout_id").removeValue();
                                            subscription_ref.child("checkout_expiration").removeValue();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Get Subscription Type
        subscription_ref.child("type").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    subscription_type = snapshot.getValue(Integer.class);
                    Log.d(FUNCTION_TAG, "subscription_type: " + subscription_type);

                    /*Check subscription type*/
                    switch (subscription_type) {
                        case Subscription.FREE:
                        case Subscription.DEBUG_FREE:
                            lock_marked = true;
                            initializeAds();
                            break;
                        case Subscription.PREMIUM1:
                        case Subscription.PREMIUM2:
                        case Subscription.DEBUG_PREMIUM:
                            premium_icon.setVisibility(View.VISIBLE);
                            lock_marked = false;
                            deinitializeAds();
                            setUpExpiryTracker();
                            nav_services.setVisible(false);
                            break;
                        default:
                            Log.wtf(FUNCTION_TAG, "Subscription type out of bounds");
                            break;
                    }

                    setUpLists();
                    setUpTrackers();
                } else {
                    if (!(cUser.getBusiness_code().equals("null"))) {
                        subscription_ref.child("type").setValue(0).addOnCompleteListener(task ->
                        {
                            Log.d(FUNCTION_TAG, "Added new data:\n" +
                                    "subscription/type/0");
                            subscription_type = 0;
                            Log.d(FUNCTION_TAG, "" + subscription_type);

                            lock_marked = true;

                            setUpLists();
                            setUpTrackers();
                        });
                    }
                }
            }

            private void setUpExpiryTracker() {
                subscription_ref.child("expiration_date").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long expiration_timestamp = snapshot.getValue(Long.class);

                            if (expiration_timestamp != null) {
                                long current_timestamp = System.currentTimeMillis() / 1000;

                                if (current_timestamp >= expiration_timestamp) {
                                    /*Expired*/
                                    subscription_ref.child("type").setValue(0);
                                    subscription_ref.child("expiration_date").removeValue();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            private void setUpTrackers() {
                setUpItemTracker();
                setUpTransactionTracker();
                setUpCategoryTracker();
            }

            private void setUpItemTracker() {
                String NESTED_FUNCTION_TAG = FUNCTION_TAG + " -> setUpItemTracker";

                items_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.e(NESTED_FUNCTION_TAG, "Snapshot doesn't exist");
                            return;
                        }

                        item_count_general = snapshot.getChildrenCount();
                        item_count_marked = 0;
                        item_count_unmarked = 0;

                        for (DataSnapshot curr_snap : snapshot.getChildren()) {
                            if (curr_snap.getValue(Item.class).isSub_marked()) {
                                item_count_marked++;
                            } else {
                                item_count_unmarked++;
                            }
                        }

                        String count = String.valueOf(item_count_general);
                        String limit = "/0";

                        //Counter visual
                        switch (subscription_type) {
                            case Subscription.DEBUG_FREE:
                                limit = "/" + Subscription.getLimit(Subscription.DEBUG_FREE, Subscription.ITEM);
                                if (item_count_general >= Subscription.getLimit(Subscription.DEBUG_FREE, Subscription.ITEM)) {
                                    at_max_items = true;
                                } else {
                                    at_max_items = false;
                                }
                                break;
                            case Subscription.FREE:
                                limit = "/" + Subscription.getLimit(Subscription.FREE, Subscription.ITEM);
                                if (item_count_general >= Subscription.getLimit(Subscription.FREE, Subscription.ITEM)) {
                                    at_max_items = true;
                                } else {
                                    at_max_items = false;
                                }
                                break;
                            case Subscription.PREMIUM1:
                                limit = "/" + Subscription.getLimit(Subscription.PREMIUM1, Subscription.ITEM);
                                if (item_count_general >= Subscription.getLimit(Subscription.PREMIUM1, Subscription.ITEM)) {
                                    at_max_items = true;
                                } else {
                                    at_max_items = false;
                                }
                                break;
                            case Subscription.PREMIUM2:
                                limit = "/" + Subscription.getLimit(Subscription.PREMIUM2, Subscription.ITEM);
                                if (item_count_general >= Subscription.getLimit(Subscription.PREMIUM2, Subscription.ITEM)) {
                                    at_max_items = true;
                                } else {
                                    at_max_items = false;
                                }
                                break;
                            case Subscription.DEBUG_PREMIUM:
                                limit = "/" + Subscription.getLimit(Subscription.DEBUG_PREMIUM, Subscription.ITEM);
                                at_max_items = item_count_general >= Subscription.getLimit(Subscription.DEBUG_PREMIUM, Subscription.ITEM);
                                break;
                        }

                        String total = count + limit;

                        items_counter.setText(total);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            private void setUpTransactionTracker() {
                String NESTED_FUNCTION_TAG = FUNCTION_TAG + " -> setUpTransactionTracker";

                history_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Log.e(NESTED_FUNCTION_TAG, "Snapshot doesn't exist");
                            return;
                        }

                        long node_count = snapshot.getChildrenCount();

                        String count = String.valueOf(node_count);
                        String limit = "/0";

                        switch (subscription_type) {
                            case Subscription.DEBUG_FREE:
                                limit = "/" + Subscription.getLimit(Subscription.DEBUG_FREE, Subscription.TRANSACTION);
                                if (item_count_general >= Subscription.getLimit(Subscription.DEBUG_FREE, Subscription.TRANSACTION)) {
                                    at_max_transactions = true;
                                } else {
                                    at_max_transactions = false;
                                }
                                break;
                            case Subscription.FREE:
                                limit = "/" + Subscription.getLimit(Subscription.FREE, Subscription.TRANSACTION);
                                if (item_count_general >= Subscription.getLimit(Subscription.FREE, Subscription.TRANSACTION)) {
                                    at_max_transactions = true;
                                } else {
                                    at_max_transactions = false;
                                }
                                break;
                            case Subscription.PREMIUM1:
                                limit = "/" + Subscription.getLimit(Subscription.PREMIUM1, Subscription.TRANSACTION);
                                if (item_count_general >= Subscription.getLimit(Subscription.PREMIUM1, Subscription.TRANSACTION)) {
                                    at_max_transactions = true;
                                } else {
                                    at_max_transactions = false;
                                }
                                break;
                            case Subscription.PREMIUM2:
                                limit = "/" + Subscription.getLimit(Subscription.PREMIUM2, Subscription.TRANSACTION);
                                if (item_count_general >= Subscription.getLimit(Subscription.PREMIUM2, Subscription.TRANSACTION)) {
                                    at_max_transactions = true;
                                } else {
                                    at_max_transactions = false;
                                }
                                break;
                            case Subscription.DEBUG_PREMIUM:
                                limit = "/" + Subscription.getLimit(Subscription.DEBUG_PREMIUM, Subscription.TRANSACTION);
                                at_max_transactions = item_count_general >= Subscription.getLimit(Subscription.DEBUG_PREMIUM, Subscription.TRANSACTION);
                                break;
                        }

                        String total = count + limit;

                        history_counter.setText(total);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            private void setUpCategoryTracker() {
                String NESTED_FUNCTION_TAG = FUNCTION_TAG + " -> setUpCategoryTracker";

                category_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Log.e(NESTED_FUNCTION_TAG, "Snapshot doesn't exist");
                            return;
                        }

                        long node_count = snapshot.getChildrenCount();

                        if (node_count >= Subscription.getLimit(Subscription.FREE, Subscription.CATEGORY)) {
                            Log.d(NESTED_FUNCTION_TAG, "Items Count: " + node_count + "\nOutside max free limit");
                            is_at_max_free_categories = true;
                        }

                        if (node_count >= Subscription.getLimit(Subscription.PREMIUM1, Subscription.CATEGORY)) {
                            Log.d(NESTED_FUNCTION_TAG, "Items count: " + node_count + "\nOutside max premium1 limit");
                            is_at_max_premium1_categories = true;
                        }

                        if (node_count >= Subscription.getLimit(Subscription.PREMIUM2, Subscription.CATEGORY)) {
                            Log.d(NESTED_FUNCTION_TAG, "Items count: " + node_count + "\nOutside max premium2 limit");
                            is_at_max_premium2_categories = true;
                        }

                        if (node_count >= Subscription.getLimit(Subscription.DEBUG_FREE, Subscription.CATEGORY)) {
                            Log.d(NESTED_FUNCTION_TAG, "Items count: " + node_count + "\nOutside max debug limit");
                            is_at_max_debug_free_categories = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void setUpRestockSearchBar() {
        restockingSearchBar.clearFocus();
        restockingSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtRestockSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                txtRestockSearch(newText);
                return false;
            }
        });
    }

    private void setUpItemSearchBar() {
        itemSearchBar.clearFocus();
        itemSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtItemSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                txtItemSearch(query);
                //filterList(newText);
                return true;
            }
        });
    }

    private void setUpItemList() {
        rvItems.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        item_query = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code());
        FirebaseRecyclerOptions<Item> options1 =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(item_query, Item.class)
                        .build();
        listAdapterItemFirebase = new ListAdapterItemFirebase(options1, this, lock_marked);
        rvItems.setAdapter(listAdapterItemFirebase);
        listAdapterItemFirebase.startListening();
    }

    private void setUpRestockList() {
        rvRestocking.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        restock_query = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code());
        FirebaseRecyclerOptions<Item> options2 =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(restock_query, Item.class)
                        .build();
        listAdapterRestockFirebase = new ListAdapterRestockFirebase(options2, this, cUser);
        rvRestocking.setAdapter(listAdapterRestockFirebase);
        listAdapterRestockFirebase.startListening();
    }

    private void setUpEmployeeList() {
        rvEmployees.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        employee_query = firebaseDatabaseHelper.getEmployeesQuery(cUser.getBusiness_code());
        FirebaseRecyclerOptions<User> options3 =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(employee_query, User.class)
                        .build();
        listAdapterEmployeeFirebase = new ListAdapterEmployeeFirebase(options3, this);
        rvEmployees.setAdapter(listAdapterEmployeeFirebase);
        listAdapterEmployeeFirebase.startListening();
    }

    ValueEventListener history_listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()) {
                return;
            }

            history_current_total.setText(String.valueOf(snapshot.getChildrenCount()));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void setUpHistoryList() {
        rvHistory.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        history_query = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code());
        FirebaseRecyclerOptions<MyTransaction> options4 =
                new FirebaseRecyclerOptions.Builder<MyTransaction>()
                        .setQuery(history_query, MyTransaction.class)
                        .build();
        listAdapterHistoryFirebase = new ListAdapterHistoryFirebase(options4, this, cUser);
        rvHistory.setAdapter(listAdapterHistoryFirebase);
        listAdapterHistoryFirebase.startListening();

        if (history_filtered_query != null) {
            history_filtered_query.removeEventListener(history_listener);
        }
        history_query.addValueEventListener(history_listener);
    }

    ItemTouchHelper.Callback call_back = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int drag_flag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            return makeMovementFlags(drag_flag, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    private void setUpCategoryList() {
        rvCategory.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
        category_query = business_code_ref.child("categories");
        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(category_query, Category.class)
                        .build();
        listAdapterCategoryFirebase = new ListAdapterCategoryFirebase(options, this);
        rvCategory.setAdapter(listAdapterCategoryFirebase);
        rvCategory.setItemAnimator(null);
        /*ItemTouchHelper itemTouchHelper = new ItemTouchHelper(call_back);
        itemTouchHelper.attachToRecyclerView(rvCategory);*/
        listAdapterCategoryFirebase.startListening();
    }

    private void sidebarContinuity() {
        switch (sessionManager.getMainStatus()) {
            case 0:
                navigationView.setCheckedItem(R.id.nav_home);
                home_layout.setVisibility(View.VISIBLE);
                items_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                history_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                break;
            case 1:
                navigationView.setCheckedItem(R.id.nav_items);
                home_layout.setVisibility(View.GONE);
                items_layout.setVisibility(View.VISIBLE);
                insights_layout.setVisibility(View.GONE);
                history_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                break;
            case 2:
                navigationView.setCheckedItem(R.id.nav_insights);
                home_layout.setVisibility(View.GONE);
                items_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.VISIBLE);
                history_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                break;
            case 3:
                navigationView.setCheckedItem(R.id.nav_history);
                home_layout.setVisibility(View.GONE);
                items_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                history_layout.setVisibility(View.VISIBLE);
                insights_layout.setVisibility(View.GONE);
            case 4:
                navigationView.setCheckedItem(R.id.nav_insights);
                home_layout.setVisibility(View.GONE);
                items_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                history_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.VISIBLE);
        }
    }

    private void setUpInsightAdapters() {
        insight_item_spinner.setAdapter(insight_item_adapter);
        insight_context_spinner.setAdapter(insight_context_adapter);

        insight_item_spinner.setOnItemSelectedListener(this);
        insight_context_spinner.setOnItemSelectedListener(this);

        items_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                insight_item_list.clear();
                insight_item_list.add("Choose Item");
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    //Spinner list - data source
                    Item item =  postSnapshot.getValue(Item.class);
                    String item_name = item.getName();
                    insight_item_list.add(item_name);
                }
                insight_item_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void processBusinessOwnerUser() {
        //Set home layout to business owner version
        home_layout = findViewById(R.id.layout_home_business_owner_rl);
        //Set business code text
        tv_business_code.setText(cUser.getBusiness_code());
        //Set username text view
        profileFnLNameBusinessOwner.setText(sessionManager.getUsername());

        firebaseDatabaseHelper.getBusinessCodeRef(cUser.getBusiness_code()).child("punch in code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String punch_in_code = snapshot.getValue(String.class);
                amTvCurrentPunchInCode.setText(punch_in_code);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        amBtnGeneratePunchInCode.setOnClickListener(view -> {
            vibrate(VIB_DURATION);
            business_code_ref.child("punch in code").setValue(randomHelper.generateRandom5NumberCharString());
        });
    }

    private void processEmployeeUser() {
        //Set home layout to employee version
        home_layout = findViewById(R.id.layout_home_employee_rl);
        //Set username text view
        profileFnLNameEmployee.setText(sessionManager.getUsername());

        /*Access removal*/
        /*Insight*/
        nav_insights.setVisible(false);
        /*Add buttons*/
        category_plus_btn.setVisibility(View.GONE);
        add_button.setVisibility(View.GONE);

        nav_services.setVisible(false);

        //Check employee subtype
        if (cUser.getUser_type() != 2) {
            nav_restock.setVisible(false);
        }
        //User is sync to business?
        if (cUser.getBusiness_code().equals("null")) {
            //Not synced
            maTvStatusNotSync.setVisibility(View.VISIBLE);
            llEmployeeLayoutNoSync.setVisibility(View.VISIBLE);

            btnEnterBusinessCode.setOnClickListener(view -> {
                String code = etBusinessCode.getText().toString();
                business_ref.child(code).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DataSnapshot snapshot = task1.getResult();
                        if (snapshot.exists()) {
                            user_ref.child("business_code").setValue(code);
                            Log.e("recChecka", "1");
                            recreate();
                        } else {
                            Toast.makeText(getApplicationContext(), "Business doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        } else {
            //Synced
            //Check approval
            if (cUser.getStatus() == 0) {
                //Pending approval
                llEmployeeLayoutPendingSync.setVisibility(View.VISIBLE);
                //maSvItems.setVisibility(View.GONE);
                rvItems.setVisibility(View.GONE);
                maTvStatusPending.setVisibility(View.VISIBLE);
                user_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        lUser = snapshot.getValue(User.class);
                        if (lUser.getStatus() != 0) {
                            Log.e("recChecka", "2");
                            recreate();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                user_ref.child("business_code").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bus_code = snapshot.getValue(String.class);
                        if (bus_code.equals("null")) {
                            Log.e("recChecka", "3");
                            recreate();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                //Approved
                llEmployeeLayoutYesSync.setVisibility(View.VISIBLE);

                user_ref.child("status").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int curr_status = snapshot.getValue(Integer.class);
                        switch (curr_status) {
                            case 0:
                                //User has been dismissed
                                employeeStatus.setText("ERROR");
                                employeeStatus.setBackgroundColor(Color.YELLOW);
                                Log.e("recChecka", "4");
                                recreate();
                                break;
                            case 1:
                                //Inactive

                                //Home view
                                employeeStatus.setText("Inactive");
                                employeeStatus.setBackgroundColor(Color.GRAY);
                                findViewById(R.id.home_employee_punch_in).setVisibility(View.VISIBLE);
                                etPunchInCode.setVisibility(View.VISIBLE);
                                btnEnterPunchInCode.setVisibility(View.VISIBLE);

                                //Items View
                                findViewById(R.id.items_not_punched_in_tv).setVisibility(View.VISIBLE);
                                rvItems.setVisibility(View.GONE);
                                rvCategory.setVisibility(View.GONE);

                                //Restocking View
                                findViewById(R.id.maTvNotPunchedInRestocking).setVisibility(View.VISIBLE);
                                rvRestocking.setVisibility(View.GONE);

                                add_button.setVisibility(View.GONE);
                                break;
                            case 2:
                                //Active

                                //Home view
                                employeeStatus.setText("Active");
                                employeeStatus.setBackgroundColor(Color.GREEN);
                                findViewById(R.id.home_employee_punch_in).setVisibility(View.GONE);
                                etPunchInCode.setVisibility(View.GONE);
                                btnEnterPunchInCode.setVisibility(View.GONE);

                                //Items View
                                findViewById(R.id.items_not_punched_in_tv).setVisibility(View.GONE);
                                rvItems.setVisibility(View.VISIBLE);
                                rvCategory.setVisibility(View.VISIBLE);

                                //Restocking View
                                findViewById(R.id.maTvNotPunchedInRestocking).setVisibility(View.GONE);
                                rvRestocking.setVisibility(View.VISIBLE);

                                add_button.setVisibility(View.GONE);
                                break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                business_code_ref.child("punch in code").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String new_punch_in_code = snapshot.getValue(String.class);
                            user_ref.child("curr_punch_in_code").get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DataSnapshot snapshot1 = task1.getResult();
                                    if (snapshot1.exists()) {
                                        String current_punch_in_code = snapshot1.getValue(String.class);
                                        if (!(new_punch_in_code.equals(current_punch_in_code))) {
                                            user_ref.child("status").setValue(1);
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                user_ref.child("user_type").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int current_user_type = snapshot.getValue(Integer.class);
                        if (current_user_type == 0) {
                            profileFnLUserType.setText("Standard Employee");
                            add_button.setVisibility(View.GONE);
                        } else if (current_user_type == 2) {
                            profileFnLUserType.setText("Inventory Manager");
                            add_button.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                user_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cUser = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                btnEnterPunchInCode.setOnClickListener(view -> {
                    GeneralHelper.hideKeyboard(view);
                    punch_in_code = etPunchInCode.getText().toString();
                    business_code_ref.child("punch in code").get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DataSnapshot snapshot = task1.getResult();
                            String current_punch_in_code = snapshot.getValue(String.class);
                            if (punch_in_code.equals(current_punch_in_code)) {
                                user_ref.child("status").setValue(2);
                                user_ref.child("curr_punch_in_code").setValue(punch_in_code);
                            } else {
                                Toast.makeText(MainActivity.this, "Punch in code incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                });
            }
        }
    }

    private void setUserData() {
        cart_ref = firebaseDatabaseHelper.getCartRef(cUser.getUid());
        items_ref = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code());
        business_ref = firebaseDatabaseHelper.getBusinessRef();
        business_code_ref = firebaseDatabaseHelper.getBusinessCodeRef(cUser.getBusiness_code());
        history_ref = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code());
        category_ref = firebaseDatabaseHelper.getItemsCategoriesRef(cUser.getBusiness_code());
        subscription_ref = firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code());
    }

    private void checkSession() {
        if (!sessionManager.getLogin()) {
            sessionManager.setLogin(false);
            sessionManager.setUsername(null);
            startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            finish();
        }
        if (user == null) {
            sessionManager.setLogin(false);
            sessionManager.setUsername(null);
            startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            finish();
        }
    }

    int total_sales_vol;
    double total_rev;
    List<Item> top_list_item;
    String insight_selected_item, insight_selected_context;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        insight_selected_item = insight_item_spinner.getSelectedItem().toString();
        insight_selected_context = insight_context_spinner.getSelectedItem().toString();
        total_sales_vol = 0;
        total_rev = 0;
        top_list_item = new ArrayList<>();

        /*Check item selection is not default*/
        if (!(insight_selected_item.equals("Choose Item") || insight_selected_context.equals("Choose Context"))) {
            getInsightReference(insight_selected_context).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot current_snapshot : snapshot.getChildren()) {
                        MyTransaction current_transaction = current_snapshot.getValue(MyTransaction.class);

                        if (current_transaction == null || !current_transaction.isIs_out()) {
                            continue;
                        }

                        List<Item> items = current_transaction.getItems();
                        if (items != null) {
                            for (Item current_item : items) {
                                if (current_item.getName().equals(insight_selected_item)) {
                                    total_sales_vol += current_item.getQuantity();
                                    total_rev += current_item.getPrice() * current_item.getQuantity();
                                }
                            }
                        }
                    }
                    item_total_sales_vol_tv.setText(total_sales_vol + " Sold");
                    item_revenue_tv.setText("₱" + total_rev + " Worth Sold");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        if (insight_selected_item.equals("Choose Item")) {
            item_total_sales_vol_tv.setText("Pending Choice");
            item_revenue_tv.setText("Pending Choice");
        }

        /*If context has been selected*/
        if (!(insight_selected_context.equals("Choose Context"))) {
            getInsightReference(insight_selected_context).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot current_snapshot : snapshot.getChildren()) {
                        MyTransaction current_transaction = current_snapshot.getValue(MyTransaction.class);

                        if (current_transaction == null || !current_transaction.isIs_out()) {
                            continue;
                        }

                        List<Item> items = current_transaction.getItems();
                        if (items != null) {
                            top_list_item.addAll(items);
                        }
                    }

                    if (!top_list_item.isEmpty()) {
                        displayInsightPieChart(sortList(combineItems(top_list_item)), insight_selected_context);
                    } else {
                        pieChart.clear();
                        pieChart.setNoDataText("No Data for " + insight_selected_context);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            pieChart.clear();
        }
    }

    /*private void displayPerformance() {
        switch (insight_selected_context) {
            case "Today":
            case "Yesterday":
                displayPerformanceTodayOrYesterday();
                break;
            case "This Week":
                displayPerformanceWeek();
                break;
            case "This Month":
                displayPerformanceMonth();
                break;
        }
    }*/

    /*private void displayPerformanceMonth() {
        getInsightReference(insight_selected_context).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot month = task.getResult();
                for (DataSnapshot week : month.getChildren()) {
                    for (DataSnapshot day : week.getChildren()) {
                        for (DataSnapshot time : day.getChildren()) {
                            for (DataSnapshot cItem : time.getChildren()) {
                                Item item = cItem.getValue(Item.class);
                                if (item != null) {
                                    if (item.getName().equals(insight_selected_item)) {
                                        total_sales_vol += item.getQuantity();
                                        total_rev += item.getPrice() * item.getQuantity();
                                    }
                                } else {
                                    Log.e("Insight Error", "This Week - item is null");
                                }
                            }
                        }
                    }
                }
                item_total_sales_vol_tv.setText(total_sales_vol + " Sold");
                item_revenue_tv.setText("₱" + total_rev + " Worth Sold");
            }
        });
    }*/

    /*private void displayPerformanceWeek() {
        getInsightReference(insight_selected_context).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot week = task.getResult();
                for (DataSnapshot day : week.getChildren()) {
                    for (DataSnapshot time : day.getChildren()) {
                        for (DataSnapshot cItem : time.getChildren()) {
                            Item item = cItem.getValue(Item.class);
                            if (item != null) {
                                if (item.getName().equals(insight_selected_item)) {
                                    total_sales_vol += item.getQuantity();
                                    total_rev += item.getPrice() * item.getQuantity();
                                }
                            } else {
                                Log.e("Insight Error", "This Week - item is null");
                            }
                        }
                    }
                }
                item_total_sales_vol_tv.setText(total_sales_vol + " Sold");
                item_revenue_tv.setText("₱" + total_rev + " Worth Sold");
            }
        });
    }*/

    /*private void displayPerformanceTodayOrYesterday() {
        getInsightReference(insight_selected_context).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot current_snapshot : snapshot.getChildren()) {
                    MyTransaction current_transaction = current_snapshot.getValue(MyTransaction.class);
                    List<Item> items = current_transaction.getItems();
                    for (Item current_item : items) {
                        if (current_item.getName().equals(insight_selected_item)) {
                            total_sales_vol += current_item.getQuantity();
                            total_rev += current_item.getPrice() * current_item.getQuantity();
                        }
                    }
                }
                item_total_sales_vol_tv.setText(total_sales_vol + " Sold");
                item_revenue_tv.setText("₱" + total_rev + " Worth Sold");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    private List<Item> combineItems(List<Item> list) {

        List<Item> combined_items = new ArrayList<>();

        for (Item item : list) {
            boolean found = false;
            for (Item cItem : combined_items) {
                if (cItem.getName().equals(item.getName())) {
                    cItem.setQuantity(cItem.getQuantity() + item.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                combined_items.add(new Item(item.getName(), item.getPrice(), item.getQuantity()));
            }
        }
        return combined_items;
    }

    private void displayInsightPieChart(List<Item> topListItem, String current_context) {
        entries = new ArrayList<>();
        int curr_count = 1;

        for (Item item : topListItem) {
            if (curr_count > 5) {
                break;
            }

            float quantity = (float) item.getQuantity();
            String name = item.getName();

            entries.add(new PieEntry(quantity, name));

            curr_count++;
        }

        //getResources().getColor(R.color.colorTextPrimary)

        pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(15f);

        pieChart.setData(pieData);
        pieChart.setCenterText(current_context);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry e1 = (PieEntry) e;
                insight_item_spinner.setSelection(insight_item_adapter.getPosition(e1.getLabel()));
            }

            @Override
            public void onNothingSelected() {

            }
        });
        pieChart.invalidate();
    }

    private void SortAndDisplay(List<Item> top_list_item) {
        int count = 0;
        top_list_item.sort((o1, o2) -> Integer.compare(o2.getQuantity(), o1.getQuantity()));
        for (Item item : top_list_item) {
            switch (count) {
                case 0:
                    top1_tv.setText(item.getName() + " - " + item.getQuantity());
                    break;
                case 1:
                    top2_tv.setText(item.getName() + " - " + item.getQuantity());
                    break;
                case 2:
                    top3_tv.setText(item.getName() + " - " + item.getQuantity());
                    break;
            }
            count++;
        }
    }

    private List<Item> sortList(List<Item> list) {
        list.sort((o1, o2) -> Integer.compare(o2.getQuantity(), o1.getQuantity()));
        return list;
    }

    ArrayList<String> sortList(ArrayList<Item> arr) {

        ArrayList<String> list = new ArrayList<>();

        for (int i = 1; i < arr.size() ; i++) {
            Item key = arr.get(i);
            int j = i - 1;

            while (j >= 0 && arr.get(j).getQuantity() > key.getQuantity()) {
                arr.set(j + 1, arr.get(j));
                j = j - 1;
            }
            arr.set(j + 1, key);
        }
        for (int i = 0; i < 3; i++) {
            list.add(arr.get(i).getName() + " - " + arr.get(i).getQuantity());
        }
        return list;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public Query getInsightReference(String context) {

        long[] timestamps;

        switch (context) {
            case "Today":
                timestamps = getTimeStampsForToday();
                return history_ref.orderByChild("transaction_date").startAt(timestamps[0]).endAt(timestamps[1]);
            case "Yesterday":
                timestamps = getTimeStampsForYesterday();
                return history_ref.orderByChild("transaction_date").startAt(timestamps[0]).endAt(timestamps[1]);
            case "This Week":
                timestamps = getTimeStampsForWeek();
                return history_ref.orderByChild("transaction_date").startAt(timestamps[0]).endAt(timestamps[1]);
            case "This Month":
                timestamps = getTimeStampsForMonth();
                return history_ref.orderByChild("transaction_date").startAt(timestamps[0]).endAt(timestamps[1]);
            default:
                return null;
        }
    }

    private long[] getTimeStampsForWeek() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start_of_week = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long end_of_week = calendar.getTimeInMillis();

        return new long[] {start_of_week, end_of_week};
    }

    private long[] getTimeStampsForMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        calendar.set(year, month, 1, 0 ,0,0);
        long start_of_month = calendar.getTimeInMillis();

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        long end_of_month = calendar.getTimeInMillis();

        return new long[] {start_of_month, end_of_month};
    }

    private long[] getTimeStampsForToday() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(year, month, day, 0, 0, 0);
        long start_of_day = calendar.getTimeInMillis();

        calendar.set(year, month, day, 23, 59, 59);
        long end_of_day = calendar.getTimeInMillis();

        return new long[] {start_of_day, end_of_day};
    }

    private long[] getTimeStampsForYesterday() {
        Calendar calendar = Calendar.getInstance();
        Calendar temp = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        day -= 1;

        if (day == 0 && month != 0) {
            month -= 1;
            temp.set(Calendar.MONTH, month);
            day = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        } else if (day == 0 && month == 0) {
            month = 11;
            temp.set(Calendar.MONTH, month);
            day = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
            year -= 1;
        }

        calendar.set(year, month, day, 0, 0, 0);
        long start_of_day = calendar.getTimeInMillis();

        calendar.set(year, month, day, 23, 59, 59);
        long end_of_day = calendar.getTimeInMillis();

        return new long[] {start_of_day, end_of_day};
    }

    private void txtItemSearch(String str) {
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(items_ref.orderByChild("name").startAt(str).endAt(str + "~"), Item.class)
                        .build();

        listAdapterItemFirebase = new ListAdapterItemFirebase(options, this, lock_marked);
        listAdapterItemFirebase.startListening();
        rvItems.setAdapter(listAdapterItemFirebase);
    }

    private void txtRestockSearch(String str) {
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(items_ref.orderByChild("name").startAt(str).endAt(str + "~"), Item.class)
                        .build();

        listAdapterRestockFirebase = new ListAdapterRestockFirebase(options, this, cUser);
        listAdapterRestockFirebase.startListening();
        rvRestocking.setAdapter(listAdapterRestockFirebase);
    }

    private void filterByCategory(Category model) {
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(items_ref.orderByChild("category").equalTo(model.getName()), Item.class)
                        .build();
        listAdapterItemFirebase = new ListAdapterItemFirebase(options, this, lock_marked);
        listAdapterItemFirebase.startListening();
        rvItems.setAdapter(listAdapterItemFirebase);
    }

    private void transactionDateFilter() {
        if (is_start_set && is_end_set) {
            history_filtered_query = history_ref.orderByChild("transaction_date").startAt(selected_date_range[0]).endAt(selected_date_range[1]);
            FirebaseRecyclerOptions<MyTransaction> options =
                    new FirebaseRecyclerOptions.Builder<MyTransaction>()
                            .setQuery(history_filtered_query, MyTransaction.class)
                            .build();

            listAdapterHistoryFirebase = new ListAdapterHistoryFirebase(options, this, cUser);
            listAdapterHistoryFirebase.startListening();
            rvHistory.setAdapter(listAdapterHistoryFirebase);

            if (history_query != null) {
                history_query.removeEventListener(history_listener);
            }
            history_filtered_query.addValueEventListener(history_listener);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_home) {
            home_layout.setVisibility(View.VISIBLE);
            items_layout.setVisibility(View.GONE);
            restock_layout.setVisibility(View.GONE);
            history_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.GONE);
            sessionManager.setMainStatus(0);
            resetInsight();
        } else if(item.getItemId() == R.id.nav_items) {
            home_layout.setVisibility(View.GONE);
            items_layout.setVisibility(View.VISIBLE);
            restock_layout.setVisibility(View.GONE);
            history_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.GONE);
            sessionManager.setMainStatus(1);
            resetInsight();
        } else if (item.getItemId() == R.id.nav_restock) {
            home_layout.setVisibility(View.GONE);
            items_layout.setVisibility(View.GONE);
            restock_layout.setVisibility(View.VISIBLE);
            history_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.GONE);
            sessionManager.setMainStatus(2);
        } else if (item.getItemId() == R.id.nav_history) {
            home_layout.setVisibility(View.GONE);
            items_layout.setVisibility(View.GONE);
            restock_layout.setVisibility(View.GONE);
            history_layout.setVisibility(View.VISIBLE);
            insights_layout.setVisibility(View.GONE);
            sessionManager.setMainStatus(3);
        } else if (item.getItemId() == R.id.nav_insights) {
            items_layout.setVisibility(View.GONE);
            home_layout.setVisibility(View.GONE);
            restock_layout.setVisibility(View.GONE);
            history_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.VISIBLE);
            sessionManager.setMainStatus(4);
        } else if (item.getItemId() == R.id.nav_logout) {
            logout();
        } else if (item.getItemId() == R.id.nav_share) {

        } else if (item.getItemId() == R.id.nav_rate) {

        } else if(item.getItemId() == R.id.nav_exit) {
            finishAffinity();
        } else if (item.getItemId() == R.id.nav_premium) {
            premiumOpenPopup();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void premiumOpenPopup() {
        String FUNC_TAG = "premiumOpenPopup";

        PopupPremiumBinding binding = PopupPremiumBinding.inflate(LayoutInflater.from(this));

        PopupWindow popupWindow = new PopupWindow(binding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        binding.btnUpgrade.setOnClickListener(view -> {
            /*Check if there is existing link*/
            if (current_checkout_link != null) {
                /*Parse link*/
                Uri uri = Uri.parse(current_checkout_link);

                /*Redirect to link*/
                redirectToLink(uri);

                return;
            }

            Request request = Subscription.createCheckOutRequest(Subscription.PREMIUM1_PRICE);

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(FUNC_TAG, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = "Empty";

                    if (response.body() != null) {
                        result = response.body().string();
                    }

                    if (result.equals("Empty")) {
                        return;
                    }

                    Log.i(FUNC_TAG, result);
                    String id = Subscription.parseCheckOutID(result);
                    String url = Subscription.parseCheckOutUrl(result);
                    Uri uri = Uri.parse(url);
                    Log.i(FUNC_TAG, "LINK: " + url);

                    firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code())
                            .child("current_checkout_id")
                            .setValue(id);
                    firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code())
                            .child("checkout_expiration")
                            .setValue(Subscription.getUnixOneMinuteExpiry());

                    redirectToLink(uri);
                }
            });
        });
    }

    private void logout() {
        sessionManager.setLogin(false);
        sessionManager.setUsername(null);
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
        finish();
    }

    private void resetInsight() {
        insight_item_spinner.setSelection(0);
        insight_context_spinner.setSelection(0);
        pieChart.clear();
    }

    private void categoryAddOpenPopup() {
        String FUNC_TAG = "openAddCategoryPopup";

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_category_add, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        Button button_set;
        EditText field_category_name;

        button_set = popupView.findViewById(R.id.category_add_set_btn);
        field_category_name = popupView.findViewById(R.id.category_add_field);

        button_set.setOnClickListener(view -> {
            String category_name = field_category_name.getText().toString().trim();

            category_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int next_prio = 0;
                    if (snapshot.exists()) {

                        for (DataSnapshot curr_snap : snapshot.getChildren()) {
                            Category curr_cat = curr_snap.getValue(Category.class);

                            if (curr_cat != null) {
                                next_prio = curr_cat.getPriority() + 1;
                            }

                        }
                    }
                    category_ref.push().setValue(new Category(category_name, next_prio));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            popupWindow.dismiss();
        });
    }

    private void itemAddOpenPopup() {
        /*LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_item_add, null);*/

        PopupItemAddBinding binding = PopupItemAddBinding.inflate(LayoutInflater.from(this));

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow = new PopupWindow(binding.getRoot(), width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        List<String> categories;
        /*Button add_btn;
        ImageButton close_btn;
        EditText name_field, price_field, cost_price_field, markup_field;
        Spinner category_spinner;*/

        /*name_field = popupView.findViewById(R.id.popup_item_add_name);
        price_field = popupView.findViewById(R.id.popup_item_add_price);
        cost_price_field = popupView.findViewById(R.id.popup_item_add_cost_price);
        add_btn = popupView.findViewById(R.id.btnPopupAdd);
        close_btn = popupView.findViewById(R.id.btnPopupClose);
        category_spinner = popupView.findViewById(R.id.popup_item_category_spinner);
        markup_field = popupView.findViewById(R.id.popup_item_price_mark_up);*/
        categories = new ArrayList<>();
        categories.add("Select Category");

        /*Populate Category Choices*/
        category_ref.addValueEventListener(new ValueEventListener() {
            ArrayAdapter<String> adapter;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot curr_snap : snapshot.getChildren()) {
                        Category curr_cat = curr_snap.getValue(Category.class);
                        categories.add(curr_cat.getName());
                    }
                }
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, categories);
                binding.popupItemCategorySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*Close Button*/
        binding.btnPopupClose.setOnClickListener(view -> {
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            GeneralHelper.hideKeyboard(view);
        });

        /*Cost Field*/
        binding.popupItemAddCostPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    double price = Double.parseDouble(s.toString());
                    String markup_string = binding.popupItemPriceMarkUp.getText().toString().trim();
                    if (!markup_string.isEmpty()) {
                        double markup = Double.parseDouble(markup_string);
                        double total = price + (price * (markup / 100));
                        binding.popupItemAddPrice.setText(Double.toString(total));
                    }
                } else {
                    binding.popupItemAddPrice.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*Markup Field*/
        binding.popupItemPriceMarkUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    double markup = Double.parseDouble(s.toString());
                    String price_string = binding.popupItemAddCostPrice.getText().toString().trim();
                    if (!price_string.isEmpty()) {
                        double price = Double.parseDouble(price_string);
                        double total = price + (price * (markup / 100));
                        binding.popupItemAddPrice.setText(Double.toString(total));
                    }
                } else {
                    binding.popupItemAddPrice.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*Add Button*/
        binding.btnPopupAdd.setOnClickListener(view -> {
            //Store field data in temp variables
            String name, price, cost_price, category;
            name =  binding.popupItemAddName.getText().toString().trim();
            price = binding.popupItemAddPrice.getText().toString().trim();
            cost_price = binding.popupItemAddCostPrice.getText().toString().trim();
            category = binding.popupItemCategorySpinner.getSelectedItem().toString();

            boolean is_sub_marked = false;

            if (at_max_items) {
                Toast.makeText(MainActivity.this, "Maximum items for this plan has been reached", Toast.LENGTH_SHORT).show();
                return;
            }

            if (is_item_premium()) {
                is_sub_marked = true;
            }

            if (isEmpty(name)) {
                Toast.makeText(MainActivity.this, "Item should at least have a name", Toast.LENGTH_SHORT).show();
                return;
            }

            Item item = new Item();
            item.setName(name);

            if (!isEmpty(price)) {
                item.setPrice(Double.parseDouble(price));
            }

            if (!isEmpty(cost_price)) {
                item.setCost_price(Double.parseDouble(cost_price));
            }

            if (!category.equals("Select Category")) {
                item.setCategory(category);
            }

            if (is_sub_marked) {
                item.setSub_marked(true);
            }

            items_ref.child(name).setValue(item);

            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            GeneralHelper.hideKeyboard(view);
        });
    }

    private void itemMaxOpenPopup() {
        String FUNC_TAG = "itemMaxOpenPopup";

        PopupItemMaxBinding binding = PopupItemMaxBinding.inflate(LayoutInflater.from(this));

        PopupWindow popupWindow = new PopupWindow(binding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        binding.btnUpgrade.setOnClickListener(view -> {
            /*Check if there is existing link*/
            if (current_checkout_link != null) {
                /*Parse link*/
                Uri uri = Uri.parse(current_checkout_link);

                /*Redirect to link*/
                redirectToLink(uri);

                return;
            }

            Request request = Subscription.createCheckOutRequest(Subscription.PREMIUM1_PRICE);

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(FUNC_TAG, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = "Empty";

                    if (response.body() != null) {
                        result = response.body().string();
                    }

                    if (result.equals("Empty")) {
                        return;
                    }

                    Log.i(FUNC_TAG, result);
                    String id = Subscription.parseCheckOutID(result);
                    String url = Subscription.parseCheckOutUrl(result);
                    Uri uri = Uri.parse(url);
                    Log.i(FUNC_TAG, "LINK: " + url);

                    firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code())
                            .child("current_checkout_id")
                            .setValue(id);
                    firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code())
                            .child("checkout_expiration")
                            .setValue(Subscription.getUnixOneMinuteExpiry());

                    redirectToLink(uri);
                }
            });
        });
    }

    private void validateCheckoutLink() {
        /*Check if existing checkout link exists*/
        firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code())
                .child("current_checkout_id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    final String FUNC_TAG = "getExistingCheckoutLink";

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || snapshot.getValue(String.class) == null) {
                            return;
                        }

                        Request request = Subscription.createCheckOutGetRequest(snapshot.getValue(String.class));

                        OkHttpClient client = new OkHttpClient();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.e(FUNC_TAG, e.toString());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String result = "Empty";

                                if (response.body() != null) {
                                    result = response.body().string();
                                }

                                if (result.equals("Empty")) {
                                    return;
                                }

                                current_checkout_link = Subscription.parseCheckOutUrl(result);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void redirectToLink(Uri uri) {
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
        finish();
    }

    private boolean is_item_max() {
        switch (subscription_type) {
            case Subscription.FREE:
                return is_at_max_free_items;
            case Subscription.DEBUG_FREE:
                return is_at_max_debug_free_items;
            case Subscription.PREMIUM1:
                return is_at_max_premium1_items;
            case Subscription.PREMIUM2:
                return is_at_max_premium2_items;
            case Subscription.DEBUG_PREMIUM:
                return is_at_max_debug_premium_items;
            default:
                return false;
        }
    }

    private boolean is_item_premium() {
        switch (subscription_type) {
            case Subscription.PREMIUM1:
            case Subscription.PREMIUM2:
            case Subscription.DEBUG_PREMIUM:
                return true;
            default:
                return false;
        }
    }

    /*Popup when editing an item*/
    private void itemEditOpenPopup(Item item) {
        String FUNCTION_TAG = "itemEditOpenPopup";

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_item_edit, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        String current_item_name = item.getName();
        double current_item_cost = item.getCost_price();
        double current_item_price = item.getPrice();
        double current_item_cost_price = item.getCost_price();
        int current_item_quantity = item.getQuantity();
        String current_item_category;
        if (item.getCategory() == null) {
            current_item_category = null;
        } else {
            current_item_category = item.getCategory();
        }

        /*title*/
        TextView epp_title;
        epp_title = popupView.findViewById(R.id.epp_title);

        /*info*/
        LinearLayout epp_ll_info;
        TextView epp_tv_item_name, epp_tv_item_price, epp_tv_item_quantity, epp_tv_item_category;

        epp_ll_info = popupView.findViewById(R.id.epp_ll_info);
        epp_tv_item_name = popupView.findViewById(R.id.epp_tv_item_name);
        epp_tv_item_price = popupView.findViewById(R.id.epp_tv_item_price);
        epp_tv_item_quantity = popupView.findViewById(R.id.epp_tv_item_quantity);
        epp_tv_item_category = popupView.findViewById(R.id.epp_tv_item_category);

        /*edit*/
        LinearLayout epp_ll_edit;
        Button button_edit, button_delete;
        EditText edit_item_name, edit_item_price, edit_item_cost, edit_item_markup;
        TextView edit_display_item_quantity;
        Spinner category_spinner;
        List<String> categories;

        epp_ll_edit = popupView.findViewById(R.id.epp_ll_edit);
        edit_item_name = popupView.findViewById(R.id.productNameEdit);
        edit_item_cost = popupView.findViewById(R.id.productCostPriceEdit);
        edit_item_markup = popupView.findViewById(R.id.productMarkupEdit);
        edit_item_price = popupView.findViewById(R.id.productPriceEdit);
        edit_display_item_quantity = popupView.findViewById(R.id.productQuantityEdit);
        button_edit = popupView.findViewById(R.id.btnEditPopupEdit);

        button_delete = popupView.findViewById(R.id.btnEditPopupDelete);
        category_spinner = popupView.findViewById(R.id.productCategoryEdit);
        categories = new ArrayList<String>();

        /*Both*/
        TextView tvAreYouSure;
        Button button_add_to_cart, button_quick_buy, button_yes, button_no;
        NumberPicker editPopupNumberPicker;
        ImageButton button_close;

        editPopupNumberPicker = popupView.findViewById(R.id.editPopupNumberPicker);
        button_add_to_cart = popupView.findViewById(R.id.btnAddToCart);
        button_quick_buy = popupView.findViewById(R.id.btnQuickBuy);
        button_yes = popupView.findViewById(R.id.btnYesBuy);
        button_no = popupView.findViewById(R.id.btnNoBuy);
        tvAreYouSure = popupView.findViewById(R.id.tvAreYouSure);
        button_close = popupView.findViewById(R.id.epp_close_button);

        /*Set number picker*/
        editPopupNumberPicker.setMinValue(0);
        editPopupNumberPicker.setMaxValue(item.getQuantity());

        /*Set edit text fields*/
        edit_item_name.setText(current_item_name);
        edit_item_cost.setText(String.valueOf(current_item_cost));
        edit_item_price.setText(String.valueOf(current_item_price));
        edit_display_item_quantity.setText(String.valueOf(current_item_quantity));

        /*Set text view fields*/
        epp_tv_item_name.setText(current_item_name);
        //Add for cost price here
        epp_tv_item_price.setText(String.valueOf(current_item_price));
        epp_tv_item_quantity.setText(String.valueOf(current_item_quantity));

        /*Spinner*/
        category_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayAdapter<String> adapter;
            int position = 0;
            int count = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                categories.add("Select Category");
                if (snapshot.exists()) {
                    for (DataSnapshot current_snapshot : snapshot.getChildren()) {
                        Category current_category = current_snapshot.getValue(Category.class);
                        categories.add(current_category.getName());
                        if (item.getCategory() != null && item.getCategory().equals(current_category.getName())) {
                            epp_tv_item_category.setText(current_category.getName());
                            position = count + 1;
                        }
                        count++;
                    }
                } else {

                }
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, categories);
                category_spinner.setAdapter(adapter);

                category_spinner.setSelection(position);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*Check user type*/
        if (cUser.getUser_type() == 0) {
            /*Standard employee*/

            /*Remove edit permissions*/
            epp_ll_info.setVisibility(View.VISIBLE);
            /*Change title (Can hard code this to the editpopup.xml, might need to do later)*/
            epp_title.setText("Item Information");
        } else {
            /*Not standard employee*/

            /*Grant edit permissions*/
            epp_ll_edit.setVisibility(View.VISIBLE);
            /*Change title (Can hard code this to the editpopup.xml, might need to do later)*/
            epp_title.setText("Edit Item");
        }

        /*Close button*/
        button_close.setOnClickListener(view -> {
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            GeneralHelper.hideKeyboard(view);
        });

        edit_item_cost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    double price = Double.parseDouble(s.toString());
                    String markup_string = edit_item_markup.getText().toString().trim();
                    if (!markup_string.isEmpty()) {
                        double markup = Double.parseDouble(markup_string);
                        double total = price + (price * (markup / 100));
                        edit_item_price.setText(Double.toString(total));
                    }
                } else {
                    edit_item_price.setText("" + current_item_price);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edit_item_markup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    double markup = Double.parseDouble(s.toString());
                    String price_string = edit_item_cost.getText().toString().trim();
                    if (!price_string.isEmpty()) {
                        double price = Double.parseDouble(price_string);
                        double total = price + (price * (markup / 100));
                        edit_item_price.setText(Double.toString(total));
                    }
                } else {
                    edit_item_price.setText("" + current_item_price);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*Edit button*/
        button_edit.setOnClickListener(view -> {

            //Set input variables
            String input_item_name, input_item_price, input_item_category, input_item_cost_price;

            //Set Item class
            Item new_item = new Item(item);

            //store editText data
            input_item_name = edit_item_name.getText().toString().trim();
            input_item_price = edit_item_price.getText().toString().trim();
            input_item_category = category_spinner.getSelectedItem().toString().trim();
            input_item_cost_price = edit_item_cost.getText().toString().trim();

            //check values
            if (!isEmpty(input_item_name)) {
                new_item.setName(input_item_name);
            }

            if (!isEmpty(input_item_price)) {
                new_item.setPrice(Double.parseDouble(input_item_price));
            }

            if (!input_item_category.equals("Select Category")) {
                new_item.setCategory(input_item_category);
            } else {
                new_item.setCategory(null);
            }

            if(!isEmpty(input_item_cost_price)) {
                new_item.setCost_price(Double.parseDouble(input_item_cost_price));
            }

            if (!(current_item_name.equals(new_item.getName()))) {
                items_ref.child(item.getName()).removeValue();
            }

            items_ref.child(new_item.getName()).setValue(new_item);

            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            GeneralHelper.hideKeyboard(view);
        });

        /*Delete button*/
        button_delete.setOnClickListener(view -> {
            items_ref.child(current_item_name).removeValue();
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            GeneralHelper.hideKeyboard(view);
        });

        /*Add to cart button*/
        button_add_to_cart.setOnClickListener(view -> {
            int selected_item_quantity = editPopupNumberPicker.getValue();

            if (selected_item_quantity == 0) {
                Toast.makeText(MainActivity.this, "Please choose quantity", Toast.LENGTH_SHORT).show();
            } else {
                //cartedItem.add(new Item(currProductName, currProductPrice, selected_product_quantity));
                cart_ref.child(current_item_name).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            Item snapshot_item = dataSnapshot.getValue(Item.class);
                            if (snapshot_item != null) {
                                int added_qty_result = selected_item_quantity + snapshot_item.getQuantity();
                                cart_ref.child(current_item_name).setValue(new Item(current_item_name, current_item_price, added_qty_result));
                            } else {
                                Log.e(FUNCTION_TAG, "Add to cart is null");
                            }
                        } else {
                            cart_ref.child(current_item_name).setValue(new Item(current_item_name, current_item_price, selected_item_quantity));
                        }
                    } else {
                        Log.e(FUNCTION_TAG, "Cart item unsuccessful");
                    }
                });
                items_ref.child(current_item_name).setValue(new Item(current_item_name,  current_item_price, current_item_cost_price, current_item_quantity - selected_item_quantity, current_item_category));
                popupWindow.dismiss();
                itemSearchBar.clearFocus();
                GeneralHelper.hideKeyboard(view);
            }
        });

        /*Quick buy button*/
        button_quick_buy.setOnClickListener(view -> {
            int selected_product_quantity = editPopupNumberPicker.getValue();

            if (selected_product_quantity == 0) {
                Toast.makeText(MainActivity.this, "Please choose quantity", Toast.LENGTH_SHORT).show();
            } else {
                button_add_to_cart.setVisibility(View.GONE);
                button_quick_buy.setVisibility(View.GONE);

                tvAreYouSure.setVisibility(View.VISIBLE);
                button_yes.setVisibility(View.VISIBLE);
                button_no.setVisibility(View.VISIBLE);
                itemSearchBar.clearFocus();
            }
        });

        /*Yes nutton*/
        button_yes.setOnClickListener(view -> {

            double customer_change;
            double customer_payment;
            double subtotal;
            int item_count;
            Object transaction_date;
            List<Item> items;
            MyTransaction transaction;
            DatabaseReference ref;

            customer_change = 0;
            subtotal = item.getPrice() * editPopupNumberPicker.getValue();
            customer_payment = subtotal;
            item_count = editPopupNumberPicker.getValue();
            transaction_date = ServerValue.TIMESTAMP;
            items = new ArrayList<>();
            items.add(new Item(item.getName(), item.getPrice(), item_count));

            if (!(item_count == 0)) {
                transaction = new MyTransaction(customer_change, customer_payment, subtotal, item_count, true, transaction_date, items, cUser.getFirst_name() + " " + cUser.getLast_name());

                ref = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).push();

                ref.setValue(transaction);

                items_ref.child(current_item_name).setValue(new Item(current_item_name, current_item_price, current_item_cost_price,current_item_quantity - item_count, current_item_category));

                openReceipt(ref.getKey());
                popupWindow.dismiss();
                itemSearchBar.clearFocus();
                GeneralHelper.hideKeyboard(view);
            } else {
                Toast.makeText(MainActivity.this, "Please choose quantity", Toast.LENGTH_SHORT).show();
            }
        });

        /*No button*/
        button_no.setOnClickListener(view ->{
            button_add_to_cart.setVisibility(View.VISIBLE);
            button_quick_buy.setVisibility(View.VISIBLE);

            tvAreYouSure.setVisibility(View.GONE);
            button_yes.setVisibility(View.GONE);
            button_no.setVisibility(View.GONE);
            itemSearchBar.clearFocus();
        });
    }

    private void itemLockOpenPopup(Item item) {
        String FUNCTION_TAG = "itemLockOpenPopup";

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_item_locked, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        Button locked_item_btn_unlock;
        ImageButton locked_item_btn_close;
        locked_item_btn_unlock = popupView.findViewById(R.id.locked_item_btn_unlock);
        locked_item_btn_close = popupView.findViewById(R.id.locked_item_btn_close);

        locked_item_btn_close.setOnClickListener(view -> {
            popupWindow.dismiss();
        });

        locked_item_btn_unlock.setOnClickListener(view -> {
            if (item_count_unmarked < Subscription.FREE_ITEM_COUNT_LIMIT) {
                items_ref.child(item.getName()).child("sub_marked").setValue(false);
                popupWindow.dismiss();
            } else {
                Toast.makeText(getApplicationContext(), "No space left to unlock item", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void restockQuantityEditOpenPopup(Item item) {
        /*Toast.makeText(getApplicationContext(), "Works", Toast.LENGTH_SHORT).show();*/
        String FUNCTION_TAG = "createRestockQuantityEditPopupWindow";

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_restock_quantity, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        TextView item_name_tv;
        EditText restock_quantity_et;
        Button set_btn;

        item_name_tv = popupView.findViewById(R.id.restockQuantityPopupItemName);
        restock_quantity_et = popupView.findViewById(R.id.restockQuantityPopupQuantity);
        set_btn = popupView.findViewById(R.id.restockQuantityPopupSetBtn);

        item_name_tv.setText(item.getName());
        restock_quantity_et.setText(String.valueOf(item.getRestock_quantity()));

        set_btn.setOnClickListener(view -> {
            items_ref.child(item.getName()).child("restock_quantity").setValue(Integer.parseInt(restock_quantity_et.getText().toString()));
            GeneralHelper.hideKeyboard(view);
            popupWindow.dismiss();
        });
    }

    private void restockCostPriceEditOpenPopup(Item item) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_restock, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        TextView text_cost_price;
        EditText editText_new_cost_price;
        Button button_set;
        DatabaseReference current_item_ref;

        text_cost_price = popupView.findViewById(R.id.restockPopupCostPrice);
        editText_new_cost_price = popupView.findViewById(R.id.restockPopupNewCostPrice);
        button_set = popupView.findViewById(R.id.restockPopupBtnSet);
        current_item_ref = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code()).child(item.getName());

        if (item.getCost_price() == 0.0) {
            text_cost_price.setText("NOT SET");
        } else {
            text_cost_price.setText("₱" + item.getCost_price());
        }

        button_set.setOnClickListener(view -> {
            Double New_Cost_Price = Double.parseDouble(editText_new_cost_price.getText().toString());
            item.setCost_price(New_Cost_Price);

            current_item_ref.setValue(item);

            popupWindow.dismiss();
            restockingSearchBar.clearFocus();
            GeneralHelper.hideKeyboard(view);
        });
    }

    private void employeeApprovalOpenPopup(User emp_user) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_employee_approval, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        TextView employee_name;
        Button yes, no;
        DatabaseReference emp_user_ref;

        employee_name = popupView.findViewById(R.id.eapTvEmployeeName);
        yes = popupView.findViewById(R.id.eapBtnYes);
        no = popupView.findViewById(R.id.eapBtnNo);
        emp_user_ref = firebaseDatabaseHelper.getUserRef(emp_user.getUid());

        employee_name.setText(String.format("%s %s", emp_user.getFirst_name(), emp_user.getLast_name()));

        yes.setOnClickListener(view -> {
            emp_user_ref.child("status").setValue(1);
            popupWindow.dismiss();
        });

        no.setOnClickListener(view -> {
            emp_user_ref.child("business_code").setValue("null");
            popupWindow.dismiss();
        });
    }

    private void employeeInfoOpenPopup(User emp_user) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_employee, null);

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        TextView employee_name, employee_status;
        RadioButton employee_standard, employee_inv_manager;
        RadioGroup employee_type;
        Button dismiss, update;
        ImageButton close;
        DatabaseReference emp_user_type_ref;

        employee_name = popupView.findViewById(R.id.epTvEmployeeName);
        employee_status = popupView.findViewById(R.id.epTvEmployeeStatus);
        employee_type = popupView.findViewById(R.id.epRbEmpType);
        employee_standard = popupView.findViewById(R.id.epRbEmpTypeNorm);
        employee_inv_manager = popupView.findViewById(R.id.epRbEmpTypeInv);
        dismiss = popupView.findViewById(R.id.epBtnDismiss);
        update = popupView.findViewById(R.id.epBtnUpdate);
        close = popupView.findViewById(R.id.employee_popup_close_button);
        emp_user_type_ref = firebaseDatabaseHelper.getUserRef(emp_user.getUid()).child("user_type");

        employee_name.setText(String.format("%s %s", emp_user.getFirst_name(), emp_user.getLast_name()));
        if (emp_user.getStatus() == 1) {
            employee_status.setText("Inactive");
            employee_status.setBackgroundColor(Color.GRAY);
        } else {
            employee_status.setText("Active");
            employee_status.setBackgroundColor(Color.GREEN);
        }

        switch (emp_user.getUser_type()) {
            case 0:
                employee_type.check(R.id.epRbEmpTypeNorm);
                break;
            case 2:
                employee_type.check(R.id.epRbEmpTypeInv);
                break;
        }

        dismiss.setOnClickListener(view -> {
            firebaseDatabaseHelper.getUserRef(emp_user.getUid()).child("business_code").setValue("null");
            firebaseDatabaseHelper.getUserRef(emp_user.getUid()).child("status").setValue(0);
            firebaseDatabaseHelper.getUserRef(emp_user.getUid()).child("curr_punch_in_code").removeValue();
            firebaseDatabaseHelper.getUserRef(emp_user.getUid()).child("user_type").setValue(0);
            popupWindow.dismiss();
        });

        update.setOnClickListener(view -> {
            if (employee_standard.isChecked()) {
                emp_user_type_ref.setValue(0);
            } else if (employee_inv_manager.isChecked()) {
                emp_user_type_ref.setValue(2);
            }
            popupWindow.dismiss();
        });

        close.setOnClickListener(view -> {
            popupWindow.dismiss();
        });
    }

    private void categorySettingOpenPopup(int position, Category model, DatabaseReference ref) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_category_edit, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        EditText name_field;
        Button edit_button, delete_button;

        name_field = popupView.findViewById(R.id.category_edit_field);
        edit_button = popupView.findViewById(R.id.category_edit_edit_btn);
        delete_button = popupView.findViewById(R.id.category_edit_delete_btn);

        name_field.setText(model.getName());

        edit_button.setOnClickListener(view -> {
            String inputted_name = name_field.getText().toString().trim();

            if (!inputted_name.isEmpty()) {
                cascadeEditCategory(ref, inputted_name, model.getName());
                popupWindow.dismiss();
            } else {
                cascadeDeleteCategory(ref, model.getName());
                popupWindow.dismiss();
            }
        });

        delete_button.setOnClickListener(view -> {
            cascadeDeleteCategory(ref, model.getName());
            popupWindow.dismiss();
        });
    }

    private void cascadeEditCategory(DatabaseReference ref, String new_name, String old_name) {
        /**/
        items_ref.orderByChild("category").equalTo(old_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot curr_snap : snapshot.getChildren()) {
                        curr_snap.getRef().child("category").setValue(new_name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("name").setValue(new_name);
    }

    private void cascadeDeleteCategory(DatabaseReference ref, String name) {
        /*Delete instances in items*/
        items_ref.orderByChild("category").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot curr_snap : snapshot.getChildren()) {
                        curr_snap.getRef().child("category").removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.removeValue();
    }

    @Override
    public void onHistoryItemClick(int position, String key) {
        openReceipt(key);
    }

    //Item List OnClick Listener
    @Override
    public void onItemClick(int position, Item item, int type) {
        if (type == 0) {
            itemEditOpenPopup(item);
        } else if (type == 1) {
            itemLockOpenPopup(item);
        } else {
            Log.wtf("???", "???");
        }
    }

    //Employee List OnClick Listener

    @Override
    public void onEmployeeClick(int position, User emp_user, boolean pending_approval) {
        if (pending_approval) {
            employeeApprovalOpenPopup(emp_user);
        } else {
            employeeInfoOpenPopup(emp_user);
        }
    }

    //Restock List OnClick Listeners
    @Override
    public void onRestockItemClick(int position, int type,Item model) {
        switch (type) {
            case 0:
                restockCostPriceEditOpenPopup(model);
                break;
            case 1:
                restockQuantityEditOpenPopup(model);
                break;
        }

    }

    @Override
    public void onItemClick(int position, Category model, DatabaseReference ref, int type) {
        switch (type) {
            case 0:
                filterByCategory(model);
                break;
            case 1:
                categorySettingOpenPopup(position, model, ref);
                break;
            case 2:
                resetItemList();
                break;
        }
    }

    void refreshItems(){
        //customAdapter.notifyDataSetChanged();
        itemSearchBar.clearFocus();
    }

    private void openReceipt(String key) {
        Intent intent = new Intent(MainActivity.this, ReceiptActivity.class);
        intent.putExtra("key", key);
        startActivity(intent);
    }

    private void resetItemList() {
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(item_query, Item.class)
                        .build();
        listAdapterItemFirebase = new ListAdapterItemFirebase(options, this, lock_marked);
        rvItems.setAdapter(listAdapterItemFirebase);
        listAdapterItemFirebase.startListening();
    }
}