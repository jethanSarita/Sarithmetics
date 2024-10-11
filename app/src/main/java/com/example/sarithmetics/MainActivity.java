package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListAdapterItemFirebase.OnItemClickListener, ListAdapterEmployeeFirebase.OnItemClickListener, ListAdapterRestockFirebase.OnItemClickListener, AdapterView.OnItemSelectedListener, ListAdapterHistoryFirebase.OnItemClickListener, ListAdapterCategoryFirebase.OnItemClickListener {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String TAG = "firebaseDatabase MainAct";
    FloatingActionButton add_button, restock_button;
    String punch_in_code;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView settings, homeIcon, cart_button, eye_open_button, eye_close_button;
    RelativeLayout home_layout, items_layout, insights_layout, restock_layout, history_layout;
    MyDatabaseHelper database;
    ArrayList<String> listItemID;
    ArrayList<String> listItemName;
    ArrayList<String> listItemPrice;
    ArrayList<String> listItemQty;
    SessionManager sessionManager;
    TextView profileFnLNameBusinessOwner, profileFnLNameEmployee, tv_business_code, maTvStatusNotSync, maTvStatusPending, amTvCurrentPunchInCode, employeeStatus, profileFnLUserType, item_total_sales_vol_tv, item_revenue_tv, item_turnover_rate_tv, top1_tv, top2_tv, top3_tv;
    //ListAdapterItem listAdapterItem;
    ListAdapterItemFirebase listAdapterItemFirebase;
    ListAdapterCategoryFirebase listAdapterCategoryFirebase;
    ListAdapterEmployeeFirebase listAdapterEmployeeFirebase;
    ListAdapterRestockFirebase listAdapterRestockFirebase;
    ListAdapterHistoryFirebase listAdapterHistoryFirebase;
    RecyclerView rvItems, rvEmployees, rvRestocking, rvHistory, rvCategory;
    ArrayList<Product> cartedProduct, currProduct;
    ArrayList<Item> cartedItem;
    Spinner insight_item_spinner, insight_context_spinner, category_spinner;
    ArrayList<String> insight_item_list, insight_context_list;
    ArrayAdapter<String> insight_item_adapter, insight_context_adapter;
    LinearLayout boxBusinessCode, llEmployeeLayoutYesSync, llEmployeeLayoutNoSync, llEmployeeLayoutPendingSync;
    androidx.appcompat.widget.SearchView itemSearchBar, restockingSearchBar;
    EditText etBusinessCode, etPunchInCode;
    Button btnEnterBusinessCode, amBtnGeneratePunchInCode, btnEnterPunchInCode, history_filter_button;
    //ScrollView maSvItems;
    RandomHelper randomHelper;
    MenuItem nav_insights, nav_restock;
    ImageButton category_plus_btn;

    //Database
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser, lUser;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference user_ref, items_ref, cart_ref, business_ref, business_code_ref, history_ref, category_ref;
    Query item_query, employee_query, restock_query, history_query, category_query;

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

    /*Internet monitoring*/
    /*private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Toast.makeText(getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Toast.makeText(getApplicationContext(), "Lost", Toast.LENGTH_SHORT).show();
            Log.e("MyNetTest", "MainActivity - OnLost");
            startNoConnectionActivity();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };*/

    /*NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();*/

    private void startNoConnectionActivity() {
        startActivity(new Intent(getApplicationContext(), NoConnectionActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeAds();

        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        randomHelper = new RandomHelper();

        punch_in_code = null;

        /*Sets up internet monitoring*/
        /*ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);*/

        /*firebase*/
        firebaseDatabase = FirebaseDatabase.getInstance(DB);
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        /*session*/
        sessionManager = new SessionManager(getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();

        /*database*/
        database = new MyDatabaseHelper(MainActivity.this);

        /*general hooks*/
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        settings = findViewById(R.id.vEyeCloseIcon);
        homeIcon = findViewById(R.id.homeIcon);

        /*navigation*/
        nav_insights = navigationView.getMenu().findItem(R.id.nav_insights);
        nav_restock = navigationView.getMenu().findItem(R.id.nav_restock);

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

        /*array lists*/
        currProduct = new ArrayList<>();
        cartedProduct = new ArrayList<>();
        cartedItem = new ArrayList<>();
        listItemID = new ArrayList<>();
        listItemName = new ArrayList<>();
        listItemPrice = new ArrayList<>();
        listItemQty = new ArrayList<>();

        /*Loading system*/
        systemLoading = new SystemLoading(MainActivity.this);
        TIMEOUT_DURATION = 10000;
        time_out_handler = new Handler();
        time_out_runnable = new Runnable() {
            @Override
            public void run() {
                //startNoConnectionActivity();
                Toast.makeText(getApplicationContext(), "Not online", Toast.LENGTH_SHORT).show();
            }
        };
        time_out_handler.postDelayed(time_out_runnable, TIMEOUT_DURATION);
        //Initiate login
        systemLoading.startLoadingDialog();


        //Clear cart
        cartedItem.clear();

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

        // THIS IS REDUNDANT NOW but keeping it for some reason idk uwu
        /*database arraylist storing*/
        //storeDataInArrays();
        /*customAdapter = new CustomAdapter(MainActivity.this, listItemName, listItemPrice, listItemQty, this);
        rvItems.setAdapter(customAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(MainActivity.this));*/


        eye_close_button.setOnClickListener(view -> {
            eyeClose();
        });

        eye_open_button.setOnClickListener(view -> {
            eyeOpen();
        });

        settings.setOnClickListener(view -> {
            openSettings();
        });

        homeIcon.setOnClickListener(view -> {

        });

        cart_button.setOnClickListener(view -> {
            openCart();
        });

        add_button.setOnClickListener(view -> {
            itemAddOpenPopup();
        });

        restock_button.setOnClickListener(view -> {
            restockItems();
        });

        history_filter_button.setOnClickListener(view ->{
            openDatePickerDialog();
        });

        category_plus_btn.setOnClickListener(view -> {
            //business_code_ref.child("categories").push().setValue("Test");
            categoryAddOpenPopup();
        });
    }

    private void openDatePickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();

                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                long start = calendar.getTimeInMillis();

                calendar.set(year, month, dayOfMonth, 23, 59, 59);
                long end = calendar.getTimeInMillis();

                long[] date = new long[] {start, end};

                TextView history_selected_date = findViewById(R.id.history_selected_date);

                history_selected_date.setText(year + "-" + (month + 1) + "-" + dayOfMonth);

                transactionDateFilter(date);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void restockItems() {
        String FUNCTION_TAG = "restockItems";

        String key;

        MyTransaction transaction;

        /*double customer_change;
        double customer_payment;*/
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

        /*Get data for each item in adapter*/
        for (int i = 0; i < listAdapterRestockFirebase.getItemCount(); i++) {

            Item item = listAdapterRestockFirebase.getItem(i);
            int new_stock_quantity ;
            int new_restock_quantity;

            /*Check if current item is being restocked*/
            if (item.getRestock_quantity() != 0) {
                restocks_count++;

                new_stock_quantity = item.getRestock_quantity() + item.getQuantity();
                new_restock_quantity = 0;

                items.add(new Item(item.getName(), item.getCost_price(), item.getRestock_quantity()));

                subtotal += item.getCost_price() * item.getRestock_quantity();
                item_count += item.getRestock_quantity();

                item.setQuantity(new_stock_quantity);
                item.setRestock_quantity(new_restock_quantity);

                /*Update item's stock*/
                items_ref.child(item.getName()).setValue(item).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(FUNCTION_TAG, item.getName() + " has been restocked");
                    } else {
                        Log.e(FUNCTION_TAG, item.getName() + " has had an error:\n" + task);
                    }
                });
            } else {
                Log.e(FUNCTION_TAG, item.getName() + "'s restock_quantity is empty");
            }
        }

        if (restocks_count > 0) {
            transaction = new MyTransaction(0.0, 0.0, subtotal, item_count, false, transaction_date, items, cUser.getFirst_name() + " " + cUser.getLast_name());

            ref.setValue(transaction);

            openReceipt(key);
        } else {
            Toast.makeText(getApplicationContext(), "No items has restock quantity set", Toast.LENGTH_SHORT).show();
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
    }

    private void initializeAds() {
        MobileAds.initialize(this, initializationStatus -> {

        });

        mAdView = findViewById(R.id.adViewItems);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void openCart() {
        Intent intent = new Intent(MainActivity.this, CartActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        //This has no functionality or system yet just a debugging and testing button
        refreshItems();
        Toast.makeText(getApplicationContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
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
        user_ref = firebaseDatabaseHelper.getCurrentUserRef();
        if (firebaseDatabaseHelper.getFirebaseUser() == null) {
            logout();
        }
        user_ref.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
            } else {
                Log.d(TAG, "Got User object: " + (task.getResult().getValue()));

                time_out_handler.removeCallbacks(time_out_runnable);
                
                cUser = task.getResult().getValue(User.class);
                
                setUserData();

                //item_list_ref = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code()); //might be unnecessary

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

            //Sidebar continuity
            sidebarContinuity();

            //List of Items
            setUpItemList();

            //List of Categories
            setUpCategoryList();

            //List of Employees
            setUpEmployeeList();

            //List of Restock
            setUpRestockList();

            //List of History Out
            setUpHistoryList();

            //Search item search bar functionality
            setUpItemSearchBar();

            //Search restock search bar functionality
            setUpRestockSearchBar();

            //Dismiss loading popup
            systemLoading.dismissDialog();
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
        listAdapterItemFirebase = new ListAdapterItemFirebase(options1, this, cUser);
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
    }

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
        listAdapterCategoryFirebase.startListening();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvCategory);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from_position = viewHolder.getBindingAdapterPosition();
            int to_position = target.getBindingAdapterPosition();

            

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        amBtnGeneratePunchInCode.setOnClickListener(view -> {
            business_code_ref.child("punch in code").setValue(randomHelper.generateRandom5NumberCharString());
        });
    }

    private void processEmployeeUser() {
        //Set home layout to employee version
        home_layout = findViewById(R.id.layout_home_employee_rl);
        //Set username text view
        profileFnLNameEmployee.setText(sessionManager.getUsername());
        //Remove access to insights
        nav_insights.setVisible(false);
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
                                employeeStatus.setText("Inactive");
                                employeeStatus.setBackgroundColor(Color.GRAY);
                                findViewById(R.id.home_employee_punch_in).setVisibility(View.VISIBLE);
                                etPunchInCode.setVisibility(View.VISIBLE);
                                btnEnterPunchInCode.setVisibility(View.VISIBLE);
                                findViewById(R.id.items_not_punched_in_tv).setVisibility(View.VISIBLE);
                                add_button.setVisibility(View.GONE);
                                //maSvItems.setVisibility(View.GONE);
                                rvItems.setVisibility(View.GONE);
                                break;
                            case 2:
                                //Active
                                employeeStatus.setText("Active");
                                employeeStatus.setBackgroundColor(Color.GREEN);
                                findViewById(R.id.home_employee_punch_in).setVisibility(View.GONE);
                                etPunchInCode.setVisibility(View.GONE);
                                btnEnterPunchInCode.setVisibility(View.GONE);
                                findViewById(R.id.items_not_punched_in_tv).setVisibility(View.GONE);
                                add_button.setVisibility(View.VISIBLE);
                                //maSvItems.setVisibility(View.VISIBLE);
                                rvItems.setVisibility(View.VISIBLE);
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
        category_ref = firebaseDatabaseHelper.getItemsCategories(cUser.getBusiness_code());
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

        if (!(insight_selected_item.equals("Choose Item") || insight_selected_context.equals("Choose Context"))) {
            displayPerformance();
        }

        if (insight_selected_item.equals("Choose Item")) {
            item_total_sales_vol_tv.setText("Pending Choice");
            item_revenue_tv.setText("Pending Choice");
        }

        if (!(insight_selected_context.equals("Choose Context"))) {
            switch (insight_selected_context) {
                case "Today":
                case "Yesterday":
                    getInsightReference(insight_selected_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot day = task.getResult();
                            for (DataSnapshot time : day.getChildren()) {
                                for (DataSnapshot cItem : time.getChildren()) {
                                    Item item = cItem.getValue(Item.class);
                                    if (item != null) {
                                        top_list_item.add(item);
                                    } else {
                                        Log.e("Insight Error", "Today/Yesterday - item is null");
                                    }
                                }
                            }
                            if (!top_list_item.isEmpty()) {
                                displayInsightPieChart(sortList(combineItems(top_list_item)), insight_selected_context);
                            } else {
                                pieChart.clear();
                                pieChart.setNoDataText("No Data for " + insight_selected_context);
                            }
                        }
                    });
                    break;
                case "This Week":
                    getInsightReference(insight_selected_context).get().addOnCompleteListener(task ->{
                        if (task.isSuccessful()) {
                            DataSnapshot week = task.getResult();
                            for (DataSnapshot day : week.getChildren()) {
                                for (DataSnapshot time : day.getChildren()) {
                                    for (DataSnapshot cItem : time.getChildren()) {
                                        Item item = cItem.getValue(Item.class);
                                        if (item != null) {
                                            top_list_item.add(item);
                                        } else {
                                            Log.e("Insight Error", "This Week - item is null");
                                        }
                                    }
                                }
                            }
                            if (!top_list_item.isEmpty()) {
                                displayInsightPieChart(sortList(combineItems(top_list_item)), insight_selected_context);
                            } else {
                                pieChart.clear();
                                pieChart.setNoDataText("No Data for " + insight_selected_context);
                            }
                        }
                    });
                    break;
                case "This Month":
                    getInsightReference(insight_selected_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot month = task.getResult();
                            for (DataSnapshot week : month.getChildren()) {
                                for (DataSnapshot day : week.getChildren()) {
                                    for (DataSnapshot time : day.getChildren()) {
                                        for (DataSnapshot cItem : time.getChildren()) {
                                            Item item = cItem.getValue(Item.class);
                                            if (item != null) {
                                                top_list_item.add(item);
                                            } else {
                                                Log.e("Insight Error", "This Week - item is null");
                                            }
                                        }
                                    }
                                }
                            }
                            if (!top_list_item.isEmpty()) {
                                displayInsightPieChart(sortList(combineItems(top_list_item)), insight_selected_context);
                            } else {
                                pieChart.clear();
                                pieChart.setNoDataText("No Data for " + insight_selected_context);
                            }
                        }
                    });
                    break;
            }
        } else {
            pieChart.clear();
        }
    }

    private void displayPerformance() {
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
    }

    private void displayPerformanceMonth() {
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
    }

    private void displayPerformanceWeek() {
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
    }

    private void displayPerformanceTodayOrYesterday() {
        getInsightReference(insight_selected_context).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot day = task.getResult();
                for (DataSnapshot time : day.getChildren()) {
                    for (DataSnapshot cItem : time.getChildren()) {
                        Item item = cItem .getValue(Item.class);
                        if (item != null) {
                            if (item.getName().equals(insight_selected_item)) {
                                total_sales_vol += item.getQuantity();
                                total_rev += item.getPrice() * item.getQuantity();
                            }
                        } else {
                            Log.e("Insight Error", "Today/Yesterday - item is null");
                        }
                    }
                }
            }
            item_total_sales_vol_tv.setText(total_sales_vol + " Sold");
            item_revenue_tv.setText("₱" + total_rev + " Worth Sold");
        });
    }

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

    public DatabaseReference getInsightReference(String context) {
        switch (context) {
            case "Today":
                return history_ref
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)))
                        .child(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + firebaseDatabaseHelper.getDayOfWeek(0));
            case "Yesterday":
                return history_ref
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)))
                        .child((Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1) + "-" + firebaseDatabaseHelper.getDayOfWeek(1));
            case "This Week":
                return history_ref
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)));
            case "This Month":
                return history_ref
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                        .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1));
            default:
                return null;
        }
    }

    private void txtItemSearch(String str) {
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(items_ref.orderByChild("name").startAt(str).endAt(str + "~"), Item.class)
                        .build();

        listAdapterItemFirebase = new ListAdapterItemFirebase(options, this, cUser);
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
        listAdapterItemFirebase = new ListAdapterItemFirebase(options, this, cUser);
        listAdapterItemFirebase.startListening();
        rvItems.setAdapter(listAdapterItemFirebase);
    }

    private void transactionDateFilter(long[] date) {
        FirebaseRecyclerOptions<MyTransaction> options =
                new FirebaseRecyclerOptions.Builder<MyTransaction>()
                        .setQuery(history_ref.orderByChild("transaction_date").startAt(date[0]).endAt(date[1]), MyTransaction.class)
                        .build();

        listAdapterHistoryFirebase = new ListAdapterHistoryFirebase(options, this, cUser);
        listAdapterHistoryFirebase.startListening();
        rvHistory.setAdapter(listAdapterHistoryFirebase);
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
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_item_add, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        List<String> categories;
        Button add_btn, close_btn;
        EditText name_field, price_field, cost_price_field;
        Spinner category_spinner;

        name_field = popupView.findViewById(R.id.popup_item_add_name);
        price_field = popupView.findViewById(R.id.popup_item_add_price);
        cost_price_field = popupView.findViewById(R.id.popup_item_add_cost_price);
        add_btn = popupView.findViewById(R.id.btnPopupAdd);
        close_btn = popupView.findViewById(R.id.btnPopupClose);
        category_spinner = popupView.findViewById(R.id.popup_item_category_spinner);
        categories = new ArrayList<>();
        categories.add("Select Category");

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
                category_spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        close_btn.setOnClickListener(view -> {
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            hideKeyboard(view);
        });

        add_btn.setOnClickListener(view -> {

            boolean condition = false;

            //Store field data in temp variables
            String name, price, cost_price, category;
            name =  name_field.getText().toString().trim();
            price = price_field.getText().toString().trim();
            cost_price = cost_price_field.getText().toString().trim();
            category = category_spinner.getSelectedItem().toString();

            //To check if fields were empty. No point storing in official data if they are empty
            if (!isEmpty(name)) {
                //pName = name;

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

                items_ref.child(name).setValue(item);

                popupWindow.dismiss();
                itemSearchBar.clearFocus();
                hideKeyboard(view);
            } else {
                Toast.makeText(MainActivity.this, "Item should at least have a name", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*Popup when editing an item*/
    private void itemEditOpenPopup(Item item) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_item_edit, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));


        String current_item_name = item.getName();
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
        TextView epp_tv_item_name, epp_tv_item_price, epp_tv_item_quantity;

        epp_ll_info = popupView.findViewById(R.id.epp_ll_info);
        epp_tv_item_name = popupView.findViewById(R.id.epp_tv_item_name);
        epp_tv_item_price = popupView.findViewById(R.id.epp_tv_item_price);
        epp_tv_item_quantity = popupView.findViewById(R.id.epp_tv_item_quantity);

        /*edit*/
        LinearLayout epp_ll_edit;
        Button button_edit, button_close, button_delete;
        EditText edit_item_name, edit_item_price;
        TextView edit_display_item_quantity;
        Spinner category_spinner;
        List<String> categories;

        epp_ll_edit = popupView.findViewById(R.id.epp_ll_edit);
        edit_item_name = popupView.findViewById(R.id.productNameEdit);
        edit_item_price = popupView.findViewById(R.id.productPriceEdit);
        edit_display_item_quantity = popupView.findViewById(R.id.productQuantityEdit);
        button_edit = popupView.findViewById(R.id.btnEditPopupEdit);
        button_close = popupView.findViewById(R.id.btnEditPopupClose);
        button_delete = popupView.findViewById(R.id.btnEditPopupDelete);
        category_spinner = popupView.findViewById(R.id.productCategoryEdit);
        categories = new ArrayList<String>();

        /*Both*/
        TextView tvAreYouSure;
        Button button_add_to_cart, button_quick_buy, button_yes, button_no;
        NumberPicker editPopupNumberPicker;

        editPopupNumberPicker = popupView.findViewById(R.id.editPopupNumberPicker);
        button_add_to_cart = popupView.findViewById(R.id.btnAddToCart);
        button_quick_buy = popupView.findViewById(R.id.btnQuickBuy);
        button_yes = popupView.findViewById(R.id.btnYesBuy);
        button_no = popupView.findViewById(R.id.btnNoBuy);
        tvAreYouSure = popupView.findViewById(R.id.tvAreYouSure);

        //Set number picker
        editPopupNumberPicker.setMinValue(0);
        editPopupNumberPicker.setMaxValue(item.getQuantity());

        //Set edit text fields
        edit_item_name.setText(current_item_name);
        edit_item_price.setText(String.valueOf(current_item_price));
        edit_display_item_quantity.setText(String.valueOf(current_item_quantity));

        //Set text view fields
        epp_tv_item_name.setText(current_item_name);
        epp_tv_item_price.setText(String.valueOf(String.valueOf(current_item_price)));
        epp_tv_item_quantity.setText(String.valueOf(String.valueOf(current_item_quantity)));

        //Spinner
        categories.add("Select Category");
        category_ref.addValueEventListener(new ValueEventListener() {
            ArrayAdapter<String> adapter;
            int position = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot current_snapshot : snapshot.getChildren()) {
                        Category current_category = current_snapshot.getValue(Category.class);
                        categories.add(current_category.getName());
                        if (item.getCategory() != null && item.getCategory().equals(current_category.getName())) {
                            position = current_category.getPriority();
                        }
                    }
                }
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, categories);
                category_spinner.setAdapter(adapter);
                category_spinner.setSelection(position + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Check user type
        if (cUser.getUser_type() == 0) {
            //Standard employee

            //Remove edit permissions
            epp_ll_info.setVisibility(View.VISIBLE);
            //Change title (Can hard code this to the editpopup.xml, might need to do later)
            epp_title.setText("Item Information");
        } else {
            //Not standard employee

            //Grant edit permissions
            epp_ll_edit.setVisibility(View.VISIBLE);
            ////Change title (Can hard code this to the editpopup.xml, might need to do later)
            epp_title.setText("Edit Item");
        }

        //Close button
        button_close.setOnClickListener(view -> {
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            hideKeyboard(view);
        });

        //Edit button
        button_edit.setOnClickListener(view -> {

            //Set input variables
            String input_item_name, input_item_price, input_item_category;

            //Set Item class
            Item new_item = new Item();

            /*//set object variables
            String obj_item_name;
            double obj_item_price;
            String obj_item_category;*/

            //store editText data
            input_item_name = edit_item_name.getText().toString().trim();
            input_item_price = edit_item_price.getText().toString().trim();
            input_item_category = category_spinner.getSelectedItem().toString().trim();

            //check values
            if (!isEmpty(input_item_name)) {
                new_item.setName(input_item_name);
            } else {
                new_item.setName(current_item_name);
            }

            if (!isEmpty(input_item_price)) {
                new_item.setPrice(Double.parseDouble(input_item_price));
            } else {
                new_item.setPrice(current_item_price);
            }

            if (!input_item_category.equals("Select Category")) {
                new_item.setCategory(input_item_category);
            } else {
                new_item.setCategory(current_item_category);
            }

            new_item.setCost_price(current_item_cost_price);

            new_item.setQuantity(current_item_quantity);

            items_ref.child(new_item.getName()).setValue(new_item);

            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            hideKeyboard(view);
        });

        //Delete button
        button_delete.setOnClickListener(view -> {
            items_ref.child(current_item_name).removeValue();
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            hideKeyboard(view);
        });

        //Add to cart button
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
                                Log.e(TAG, "add to cart is null 557");
                            }
                        } else {
                            cart_ref.child(current_item_name).setValue(new Item(current_item_name, current_item_price, selected_item_quantity));
                        }
                    } else {
                        Log.e(TAG, "cart item unsuccessful");
                    }
                });
                items_ref.child(current_item_name).setValue(new Item(current_item_name, current_item_price, current_item_quantity - selected_item_quantity));
                popupWindow.dismiss();
                itemSearchBar.clearFocus();
                hideKeyboard(view);
            }
        });

        //Quick buy button
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

        button_yes.setOnClickListener(view -> {

            //int selected_product_quantity = editPopupNumberPicker.getValue();

            double customer_change;
            double customer_payment;
            double subtotal;
            int item_count;
            Object transaction_date;
            List<Item> items;
            MyTransaction transaction;
            DatabaseReference ref;

            customer_change = 0.0;
            customer_payment = item.getPrice();
            subtotal = item.getPrice() * editPopupNumberPicker.getValue();
            item_count = editPopupNumberPicker.getValue();
            transaction_date = ServerValue.TIMESTAMP;
            items = new ArrayList<>();
            items.add(new Item(item.getName(), item.getPrice(), item_count));
            transaction = new MyTransaction(customer_change, customer_payment, subtotal, item_count, true, transaction_date, items, cUser.getFirst_name() + " " + cUser.getLast_name());
            ref = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).push();

            ref.setValue(transaction);

            popupWindow.dismiss();
            itemSearchBar.clearFocus();
            hideKeyboard(view);
        });

        button_no.setOnClickListener(view ->{
            button_add_to_cart.setVisibility(View.VISIBLE);
            button_quick_buy.setVisibility(View.VISIBLE);

            tvAreYouSure.setVisibility(View.GONE);
            button_yes.setVisibility(View.GONE);
            button_no.setVisibility(View.GONE);
            itemSearchBar.clearFocus();
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
            hideKeyboard(view);
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
            hideKeyboard(view);
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
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.CENTER, 0, 0));

        TextView employee_name, employee_status;
        RadioButton employee_standard, employee_inv_manager;
        RadioGroup employee_type;
        Button dismiss, update, close;
        DatabaseReference emp_user_type_ref;

        employee_name = popupView.findViewById(R.id.epTvEmployeeName);
        employee_status = popupView.findViewById(R.id.epTvEmployeeStatus);
        employee_type = popupView.findViewById(R.id.epRbEmpType);
        employee_standard = popupView.findViewById(R.id.epRbEmpTypeNorm);
        employee_inv_manager = popupView.findViewById(R.id.epRbEmpTypeInv);
        dismiss = popupView.findViewById(R.id.epBtnDismiss);
        update = popupView.findViewById(R.id.epBtnUpdate);
        close = popupView.findViewById(R.id.epBtnClose);
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
                cascadeEditCategory(ref, model.getName(), inputted_name);
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

        /*Renew priority numbering*/
        category_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot curr_snap : snapshot.getChildren()) {
                        if (!(curr_snap.getKey().equals(ref.getKey()))) {
                            category_ref.child(curr_snap.getKey()).child("priority").setValue(count);
                            count++;
                        }
                    }
                }
                ref.removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onHistoryItemClick(int position, String key) {
        openReceipt(key);
    }

    //Item List OnClick Listener
    @Override
    public void onItemClick(int position, Item item) {
        itemEditOpenPopup(item);
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
        listAdapterItemFirebase = new ListAdapterItemFirebase(options, this, cUser);
        rvItems.setAdapter(listAdapterItemFirebase);
        listAdapterItemFirebase.startListening();
    }
}