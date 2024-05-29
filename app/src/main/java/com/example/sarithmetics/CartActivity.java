package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CustomAdapter.OnItemClickListener {
    ArrayList<String> cartedItemName, cartedItemPrice, cartedItemQty;
    ArrayList<Item> cartedItem;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;
    ImageView back, emptyCart;
    TextView totalTextView, changeTextView;
    Button calculate, checkOut;
    EditText customerPayment;
    float price_total;
    /*database*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        /*database*/
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        /*id hook*/
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalTextView = findViewById(R.id.tvCartTotal);
        changeTextView = findViewById(R.id.tvCartChange);
        calculate = findViewById(R.id.btnCartCalculate);
        checkOut = findViewById(R.id.btnCartCheckOut);
        customerPayment = findViewById(R.id.etnCustomerPayment);
        back = findViewById(R.id.ivToolBarCartBack);
        emptyCart = findViewById(R.id.ivToolBarCartEmptyCart);

        price_total = 0;

        /*ArrayList hook*/
        cartedItemName = new ArrayList<>();
        cartedItemPrice = new ArrayList<>();
        cartedItemQty = new ArrayList<>();
        cartedItem = new ArrayList<>();
        cartedItem.clear();
        /*cartedProduct = new ArrayList<>();
        cartedProduct.clear();*/

        /*Listing*/
        //cartedItem = (ArrayList<Item>) getIntent().getSerializableExtra("key");
        storeDataInArrays();
        customAdapter = new CustomAdapter(CartActivity.this, cartedItemName, cartedItemPrice, cartedItemQty, this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));

        totalTextView.setText(String.valueOf(price_total));

        calculate.setOnClickListener(view -> {
            float customerPaymentFloat = 0;
            String tempPayment = customerPayment.getText().toString().trim();
            if(!isEmpty(tempPayment)){
                customerPaymentFloat = Float.parseFloat(tempPayment);
            }
            float result = customerPaymentFloat - price_total;
            if(result < 0){
                Toast.makeText(CartActivity.this,"Missing " + ( -1 * result) + " Pesos", Toast.LENGTH_SHORT).show();
            }else{
                changeTextView.setText("₱" + String.valueOf(result));
                checkOut.setVisibility(View.VISIBLE);
            }
        });

        checkOut.setOnClickListener(view -> {
            cartedItem.clear();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        back.setOnClickListener(view -> finish());

        emptyCart.setOnClickListener(view -> {
            int prodID, prodQty;
            MyDatabaseHelper myDB = new MyDatabaseHelper(CartActivity.this);
            for(Item i : cartedItem){
                prodQty = i.getQuantity();
                //Add restocking system in here
            }
            cartedItem.clear();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });
    }

    void refreshItems(){
        storeDataInArrays();
        customAdapter.notifyDataSetChanged();
    }

    void storeDataInArrays(){
        cartedItemName.clear();
        cartedItemPrice.clear();
        cartedItemQty.clear();
        for(Item i : cartedItem){
            cartedItemName.add(i.getName());
            cartedItemPrice.add(String.valueOf(i.getPrice()));
            cartedItemQty.add(String.valueOf(i.getQuantity()));
            price_total += i.getPrice() * (float) i.getQuantity();
        }
    }



    @Override
    public void onItemClick(int position, String productName, String productPrice, String productQty) {

    }
}