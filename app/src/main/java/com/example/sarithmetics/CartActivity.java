package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CustomAdapter.OnItemClickListener {
    private static final String TAG = "firebaseDatabase CartAct";
    LinearLayout cart_activity_layout, calculation_layout;
    ArrayList<String> cartedItemName, cartedItemPrice, cartedItemQty;
    ArrayList<Item> cartedItem, items;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;
    ImageView back, emptyCart;
    TextView totalTextView, changeTextView;
    Button calculate, checkOut;
    EditText customerPayment;
    TextView cart_status;
    double price_total;

    /*database*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser;
    DatabaseReference cartRef, itemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //You're probably wondering why this one is using customerAdpater rather than MainAdapter.
        //Mainly because I've made too much commitments to the code so the changes would be too
        //difficult for now. It's not broken, but it can be improved SOME OTHER TIME..

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //*database*/
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
        cart_activity_layout = findViewById(R.id.cart_activity_layout);
        calculation_layout = findViewById(R.id.calculation_layout);
        cart_status = findViewById(R.id.cart_status);

        price_total = 0;

        /*ArrayList hook*/
        items = new ArrayList<>();
        cartedItemName = new ArrayList<>();
        cartedItemPrice = new ArrayList<>();
        cartedItemQty = new ArrayList<>();
        cartedItem = new ArrayList<>();
        cartedItem.clear();

        /*Listing*/
        customAdapter = new CustomAdapter(CartActivity.this, cartedItemName, cartedItemPrice, cartedItemQty, this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));

        cart_status.setText("Cart loading...");

        /*Get Current User*/
        firebaseDatabaseHelper.getCurrentUserRef().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "Got User object: " + task.getResult().getValue());
                cUser = task.getResult().getValue(User.class);
                if (cUser != null) {
                    /*Set refs*/
                    cartRef = firebaseDatabaseHelper.getCartRef(cUser.getUid());
                    itemsRef = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code());
                    cartRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            clearArrays();
                            price_total = 0;
                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                Item item = postSnapshot.getValue(Item.class);
                                cartedItem.add(item);
                                cartedItemName.add(item.getName());
                                cartedItemPrice.add(String.valueOf(item.getPrice()));
                                cartedItemQty.add(String.valueOf(item.getQuantity()));
                                price_total += item.getPrice() * item.getQuantity();
                            }

                            if (cartedItem.isEmpty()) {
                                //cart is empty
                                cart_status.setText("Cart is empty");
                                emptyCart.setVisibility(View.GONE);
                                calculation_layout.setVisibility(View.INVISIBLE);
                                cart_status.setVisibility(View.VISIBLE);
                            } else {
                                //cart is not empty
                                emptyCart.setVisibility(View.VISIBLE);
                                calculation_layout.setVisibility(View.VISIBLE);
                                cart_status.setVisibility(View.GONE);
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
                    Log.d(TAG, "User is null");
                }
            } else {
                Log.e(TAG, "Error getting data", task.getException());
            }
        });

        calculate.setOnClickListener(view -> {
            double customerPaymentFloat = 0;
            changeTextView.setText("");
            checkOut.setVisibility(View.GONE);
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
            for (Item curr_item : cartedItem) {
                itemsRef.child(curr_item.getName()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            Item item = dataSnapshot.getValue(Item.class);
                            if (item != null) {
                                int added_qty_result = curr_item.getQuantity() + item.getQuantity();
                                itemsRef.child(curr_item.getName()).setValue(new Item(curr_item.getName(), curr_item.getPrice(), added_qty_result));
                            } else {
                                Log.e(TAG, "add to cart is null 158");
                            }
                        } else {
                            itemsRef.child(curr_item.getName()).setValue(curr_item);
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

    public void clearArrays() {
        cartedItem.clear();
        cartedItemName.clear();
        cartedItemPrice.clear();
        cartedItemQty.clear();
    }



    @Override
    public void onItemClick(int position, String productName, String productPrice, String productQty) {
        createCartPopupWindow(productName, Double.parseDouble(productPrice), Integer.parseInt(productQty));
    }

    private void createCartPopupWindow(String item_name, Double item_price, int item_quantity) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.cart_popup, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        cart_activity_layout.post(() -> popupWindow.showAtLocation(cart_activity_layout, Gravity.TOP, 0, 0));

        TextView tv_item_name, tv_item_price;
        NumberPicker np_item_quantity;
        Button btn_update, btn_close, btn_delete;
        final Item[] current_item = new Item[1];

        tv_item_name = popupView.findViewById(R.id.cart_popup_item_name_tv);
        tv_item_price = popupView.findViewById(R.id.cart_popup_item_price_tv);
        np_item_quantity = popupView.findViewById(R.id.cart_popup_number_picker);
        btn_update = popupView.findViewById(R.id.cart_popup_btn_update);
        btn_close = popupView.findViewById(R.id.cart_popup_btn_close);
        btn_delete = popupView.findViewById(R.id.cart_popup_btn_delete);

        tv_item_name.setText(item_name);
        tv_item_price.setText(String.valueOf(item_price));

        itemsRef.child(item_name).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    current_item[0] = snapshot.getValue(Item.class);
                    np_item_quantity.setMinValue(0);
                    np_item_quantity.setMaxValue(current_item[0].getQuantity() + item_quantity);
                    np_item_quantity.setValue(item_quantity);
                }
            }
        });

        btn_update.setOnClickListener(view -> {
            int new_qty_value = np_item_quantity.getValue();
            if (new_qty_value != item_quantity) {
                if (new_qty_value == 0) {
                    itemsRef.child(item_name).child("quantity").setValue(current_item[0].getQuantity() + item_quantity);
                    cartRef.child(item_name).removeValue();
                } else {
                    itemsRef.child(item_name).child("quantity").setValue((current_item[0].getQuantity() + item_quantity) - new_qty_value);
                    cartRef.child(item_name).child("quantity").setValue(new_qty_value);
                }
                popupWindow.dismiss();
            }
        });

        btn_delete.setOnClickListener(view -> {
            cartRef.child(item_name).removeValue();
            itemsRef.child(item_name).child("quantity").setValue(current_item[0].getQuantity() + item_quantity);
            popupWindow.dismiss();
        });

        btn_close.setOnClickListener(view -> popupWindow.dismiss());
    }
}