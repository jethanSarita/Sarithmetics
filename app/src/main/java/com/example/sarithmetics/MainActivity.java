package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CustomAdapter.OnItemClickListener {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String TAG = "firebaseDatabase MainAct";
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView settings, homeIcon, cart_button, add_button, eye_open_button, eye_close_button;
    RelativeLayout homeLayout, itemsLayout;
    MyDatabaseHelper database;
    ArrayList<String> listItemID;
    ArrayList<String> listItemName;
    ArrayList<String> listItemPrice;
    ArrayList<String> listItemQty;
    SessionManager sessionManager;
    TextView profileFnLNameBusinessOwner, profileFnLNameEmployee, tv_business_code;
    CustomAdapter customAdapter;
    RecyclerView rvItems;
    ArrayList<Product> cartedProduct, currProduct;
    ArrayList<Item> cartedItem;
    LinearLayout boxBusinessCode, llEmployeeLayoutYesSync, llEmployeeLayoutNoSync;
    androidx.appcompat.widget.SearchView itemSearchBar;
    EditText etBusinessCode;
    Button btnEnterBusinessCode;

    //Database
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRef, itemsRef, current_user_ref, cartRef;

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

        /*home page layout*/
        profileFnLNameEmployee = findViewById(R.id.profileFnLNameEmployee);
        profileFnLNameBusinessOwner = findViewById(R.id.profileFnLNameBusinessOwner);
        tv_business_code = findViewById(R.id.tvBusinessCode);
        rvItems = findViewById(R.id.recyclerViewItem);

        /*employee session hooks*/
        llEmployeeLayoutYesSync = findViewById(R.id.llEmployeeLayoutYesSync);
        llEmployeeLayoutNoSync = findViewById(R.id.llEmployeeLayoutNoSync);
        etBusinessCode = findViewById(R.id.etBusinessCode);
        btnEnterBusinessCode = findViewById(R.id.btnEnterBusinessCode);

        /*items page hooks*/
        itemSearchBar = findViewById(R.id.itemSearchBar);
        itemsLayout = findViewById(R.id.layoutItems);
        cart_button = findViewById(R.id.ivCart);
        add_button = findViewById(R.id.ivAddItem);

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

        //Check if session exists
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

        //Get current user information
        userRef = firebaseDatabaseHelper.getUserRef();
        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
            } else {
                Log.d(TAG, "Got User object: " + String.valueOf(task.getResult().getValue()));
                cUser = task.getResult().getValue(User.class);
                //Check usertype
                //[Employee, Business Owner, Employee - Inventory Manager]
                switch (cUser.getUser_type()) {
                    case 0:
                        //Employee

                        //Set home layout to employee version
                        homeLayout = findViewById(R.id.layoutHomeEmployee);
                        //Get rid of add item button
                        add_button.setVisibility(View.GONE);
                        //Set username text view
                        profileFnLNameEmployee.setText(sessionManager.getUsername());

                        //User is sync to business?
                        if (cUser.getBusiness_code().equals("null")) {
                            //No
                            llEmployeeLayoutNoSync.setVisibility(View.VISIBLE);
                        } else {
                            //Yes
                            llEmployeeLayoutYesSync.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 1:
                        //Business Owner

                        //Set home layout to business owner version
                        homeLayout = findViewById(R.id.layoutHomeBusinessOwner);
                        //Set business code text
                        tv_business_code.setText(cUser.getBusiness_code());
                        //Set username text view
                        profileFnLNameBusinessOwner.setText(sessionManager.getUsername());
                        break;
                    case 2:
                        //Employee - Inventory Manager

                        //Set home layout to employee version
                        homeLayout = findViewById(R.id.layoutHomeEmployee);
                        //Set username text view
                        profileFnLNameEmployee.setText(sessionManager.getUsername());
                        //No need to check business sync, since employee cant exist
                        //without an already in sync of a business
                        llEmployeeLayoutYesSync.setVisibility(View.VISIBLE);
                        break;
                }
            }

            if(sessionManager.getMainStatus()){
                itemsLayout.setVisibility(View.VISIBLE);
                homeLayout.setVisibility(View.GONE);
                navigationView.setCheckedItem(R.id.nav_items);
            }else{
                itemsLayout.setVisibility(View.GONE);
                homeLayout.setVisibility(View.VISIBLE);
                navigationView.setCheckedItem(R.id.nav_home);
            }

            cartRef = firebaseDatabaseHelper.getCartRef(cUser.getUid());

            itemsRef = firebaseDatabaseHelper.getItemRef(cUser.getBusiness_code());
            itemsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    clearArrays();
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        // TODO: handle the post
                        Item item = postSnapshot.getValue(Item.class);
                        listItemName.add(item.getName());
                        listItemPrice.add(String.valueOf(item.getPrice()));
                        listItemQty.add(String.valueOf(item.getQuantity()));
                    }
                    refreshItems();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            });
            btnEnterBusinessCode.setOnClickListener(view -> {
                String code = etBusinessCode.getText().toString();
                userRef.child(cUser.getUid()).child("business_code").setValue(code);
                recreate();
            });
        });
        //set current user reference

        /*tool bar*/
        setSupportActionBar(toolbar);

        itemSearchBar.clearFocus();
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });*/


        /*navigation drawer menu*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        /*database arraylist storing*/
        //storeDataInArrays();
        customAdapter = new CustomAdapter(MainActivity.this, listItemName, listItemPrice, listItemQty, this);
        rvItems.setAdapter(customAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        /*Welcome back, "[FirstName] [LastName]"*/
        /*Cursor cursor = database.getUser(sessionManager.getUsername());
        String currentUser = null;
        if(cursor.getCount() == 0){
            Toast.makeText(MainActivity.this, "No item data", Toast.LENGTH_SHORT).show();
        }else{
            cursor.moveToNext();
            currentUser = cursor.getString(1) +  " " + cursor.getString(2);
        }
        profileFnLName.setText(currentUser);*/



        eye_close_button.setOnClickListener(view -> {
            eye_close_button.setVisibility(View.GONE);
            eye_open_button.setVisibility(View.VISIBLE);
            boxBusinessCode.setBackgroundColor(Color.TRANSPARENT);
            tv_business_code.setVisibility(View.VISIBLE);
        });
        eye_open_button.setOnClickListener(view -> {
            eye_open_button.setVisibility(View.GONE);
            eye_close_button.setVisibility(View.VISIBLE);
            boxBusinessCode.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
            tv_business_code.setVisibility(View.INVISIBLE);
        });

        settings.setOnClickListener(view -> {
            refreshItems();
            Toast.makeText(getApplicationContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
        });
        homeIcon.setOnClickListener(view -> {

        });
        cart_button.setOnClickListener(view -> {
            /*cartRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {
                        Intent intent = new Intent(MainActivity.this, CartActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "cart_button error");
                }
            });*/
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });
        add_button.setOnClickListener(view -> {
            /*Add Item Activity PENDING*/
            CreatePopUpWindow();
        });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.nav_home){
            itemsLayout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
            sessionManager.setMainStatus(false);
        }else if(item.getItemId() == R.id.nav_items){
            homeLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.VISIBLE);
            sessionManager.setMainStatus(true);
            /*need to add extra option her for partner's part*/
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

    private void CreatePopUpWindow() {
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

        close.setOnClickListener(view -> popupWindow.dismiss());

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

            itemsRef.child(pName).setValue(new Item(pName, pPrice, pQty));
            //refreshItems();
            popupWindow.dismiss();
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
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(drawerLayout, Gravity.TOP, 0, 0);
            }
        });

        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
        NumberPicker editPopupNumberPicker;
        Button button_edit, button_close, button_delete, button_add_to_cart;
        EditText productName, productPrice, productQuantity;
        productName = popupView.findViewById(R.id.productNameEdit);
        productPrice = popupView.findViewById(R.id.productPriceEdit);
        productQuantity = popupView.findViewById(R.id.productQuantityEdit);
        button_edit = popupView.findViewById(R.id.btnEditPopupEdit);
        button_close = popupView.findViewById(R.id.btnEditPopupClose);
        editPopupNumberPicker = popupView.findViewById(R.id.editPopupNumberPicker);
        button_delete = popupView.findViewById(R.id.btnEditPopupDelete);
        button_add_to_cart = popupView.findViewById(R.id.btnAddToCart);

        editPopupNumberPicker.setMinValue(0);
        editPopupNumberPicker.setMaxValue(currProductQty);

        productName.setText(currProductName);
        productPrice.setText(String.valueOf(currProductPrice));
        productQuantity.setText(String.valueOf(currProductQty));

        button_close.setOnClickListener(view -> popupWindow.dismiss());

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
                itemsRef.child(currProductName).removeValue();
            }

            itemsRef.child(pName).setValue(new Item(pName, pPrice, pQty));
            //refreshItems();
            popupWindow.dismiss();
        });
        button_delete.setOnClickListener(view -> {

            //myDB.deleteItem(currProductID);

            itemsRef.child(currProductName).removeValue();
            //refreshItems();
            popupWindow.dismiss();
        });
        button_add_to_cart.setOnClickListener(view -> {
            int selected_product_quantity = editPopupNumberPicker.getValue();

            if (selected_product_quantity == 0) {
                Toast.makeText(MainActivity.this, "Please choose quantity", Toast.LENGTH_SHORT).show();
            } else {
                //cartedItem.add(new Item(currProductName, currProductPrice, selected_product_quantity));
                cartRef.child(currProductName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            Item item = dataSnapshot.getValue(Item.class);
                            if (item != null) {
                                int added_qty_result = selected_product_quantity + item.getQuantity();
                                cartRef.child(currProductName).setValue(new Item(currProductName, currProductPrice, added_qty_result));
                            } else {
                                Log.e(TAG, "add to cart is null 557");
                            }
                        } else {
                            cartRef.child(currProductName).setValue(new Item(currProductName, currProductPrice, selected_product_quantity));
                        }
                    } else {
                        Log.e(TAG, "cart item unsuccessful");
                    }
                });
                itemsRef.child(currProductName).setValue(new Item(currProductName, currProductPrice, currProductQty - selected_product_quantity));
                popupWindow.dismiss();
            }
        });
    }

    void clearArrays(){
        listItemName.clear();
        listItemPrice.clear();
        listItemQty.clear();
    }

    void storeDataInArrays(){
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
    }

    void refreshItems(){
        customAdapter.notifyDataSetChanged();
        itemSearchBar.clearFocus();
    }

    @Override
    public void onItemClick(int position, String productName, String productPrice, String productQty) {
        /*Toast.makeText(MainActivity.this, "Selected: " + productId + productName + productPrice + productQty, Toast.LENGTH_SHORT).show();*/
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
}