package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CustomAdapter.OnItemClickListener, MainAdapter.OnItemClickListener, EmployeeAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String TAG = "firebaseDatabase MainAct";
    FloatingActionButton add_button;
    String punch_in_code;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView settings, homeIcon, cart_button, eye_open_button, eye_close_button;
    RelativeLayout home_layout, items_layout, insights_layout;
    MyDatabaseHelper database;
    ArrayList<String> listItemID;
    ArrayList<String> listItemName;
    ArrayList<String> listItemPrice;
    ArrayList<String> listItemQty;
    SessionManager sessionManager;
    TextView profileFnLNameBusinessOwner, profileFnLNameEmployee, tv_business_code, maTvStatusNotSync, maTvStatusPending, amTvCurrentPunchInCode, employeeStatus, profileFnLUserType, item_total_sales_vol_tv, item_revenue_tv, item_turnover_rate_tv, top1_tv, top2_tv, top3_tv;
    CustomAdapter customAdapter;
    MainAdapter mainAdapter;
    EmployeeAdapter employeeAdapter;
    RecyclerView rvItems, rvEmployees;
    ArrayList<Product> cartedProduct, currProduct;
    ArrayList<Item> cartedItem;
    Spinner insight_item_spinner, insight_context_spinner;
    ArrayList<String> insight_item_list, insight_context_list;
    ArrayAdapter<String> insight_item_adapter, insight_context_adapter;
    LinearLayout boxBusinessCode, llEmployeeLayoutYesSync, llEmployeeLayoutNoSync, llEmployeeLayoutPendingSync;
    androidx.appcompat.widget.SearchView itemSearchBar;
    EditText etBusinessCode, etPunchInCode;
    Button btnEnterBusinessCode, amBtnGeneratePunchInCode, btnEnterPunchInCode;
    ScrollView maSvItems;
    RandomHelper randomHelper;
    MenuItem nav_insights;

    //Database
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser, lUser;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference user_ref, items_ref, cart_ref, business_ref, business_code_ref, history_ref;
    Query item_query, employee_query;

    ArrayAdapter<String> adp;

    /*Internet monitoring*/
    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Toast.makeText(getApplicationContext(), "Connected to the internet", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), NoConnectionActivity.class));
            finish();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };

    NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomHelper = new RandomHelper();

        punch_in_code = null;

        /*Sets up internet monitoring*/
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);
        ////Make some checks for internet connectivity, thank you in advance me -Jethan

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

        /*home page layout*/
        profileFnLNameEmployee = findViewById(R.id.profileFnLNameEmployee);
        profileFnLNameBusinessOwner = findViewById(R.id.profileFnLNameBusinessOwner);
        tv_business_code = findViewById(R.id.tvBusinessCode);
        rvItems = findViewById(R.id.recyclerViewItem);
        rvEmployees = findViewById(R.id.rvEmployees);
        maTvStatusNotSync = findViewById(R.id.maTvStatusNotSync);
        maTvStatusPending = findViewById(R.id.maTvStatusPending);
        amBtnGeneratePunchInCode = findViewById(R.id.amBtnGeneratePunchInCode);
        amTvCurrentPunchInCode = findViewById(R.id.amTvCurrentPunchInCode);
        etPunchInCode = findViewById(R.id.etPunchInCode);
        btnEnterPunchInCode = findViewById(R.id.btnEnterPunchInCode);
        employeeStatus = findViewById(R.id.employeeStatus);
        profileFnLUserType = findViewById(R.id.profileFnLUserType);

        /*employee session hooks*/
        llEmployeeLayoutYesSync = findViewById(R.id.llEmployeeLayoutYesSync);
        llEmployeeLayoutNoSync = findViewById(R.id.llEmployeeLayoutNoSync);
        llEmployeeLayoutPendingSync = findViewById(R.id.llEmployeeLayoutPendingSync);
        etBusinessCode = findViewById(R.id.etBusinessCode);
        btnEnterBusinessCode = findViewById(R.id.btnEnterBusinessCode);

        /*items page hooks*/
        items_layout = findViewById(R.id.layoutItems);
        itemSearchBar = findViewById(R.id.itemSearchBar);
        cart_button = findViewById(R.id.ivCart);
        add_button = findViewById(R.id.ivAddItem);
        maSvItems = findViewById(R.id.maSvItems);

        /*insights layout*/
        insights_layout = findViewById(R.id.layout_insight);
        insight_item_list = new ArrayList<>();
        insight_context_list = new ArrayList<>(Arrays.asList("Choose Context", "Today", "Yesterday", "This Week", "This Month"));
        insight_item_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, insight_item_list);
        insight_context_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, insight_context_list);
        item_total_sales_vol_tv = findViewById(R.id.item_total_sales_vol_tv);
        item_revenue_tv = findViewById(R.id.item_revenue_tv);
        //item_turnover_rate_tv = findViewById(R.id.item_turnover_rate_tv);
        top1_tv = findViewById(R.id.top1_tv);
        top2_tv = findViewById(R.id.top2_tv);
        top3_tv = findViewById(R.id.top3_tv);

        eye_open_button = findViewById(R.id.ivEyeOpenIcon);
        eye_close_button = findViewById(R.id.ivEyeCloseIcon);
        boxBusinessCode = findViewById(R.id.boxBusinessCode);

        /*array lists*/
        currProduct = new ArrayList<>();
        cartedProduct = new ArrayList<>();
        cartedItem = new ArrayList<>();
        listItemID = new ArrayList<>();
        listItemName = new ArrayList<>();
        listItemPrice = new ArrayList<>();
        listItemQty = new ArrayList<>();

        //Clear cart
        cartedItem.clear();

        insight_item_spinner = findViewById(R.id.insight_item_perf_spinner);
        insight_context_spinner = findViewById(R.id.insight_context_perf_spinner);

        //Check if session exists
        checkInternet();

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
            OpenAddItemPopup();
        });
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
        user_ref.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
            } else {
                Log.d(TAG, "Got User object: " + (task.getResult().getValue()));
                
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

            //List of Employees
            setUpEmployeeList();

            //Search bar functionality
            setUpSearchBar();
        });
    }

    private void setUpSearchBar() {
        itemSearchBar.clearFocus();
        itemSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                txtSearch(query);
                //filterList(newText);
                return true;
            }
        });
    }

    private void setUpEmployeeList() {
        rvEmployees.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        employee_query = firebaseDatabaseHelper.getEmployeesQuery(cUser.getBusiness_code());
        FirebaseRecyclerOptions<User> options2 =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(employee_query, User.class)
                        .build();
        employeeAdapter = new EmployeeAdapter(options2, this);
        rvEmployees.setAdapter(employeeAdapter);
        employeeAdapter.startListening();
    }

    private void setUpItemList() {
        rvItems.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        item_query = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code());
        FirebaseRecyclerOptions<Item> options1 =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(item_query, Item.class)
                        .build();
        mainAdapter = new MainAdapter(options1, this, cUser);
        rvItems.setAdapter(mainAdapter);
        mainAdapter.startListening();
    }

    private void sidebarContinuity() {
        switch (sessionManager.getMainStatus()) {
            case 0:
                items_layout.setVisibility(View.GONE);
                home_layout.setVisibility(View.VISIBLE);
                insights_layout.setVisibility(View.GONE);
                navigationView.setCheckedItem(R.id.nav_home);
                break;
            case 1:
                items_layout.setVisibility(View.VISIBLE);
                home_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.GONE);
                navigationView.setCheckedItem(R.id.nav_items);
                break;
            case 2:
                items_layout.setVisibility(View.GONE);
                home_layout.setVisibility(View.GONE);
                insights_layout.setVisibility(View.VISIBLE);
                navigationView.setCheckedItem(R.id.nav_insights);
                break;
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
        home_layout = findViewById(R.id.layoutHomeBusinessOwner);
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
        home_layout = findViewById(R.id.layoutHomeEmployee);
        //Set username text view
        profileFnLNameEmployee.setText(sessionManager.getUsername());

        nav_insights.setVisible(false);

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
                maSvItems.setVisibility(View.GONE);
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
                                findViewById(R.id.tvPunchIn).setVisibility(View.VISIBLE);
                                etPunchInCode.setVisibility(View.VISIBLE);
                                btnEnterPunchInCode.setVisibility(View.VISIBLE);
                                findViewById(R.id.maTvNotPunchedIn).setVisibility(View.VISIBLE);
                                maSvItems.setVisibility(View.GONE);
                                break;
                            case 2:
                                //Active
                                employeeStatus.setText("Active");
                                employeeStatus.setBackgroundColor(Color.GREEN);
                                findViewById(R.id.tvPunchIn).setVisibility(View.GONE);
                                etPunchInCode.setVisibility(View.GONE);
                                btnEnterPunchInCode.setVisibility(View.GONE);
                                findViewById(R.id.maTvNotPunchedIn).setVisibility(View.GONE);
                                maSvItems.setVisibility(View.VISIBLE);
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
                            add_button.setVisibility(View.VISIBLE);
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
        history_ref = firebaseDatabaseHelper.getBusinessTransactionHistory(cUser.getBusiness_code());
    }

    private void checkInternet() {
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String current_item = insight_item_spinner.getSelectedItem().toString();
        String current_context = insight_context_spinner.getSelectedItem().toString();
        total_sales_vol = 0;
        total_rev = 0;
        top_list_item = new ArrayList<>();

        if (!(current_item.equals("Choose Item") || current_context.equals("Choose Context"))) {
            switch (current_context) {
                case "Today":
                case "Yesterday":
                    getInsightReference(current_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            for (DataSnapshot day : dataSnapshot.getChildren()) {
                                Item item = day.getValue(Item.class);
                                if (item != null) {
                                    if (item.getName().equals(current_item)) {
                                        total_sales_vol += item.getQuantity();
                                        total_rev += item.getPrice() * item.getQuantity();
                                    }
                                } else {
                                    Log.e("Insight Error", "Today/Yesterday - item is null");
                                }
                            }
                        }
                        item_total_sales_vol_tv.setText("" + total_sales_vol);
                        item_revenue_tv.setText("" + total_rev);
                    });
                    break;
                case "This Week":
                    getInsightReference(current_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            for (DataSnapshot week : dataSnapshot.getChildren()) {
                                for (DataSnapshot day : week.getChildren()) {
                                    Item item = day.getValue(Item.class);
                                    if (item != null) {
                                        if (item.getName().equals(current_item)) {
                                            total_sales_vol += item.getQuantity();
                                            total_rev += item.getPrice() * item.getQuantity();
                                        }
                                    } else {
                                        Log.e("Insight Error", "This Week - item is null");
                                    }
                                }
                            }
                            item_total_sales_vol_tv.setText("" + total_sales_vol);
                            item_revenue_tv.setText("" + total_rev);
                        }
                    });
                    break;
                case "This Month":
                    getInsightReference(current_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            for (DataSnapshot month : dataSnapshot.getChildren()) {
                                for (DataSnapshot week : month.getChildren()) {
                                    for (DataSnapshot day : week.getChildren()) {
                                        Item item = day.getValue(Item.class);
                                        if (item != null) {
                                            if (item.getName().equals(current_item)) {
                                                total_sales_vol += item.getQuantity();
                                                total_rev += item.getPrice() * item.getQuantity();
                                            }
                                        } else {
                                            Log.e("Insight Error", "This Week - item is null");
                                        }
                                    }
                                }
                            }
                            item_total_sales_vol_tv.setText("" + total_sales_vol);
                            item_revenue_tv.setText("" + total_rev);
                        }
                    });
                    break;
            }
        }

        if (!(current_context.equals("Choose Context"))) {
            switch (current_context) {
                case "Today":
                case "Yesterday":
                    getInsightReference(current_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            for (DataSnapshot day : dataSnapshot.getChildren()) {
                                Item item = day.getValue(Item.class);
                                if (item != null) {
                                    top_list_item.add(item);
                                } else {
                                    Log.e("Insight Error", "Today/Yesterday - item is null");
                                }
                            }
                        }
                        if (!top_list_item.isEmpty()) {
                            SortAndDisplay(top_list_item);
                        } else {
                            //List is empty
                        }
                    });
                    break;
                case "This Week":
                    getInsightReference(current_context).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            for (DataSnapshot week : dataSnapshot.getChildren()) {
                                for (DataSnapshot day : week.getChildren()) {
                                    Item item = day.getValue(Item.class);
                                    if (item != null) {
                                        top_list_item.add(item);
                                    } else {
                                        Log.e("Insight Error", "This Week - item is null");
                                    }
                                }
                            }
                        }
                        if (!top_list_item.isEmpty()) {
                            SortAndDisplay(top_list_item);
                        } else {
                            //List is empty
                        }
                    });
                    break;
                case "This Month":
                    getInsightReference(current_context).get().addOnCompleteListener(task -> {
                       if (task.isSuccessful()) {
                           DataSnapshot dataSnapshot = task.getResult();
                           for (DataSnapshot month : dataSnapshot.getChildren()) {
                               for (DataSnapshot week : month.getChildren()) {
                                   for (DataSnapshot day : week.getChildren()) {
                                       Item item = day.getValue(Item.class);
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
                            SortAndDisplay(top_list_item);
                       } else {
                            //List is empty
                       }
                    });
                    break;
            }
        }
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

    private void txtSearch(String str) {
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(items_ref.orderByChild("name").startAt(str).endAt(str + "~"), Item.class)
                        .build();

        mainAdapter = new MainAdapter(options, this, cUser);
        mainAdapter.startListening();
        rvItems.setAdapter(mainAdapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.nav_home){
            items_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.GONE);
            home_layout.setVisibility(View.VISIBLE);
            sessionManager.setMainStatus(0);
        }else if(item.getItemId() == R.id.nav_items){
            home_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.GONE);
            items_layout.setVisibility(View.VISIBLE);
            sessionManager.setMainStatus(1);
        }else if(item.getItemId() == R.id.nav_insights){
            items_layout.setVisibility(View.GONE);
            home_layout.setVisibility(View.GONE);
            insights_layout.setVisibility(View.VISIBLE);
            sessionManager.setMainStatus(2);
        }else if(item.getItemId() == R.id.nav_logout){
            sessionManager.setLogin(false);
            sessionManager.setUsername(null);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            finish();
        }else if (item.getItemId() == R.id.nav_share) {

        }else if (item.getItemId() == R.id.nav_rate) {

        }else if(item.getItemId() == R.id.nav_exit) {
            finishAffinity();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void OpenAddItemPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.mainpopup, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(drawerLayout, Gravity.TOP, 0, 0);
            }
        });
        Button add, close;
        EditText productName, productPrice, productQuantity;
        productName = popupView.findViewById(R.id.productName);
        productPrice = popupView.findViewById(R.id.productPrice);
        productQuantity = popupView.findViewById(R.id.productQuantity);
        add = popupView.findViewById(R.id.btnPopupAdd);
        close = popupView.findViewById(R.id.btnPopupClose);

        close.setOnClickListener(view -> {
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
        });

        add.setOnClickListener(view -> {
            /*Write sql insertion code here*/
            MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);

            //Initialize data (In case of empty fields)
            String pName = "NULL";
            double pPrice = 0;
            int pQty = 0;

            //Store field data in temp variables
            String tempPName, tempPPrice, tempPQty;
            tempPName =  productName.getText().toString().trim();
            tempPPrice = productPrice.getText().toString().trim();
            tempPQty = productQuantity.getText().toString().trim();

            //To check if fields were empty. No point storing in official data if they are empty
            if (!isEmpty(tempPName)) {
                pName = tempPName;
            }

            if (!isEmpty(tempPPrice)) {
                pPrice = Double.parseDouble(tempPPrice);
            }

            if (!isEmpty(tempPQty)) {
                pQty = Integer.parseInt(tempPQty);
            }


            //old local database
            //myDB.addItem(pName, pPrice, pQty);

            items_ref.child(pName).setValue(new Item(pName, pPrice, pQty));
            //refreshItems();
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
        });
    }
    /*Popup when editing an item*/
    private void CreateEditPopUpWindow(String currProductName, double currProductPrice, int currProductQty) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.editpopup, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.TOP, 0, 0));

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
        EditText productName, productPrice, productQuantity;

        epp_ll_edit = popupView.findViewById(R.id.epp_ll_edit);
        productName = popupView.findViewById(R.id.productNameEdit);
        productPrice = popupView.findViewById(R.id.productPriceEdit);
        productQuantity = popupView.findViewById(R.id.productQuantityEdit);
        button_edit = popupView.findViewById(R.id.btnEditPopupEdit);
        button_close = popupView.findViewById(R.id.btnEditPopupClose);
        button_delete = popupView.findViewById(R.id.btnEditPopupDelete);

        /*Both*/
        Button button_add_to_cart;
        NumberPicker editPopupNumberPicker;

        editPopupNumberPicker = popupView.findViewById(R.id.editPopupNumberPicker);
        button_add_to_cart = popupView.findViewById(R.id.btnAddToCart);

        //Set number picker
        editPopupNumberPicker.setMinValue(0);
        editPopupNumberPicker.setMaxValue(currProductQty);

        //Set edit text fields
        productName.setText(currProductName);
        productPrice.setText(String.valueOf(currProductPrice));
        productQuantity.setText(String.valueOf(currProductQty));

        //Set text view fields
        epp_tv_item_name.setText(currProductName);
        epp_tv_item_price.setText(String.valueOf(currProductPrice));
        epp_tv_item_quantity.setText(String.valueOf(currProductQty));

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
        });

        //Edit button
        button_edit.setOnClickListener(view -> {

            //set default values
            String pName = "NULL";
            double pPrice = 0;
            int pQty = 0;
            String tempPName, tempPPrice, tempPQty;
            //take inputted values
            tempPName =  productName.getText().toString().trim();
            tempPPrice = productPrice.getText().toString().trim();
            tempPQty = productQuantity.getText().toString().trim();
            //commit values
            if(!isEmpty(tempPName)){
                pName = tempPName;
            }
            if(!isEmpty(tempPPrice)){
                pPrice = Double.parseDouble(tempPPrice);
            }
            if(!isEmpty(tempPQty)){
                pQty = Integer.parseInt(tempPQty);
            }

            //myDB.editItem(currProductID, pName, pPrice, pQty);

            if(!(currProductName.equals(pName))){
                items_ref.child(currProductName).removeValue();
            }

            items_ref.child(pName).setValue(new Item(pName, pPrice, pQty));
            //refreshItems();
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
        });

        //Delete button
        button_delete.setOnClickListener(view -> {

            //myDB.deleteItem(currProductID);

            items_ref.child(currProductName).removeValue();
            //refreshItems();
            popupWindow.dismiss();
            itemSearchBar.clearFocus();
        });

        //Add to cart button
        button_add_to_cart.setOnClickListener(view -> {
            int selected_product_quantity = editPopupNumberPicker.getValue();

            if (selected_product_quantity == 0) {
                Toast.makeText(MainActivity.this, "Please choose quantity", Toast.LENGTH_SHORT).show();
            } else {
                //cartedItem.add(new Item(currProductName, currProductPrice, selected_product_quantity));
                cart_ref.child(currProductName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            Item item = dataSnapshot.getValue(Item.class);
                            if (item != null) {
                                int added_qty_result = selected_product_quantity + item.getQuantity();
                                cart_ref.child(currProductName).setValue(new Item(currProductName, currProductPrice, added_qty_result));
                            } else {
                                Log.e(TAG, "add to cart is null 557");
                            }
                        } else {
                            cart_ref.child(currProductName).setValue(new Item(currProductName, currProductPrice, selected_product_quantity));
                        }
                    } else {
                        Log.e(TAG, "cart item unsuccessful");
                    }
                });
                items_ref.child(currProductName).setValue(new Item(currProductName, currProductPrice, currProductQty - selected_product_quantity));
                popupWindow.dismiss();
                itemSearchBar.clearFocus();
            }
        });
    }

    /*void clearArrays(){
        listItemName.clear();
        listItemPrice.clear();
        listItemQty.clear();
    }*/

    /*void storeDataInArrays(){
        listItemID.clear();
        listItemName.clear();
        listItemPrice.clear();
        listItemQty.clear();
        Cursor cursor = database.readAllProductData();
        if(cursor.getCount() == 0){
            //Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
        }else{
            while(cursor.moveToNext()){
                listItemID.add(cursor.getString(0));
                listItemName.add(cursor.getString(1));
                listItemPrice.add(cursor.getString(2));
                listItemQty.add(cursor.getString(3));
            }
            storeProductDataInCurrProduct();
        }
    }*/

    void refreshItems(){
        //customAdapter.notifyDataSetChanged();
        itemSearchBar.clearFocus();
    }

    @Override
    public void onItemClick(int position, String productName, String productPrice, String productQty) {
        CreateEditPopUpWindow(productName, Double.parseDouble(productPrice), Integer.parseInt(productQty));
    }

    void storeProductDataInCurrProduct(){
        currProduct.clear();
        Product temp;
        for(int i = 0; i < listItemID.size(); i++){
            temp = new Product(Integer.parseInt(listItemPrice.get(i)), listItemName.get(i), Float.parseFloat(listItemPrice.get(i)), Integer.parseInt(listItemQty.get(i)));
            currProduct.add(temp);
        }
    }

    void filterList(String newText){
        ArrayList<Product> filteredList = new ArrayList<>();
        for(Product p : currProduct){
            if(p.getProductName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(p);
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
        }else{
            customAdapter.setFilteredList(filteredList);
        }
    }

    @Override
    public void onEmployeeClick(int position, User emp_user, boolean pending_approval) {
        if (pending_approval) {
            createEmployeeApprovalPopupWindow(emp_user);
        } else {
            createEmployeePopupWindow(emp_user);
        }
    }

    private void createEmployeeApprovalPopupWindow(User emp_user) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.employee_approval_popup, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.TOP, 0, 0));

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

    private void createEmployeePopupWindow(User emp_user) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.employee_popup, null);

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        drawerLayout.post(() -> popupWindow.showAtLocation(drawerLayout, Gravity.TOP, 0, 0));

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
}