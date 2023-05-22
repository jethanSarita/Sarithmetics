package com.example.sarithmetics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    ArrayList<String> cartedProductID, cartedProductName, cartedProductPrice, cartedProductQty;
    ArrayList<Product> cartedProduct;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;
    ImageView back, emptyCart;
    TextView totalTextView, changeTextView;
    Button calculate, checkOut;
    EditText customerPayment;
    float total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        /*id hook*/
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalTextView = findViewById(R.id.tvCartTotal);
        changeTextView = findViewById(R.id.tvCartChange);
        calculate = findViewById(R.id.btnCartCalculate);
        checkOut = findViewById(R.id.btnCartCheckOut);
        customerPayment = findViewById(R.id.etnCustomerPayment);
        /*Pseudo buttons*/
        back = findViewById(R.id.ivToolBarCartBack);
        emptyCart = findViewById(R.id.ivToolBarCartEmptyCart);

        total = 0;

        /*ArrayList hook*/
        cartedProductID = new ArrayList<>();
        cartedProductName = new ArrayList<>();
        cartedProductPrice = new ArrayList<>();
        cartedProductQty = new ArrayList<>();
        cartedProduct = new ArrayList<>();
        cartedProduct.clear();

        /*Listing*/
        cartedProduct = (ArrayList<Product>) getIntent().getSerializableExtra("key");
        storeDataInArrays();
        customAdapter = new CustomAdapter(CartActivity.this, cartedProductID, cartedProductName, cartedProductPrice, cartedProductQty);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));

        totalTextView.setText(String.valueOf(total));

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float customerPaymentFloat = Float.parseFloat(customerPayment.getText().toString().trim());
                float result = customerPaymentFloat - total;
                if(result < 0){
                    Toast.makeText(CartActivity.this,"Missing " + ( -1 * result) + " Pesos", Toast.LENGTH_SHORT).show();
                }else{
                    changeTextView.setText(String.valueOf(result));
                    checkOut.setVisibility(View.VISIBLE);
                }
            }
        });

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartedProduct.clear();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        emptyCart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int prodID, prodQty;
                MyDatabaseHelper myDB = new MyDatabaseHelper(CartActivity.this);
                for(Product p : cartedProduct){
                    prodID = p.getProductID();
                    prodQty = p.getProductQty();
                    myDB.addStock(prodID, prodQty);
                }
                cartedProduct.clear();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    void refreshItems(){
        storeDataInArrays();
        customAdapter.notifyDataSetChanged();
    }
    void storeDataInArrays(){
        cartedProductID.clear();
        cartedProductName.clear();
        cartedProductPrice.clear();
        cartedProductQty.clear();
        for(Product p : cartedProduct){
            cartedProductID.add(String.valueOf(p.getProductID()));
            cartedProductName.add(p.getProductName());
            cartedProductPrice.add(String.valueOf(p.getProductPrice()));
            cartedProductQty.add(String.valueOf(p.getProductQty()));
            total += p.getProductPrice() * (float) p.getProductQty();
        }
    }
}