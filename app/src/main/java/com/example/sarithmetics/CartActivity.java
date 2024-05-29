package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CustomAdapter.OnItemClickListener {
    private static final String TAG = "firebaseDatabase CartAct";
    ArrayList<String> cartedItemName, cartedItemPrice, cartedItemQty;
    ArrayList<Item> cartedItem;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;
    ImageView back, emptyCart;
    TextView totalTextView, changeTextView;
    Button calculate, checkOut;
    EditText customerPayment;
    double price_total;
    /*database*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser;
    DatabaseReference cartRef, itemRef;

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

        /*Get Current User*/
        firebaseDatabaseHelper.getUserRef().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d("firebaseDatabase CartAct", "Got User object: " + String.valueOf(task.getResult().getValue()));
                cUser = task.getResult().getValue(User.class);
                if (cUser != null) {
                    /*Set refs*/
                    cartRef = firebaseDatabaseHelper.getCartRef(cUser.getUid());
                    itemRef = firebaseDatabaseHelper.getItemRef(cUser.getBusiness_code());
                    cartRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            clearArrays();
                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                Item item = postSnapshot.getValue(Item.class);
                                cartedItem.add(item);
                                cartedItemName.add(item.getName());
                                cartedItemPrice.add(String.valueOf(item.getPrice()));
                                cartedItemQty.add(String.valueOf(item.getQuantity()));
                                price_total += item.getPrice() * item.getQuantity();
                            }

                            if (cartedItem.isEmpty()) {
                                emptyCart.setVisibility(View.GONE);
                                findViewById(R.id.calculation_layout).setVisibility(View.INVISIBLE);
                            } else {
                                findViewById(R.id.cart_status).setVisibility(View.GONE);
                            }

                            customAdapter.notifyDataSetChanged();
                            totalTextView.setText(String.valueOf(price_total));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("item value event listener", "loadPost:onCancelled", error.toException());
                        }
                    });
                } else {
                    Log.d("firebaseDatabase CartAct", "User is null");
                }
            } else {
                Log.e("firebaseDatabase CartAct", "Error getting data", task.getException());
            }
        });

        calculate.setOnClickListener(view -> {
            double customerPaymentFloat = 0;
            String tempPayment = customerPayment.getText().toString().trim();
            if(!isEmpty(tempPayment)){
                customerPaymentFloat = Double.parseDouble(tempPayment);
            }
            double result = customerPaymentFloat - price_total;
            if(result < 0){
                Toast.makeText(CartActivity.this,"Missing " + ( -1 * result) + " Pesos", Toast.LENGTH_SHORT).show();
            }else{
                changeTextView.setText("₱" + String.valueOf(result));
                checkOut.setVisibility(View.VISIBLE);
            }
        });

        checkOut.setOnClickListener(view -> {
            clearArrays();
            cartRef.removeValue();
            finish();
        });

        back.setOnClickListener(view -> finish());

        emptyCart.setOnClickListener(view -> {
            /*int prodID, prodQty;
            MyDatabaseHelper myDB = new MyDatabaseHelper(CartActivity.this);
            for(Item i : cartedItem){
                prodQty = i.getQuantity();
                //Add restocking system in here
            }*/
            for (Item curr_item : cartedItem) {
                itemRef.child(curr_item.getName()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            Item item = dataSnapshot.getValue(Item.class);
                            if (item != null) {
                                int added_qty_result = curr_item.getQuantity() + item.getQuantity();
                                itemRef.child(curr_item.getName()).setValue(new Item(curr_item.getName(), curr_item.getPrice(), added_qty_result));
                            } else {
                                Log.e(TAG, "add to cart is null 158");
                            }
                        } else {
                            itemRef.child(curr_item.getName()).setValue(curr_item);
                        }
                        cartRef.removeValue();
                    } else {
                        Log.e(TAG, "empty cart unsuccessful");
                    }
                });
            }
            clearArrays();
            cartedItem.clear();
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
        /*for(Item i : cartedItem){
            cartedItemName.add(i.getName());
            cartedItemPrice.add(String.valueOf(i.getPrice()));
            cartedItemQty.add(String.valueOf(i.getQuantity()));
            price_total += i.getPrice() * (float) i.getQuantity();
        }*/
    }

    public void clearArrays() {
        cartedItem.clear();
        cartedItemName.clear();
        cartedItemPrice.clear();
        cartedItemQty.clear();
    }



    @Override
    public void onItemClick(int position, String productName, String productPrice, String productQty) {

    }
}