package com.example.sarithmetics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CustomAdapter.OnItemClickListener {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView settings, homeIcon, cart, add;
    RelativeLayout homeLayout, itemsLayout;
    MyDatabaseHelper database;
    ArrayList<String> listProductID;
    ArrayList<String> listProductName;
    ArrayList<String> listProductPrice;
    ArrayList<String> listProductQty;
    SessionManager sessionManager;
    TextView profileFnLName;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*hook*/
        sessionManager = new SessionManager(getApplicationContext());
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        settings = findViewById(R.id.settingsBtn);
        homeIcon = findViewById(R.id.homeIcon);
        homeLayout = findViewById(R.id.layoutHome);
        itemsLayout = findViewById(R.id.layoutItems);
        cart = findViewById(R.id.ivCart);
        add = findViewById(R.id.ivAddItem);
        profileFnLName = findViewById(R.id.profileFnLName);
        database = new MyDatabaseHelper(MainActivity.this);
        listProductID = new ArrayList<>();
        listProductName = new ArrayList<>();
        listProductPrice = new ArrayList<>();
        listProductQty = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        /*tool bar*/
        setSupportActionBar(toolbar);

        /*navigation drawer menu*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        /*database arraylist storing*/
        storeDataInArrays();
        customAdapter = new CustomAdapter(MainActivity.this, listProductID, listProductName, listProductPrice, listProductQty, this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        /*Welcome back, "[FirstName] [LastName]"*/
        Cursor cursor = database.getUser(sessionManager.getUsername());
        String currentUser = null;
        if(cursor.getCount() == 0){
            Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
        }else{
            cursor.moveToNext();
            currentUser = cursor.getString(1) +  " " + cursor.getString(2);
        }
        profileFnLName.setText(currentUser);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
            }
        });
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Cart Activity PENDING*/
                Toast.makeText(getApplicationContext(), "Cart", Toast.LENGTH_SHORT).show();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
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
        }else if(item.getItemId() == R.id.nav_items){
            homeLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.VISIBLE);
            /*need to add extra option her for partner's part*/
        }else if(item.getItemId() == R.id.nav_logout){
            sessionManager.setLogin(false);
            sessionManager.setUsername(null);
            startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            finish();
        }else if(item.getItemId() == R.id.nav_share){

        }else if(item.getItemId() == R.id.nav_rate){

        }else if(item.getItemId() == R.id.nav_exit){
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
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Write sql insertion code here*/

                MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
                String pName =  productName.getText().toString().trim();
                float pPrice = Float.parseFloat(productPrice.getText().toString().trim());
                int pQty = Integer.valueOf(productQuantity.getText().toString().trim());
                myDB.addItem(pName, pPrice, pQty);
                spawnItems();
                popupWindow.dismiss();
            }
        });
    }
    /*Popup when editing an item*/
    private void CreateEditPopUpWindow() {
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
        Button add, close;
        EditText productName, productPrice, productQuantity;
        productName = popupView.findViewById(R.id.productName);
        productPrice = popupView.findViewById(R.id.productPrice);
        productQuantity = popupView.findViewById(R.id.productQuantity);
        add = popupView.findViewById(R.id.btnPopupAdd);
        close = popupView.findViewById(R.id.btnPopupClose);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Write sql insertion code here*/

                MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
                String pName =  productName.getText().toString().trim();
                float pPrice = Float.parseFloat(productPrice.getText().toString().trim());
                int pQty = Integer.valueOf(productQuantity.getText().toString().trim());
                myDB.addItem(pName, pPrice, pQty);
                spawnItems();
                popupWindow.dismiss();
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
            Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
        }else{
            while(cursor.moveToNext()){
                listProductID.add(cursor.getString(0));
                listProductName.add(cursor.getString(1));
                listProductPrice.add(cursor.getString(2));
                listProductQty.add(cursor.getString(3));
            }
        }
    }

    void spawnItems(){
        storeDataInArrays();
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position, String productId, String productName, String productPrice, String productQty) {
        Toast.makeText(MainActivity.this, "Selected: " + productId + productName + productPrice + productQty, Toast.LENGTH_SHORT).show();
    }
}