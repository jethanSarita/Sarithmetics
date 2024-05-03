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
import android.database.Cursor;
import android.graphics.Color;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CustomAdapter.OnItemClickListener {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView settings, homeIcon, cart_button, add_button, eye_open_button, eye_close_button;
    RelativeLayout homeLayout, itemsLayout;
    MyDatabaseHelper database;
    ArrayList<String> listProductID;
    ArrayList<String> listProductName;
    ArrayList<String> listProductPrice;
    ArrayList<String> listProductQty;
    SessionManager sessionManager;
    TextView profileFnLNameBusinessOwner, profileFnLNameEmployee, tvBusinessCode, business_code;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;
    ArrayList<Product> cartedProduct, currProduct;
    LinearLayout boxBusinessCode, llEmployeeLayoutYesSync, llEmployeeLayoutNoSync;
    androidx.appcompat.widget.SearchView searchView;

    User cUser;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*firebase*/
        firebaseDatabase = FirebaseDatabase.getInstance(DB);

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
        tvBusinessCode = findViewById(R.id.tvBusinessCode);
        recyclerView = findViewById(R.id.recyclerViewItem);

        /*employee session hooks*/
        llEmployeeLayoutYesSync = findViewById(R.id.llEmployeeLayoutYesSync);
        llEmployeeLayoutNoSync = findViewById(R.id.llEmployeeLayoutNoSync);

        /*items page hooks*/
        searchView = findViewById(R.id.itemSearchBar);
        itemsLayout = findViewById(R.id.layoutItems);
        cart_button = findViewById(R.id.ivCart);
        add_button = findViewById(R.id.ivAddItem);

        eye_open_button = findViewById(R.id.ivEyeOpenIcon);
        eye_close_button = findViewById(R.id.ivEyeCloseIcon);
        boxBusinessCode = findViewById(R.id.boxBusinessCode);
        business_code = findViewById(R.id.tvBusinessCode);

        /*array lists*/
        currProduct = new ArrayList<>();
        cartedProduct = new ArrayList<>();
        listProductID = new ArrayList<>();
        listProductName = new ArrayList<>();
        listProductPrice = new ArrayList<>();
        listProductQty = new ArrayList<>();

        cartedProduct.clear();

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

        usersRef = firebaseDatabase.getReference("Users");
        usersRef.child(user.getUid()).child("").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebaseDatabase", "Error getting data", task.getException());
            } else {
                Log.d("firebaseDatabase", "Got User object: " + String.valueOf(task.getResult().getValue()));
                cUser = task.getResult().getValue(User.class);
                switch (cUser.getUser_type()) {
                    case 0:
                        //Employee
                        homeLayout = findViewById(R.id.layoutHomeEmployee);
                        add_button.setVisibility(View.GONE);
                        if (cUser.getBusiness_code().equals("null")) {
                            llEmployeeLayoutNoSync.setVisibility(View.VISIBLE);
                        } else {
                            llEmployeeLayoutYesSync.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 1:
                        //Business Owner
                        homeLayout = findViewById(R.id.layoutHomeBusinessOwner);
                        tvBusinessCode.setText(cUser.getBusiness_code());
                        break;
                    case 2:
                        //Employee - Inventory Manager
                        homeLayout = findViewById(R.id.layoutHomeEmployee);
                        add_button.setVisibility(View.GONE);
                        llEmployeeLayoutYesSync.setVisibility(View.VISIBLE);
                        break;
                }

            }
        });

        /*tool bar*/
        setSupportActionBar(toolbar);

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });


        /*navigation drawer menu*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        /*database arraylist storing*/
        storeDataInArrays();
        customAdapter = new CustomAdapter(MainActivity.this, listProductID, listProductName, listProductPrice, listProductQty, this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

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

        profileFnLName.setText(sessionManager.getUsername());



        if(sessionManager.getMainStatus()){
            itemsLayout.setVisibility(View.VISIBLE);
            homeLayout.setVisibility(View.GONE);
            navigationView.setCheckedItem(R.id.nav_items);
        }else{
            itemsLayout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
            navigationView.setCheckedItem(R.id.nav_home);
        }

        eye_close_button.setOnClickListener(view -> {
            eye_close_button.setVisibility(View.GONE);
            eye_open_button.setVisibility(View.VISIBLE);
            boxBusinessCode.setBackgroundColor(Color.TRANSPARENT);
            business_code.setVisibility(View.VISIBLE);
        });
        eye_open_button.setOnClickListener(view -> {
            eye_open_button.setVisibility(View.GONE);
            eye_close_button.setVisibility(View.VISIBLE);
            boxBusinessCode.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
            business_code.setVisibility(View.INVISIBLE);
        });

        settings.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), sessionManager.getUsername(), Toast.LENGTH_SHORT).show();
        });
        homeIcon.setOnClickListener(view -> {

        });
        cart_button.setOnClickListener(view -> {
            /*Cart Activity PENDING*/
            if(cartedProduct.isEmpty()){
                Toast.makeText(MainActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
            }else{
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                intent.putExtra("key", cartedProduct);
                startActivity(intent);
            }
        });
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Add Item Activity PENDING*/
                CreatePopUpWindow();
            }
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
            float pPrice = 0;
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
                pPrice = Float.parseFloat(tempPPrice);
            }

            if (!isEmpty(tempPQty)) {
                pQty = Integer.parseInt(tempPQty);
            }


            myDB.addItem(pName, pPrice, pQty);
            refreshItems();
            popupWindow.dismiss();
        });
    }
    /*Popup when editing an item*/
    private void CreateEditPopUpWindow(int currProductID, String currProductName, float currProductPrice, int currProductQty) {
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
        Button edit, close, delete, cart;
        EditText productName, productPrice, productQuantity;
        productName = popupView.findViewById(R.id.productNameEdit);
        productPrice = popupView.findViewById(R.id.productPriceEdit);
        productQuantity = popupView.findViewById(R.id.productQuantityEdit);
        edit = popupView.findViewById(R.id.btnEditPopupEdit);
        close = popupView.findViewById(R.id.btnEditPopupClose);
        editPopupNumberPicker = popupView.findViewById(R.id.editPopupNumberPicker);
        delete = popupView.findViewById(R.id.btnEditPopupDelete);
        cart = popupView.findViewById(R.id.btnAddToCart);

        editPopupNumberPicker.setMinValue(0);
        editPopupNumberPicker.setMaxValue(currProductQty);

        productName.setText(currProductName);
        productPrice.setText(String.valueOf(currProductPrice));
        productQuantity.setText(String.valueOf(currProductQty));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Write sql update code here*/

                String pName = "NULL";
                float pPrice = 0;
                int pQty = 0;
                String tempPName, tempPPrice, tempPQty;
                tempPName =  productName.getText().toString().trim();
                tempPPrice = productPrice.getText().toString().trim();
                tempPQty = productQuantity.getText().toString().trim();
                if(!isEmpty(tempPName)){
                    pName = tempPName;
                }
                if(!isEmpty(tempPPrice)){
                    pPrice = Float.parseFloat(tempPPrice);
                }
                if(!isEmpty(tempPQty)){
                    pQty = Integer.parseInt(tempPQty);
                }
                myDB.editItem(currProductID, pName, pPrice, pQty);
                refreshItems();
                popupWindow.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDB.deleteItem(currProductID);
                refreshItems();
                popupWindow.dismiss();
            }
        });
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pQty = editPopupNumberPicker.getValue();
                if(pQty == 0){
                    Toast.makeText(MainActivity.this, "Please choose quantity", Toast.LENGTH_SHORT).show();
                }else{
                    Product product = new Product(currProductID, currProductName, currProductPrice, pQty);
                    cartedProduct.add(product);
                    myDB.removeStock(currProductID, pQty);
                    refreshItems();
                    popupWindow.dismiss();
                }
            }
        });
    }

    void storeDataInArrays(){
        listProductID.clear();
        listProductName.clear();
        listProductPrice.clear();
        listProductQty.clear();
        Cursor cursor = database.readAllProductData();
        if(cursor.getCount() == 0){
            //Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
        }else{
            while(cursor.moveToNext()){
                listProductID.add(cursor.getString(0));
                listProductName.add(cursor.getString(1));
                listProductPrice.add(cursor.getString(2));
                listProductQty.add(cursor.getString(3));
            }
            storeProductDataInCurrProduct();
        }
    }

    void refreshItems(){
        storeDataInArrays();
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position, String productId, String productName, String productPrice, String productQty) {
        /*Toast.makeText(MainActivity.this, "Selected: " + productId + productName + productPrice + productQty, Toast.LENGTH_SHORT).show();*/
        CreateEditPopUpWindow(Integer.parseInt(productId), productName, Float.parseFloat(productPrice), Integer.parseInt(productQty));
    }

    void storeProductDataInCurrProduct(){
        currProduct.clear();
        Product temp;
        for(int i = 0; i < listProductID.size(); i++){
            temp = new Product(Integer.parseInt(listProductPrice.get(i)), listProductName.get(i), Float.parseFloat(listProductPrice.get(i)), Integer.parseInt(listProductQty.get(i)));
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