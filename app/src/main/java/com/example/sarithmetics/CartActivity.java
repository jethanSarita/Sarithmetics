package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CartActivity extends AppCompatActivity implements ListAdapterCartFirebase.OnItemClickListener {
    private static final String TAG = "firebaseDatabase CartAct";
    LinearLayout cart_activity_layout, calculation_layout;
    ArrayList<String> cartedItemName, cartedItemPrice, cartedItemQty;
    ArrayList<Item> cartedItem, items;
    ListAdapterItem listAdapterItem;
    RecyclerView recyclerView;
    ImageView back, btn_empty_cart;
    TextView totalTextView, changeTextView;
    Button btn_calculate, btn_checkout;
    EditText customerPayment;
    TextView cart_status;
    double price_total;

    /*database*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser;
    DatabaseReference cart_ref, items_ref;
    Query cart_query;
    ListAdapterCartFirebase listAdapterCartFirebase;

    //ads
    AdView mAdView;

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            //Toast.makeText(getApplicationContext(), "Connected to the internet", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            //Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
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

        //You're probably wondering why this one is using customerAdapter rather than MainAdapter.
        //Mainly because I've made too much commitments to the code so the changes would be too
        //difficult for now. It's not broken, but it can be improved SOME OTHER TIME..

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initializeAds();

        //*database*/
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        /*Sets up internet monitoring*/
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);

        /*id hook*/
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalTextView = findViewById(R.id.tvCartTotal);
        changeTextView = findViewById(R.id.tvCartChange);
        btn_calculate = findViewById(R.id.btnCartCalculate);
        btn_checkout = findViewById(R.id.btnCartCheckOut);
        customerPayment = findViewById(R.id.etnCustomerPayment);
        back = findViewById(R.id.ivToolBarCartBack);
        btn_empty_cart = findViewById(R.id.ivToolBarCartEmptyCart);
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
        /*listAdapterItem = new ListAdapterItem(CartActivity.this, cartedItemName, cartedItemPrice, cartedItemQty, this);
        recyclerView.setAdapter(listAdapterItem);*/
        /*recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));*/

        //cart_status.setText("Cart loading...");

        /*Get Current User*/
        getCurrentUserInformation();

        btn_calculate.setOnClickListener(view -> {
            calculate();
        });

        btn_checkout.setOnClickListener(view -> {
            checkOut();
        });

        back.setOnClickListener(view -> finish());

        btn_empty_cart.setOnClickListener(view -> {
            emptyCart();
        });

    }

    private void getCurrentUserInformation() {
        firebaseDatabaseHelper.getCurrentUserRef().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "Got User object: " + task.getResult().getValue());
                cUser = task.getResult().getValue(User.class);
                if (cUser != null) {
                    /*Set refs*/
                    cart_ref = firebaseDatabaseHelper.getCartRef(cUser.getUid());
                    items_ref = firebaseDatabaseHelper.getItemsRef(cUser.getBusiness_code());

                    setUpCartList();
                } else {
                    Log.d(TAG, "User is null");
                }
            } else {
                Log.e(TAG, "Error getting data", task.getException());
            }
        });
    }

    private void setUpCartList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        cart_query = firebaseDatabaseHelper.getCartRef(cUser.getUid());
        FirebaseRecyclerOptions<Item> options1 =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(cart_query, Item.class)
                        .build();
        listAdapterCartFirebase = new ListAdapterCartFirebase(options1, this, cUser) {
            @Override
             public void onDataChanged() {
                super.onDataChanged();
                updateView();
            }
        };
        recyclerView.setAdapter(listAdapterCartFirebase);
        listAdapterCartFirebase.startListening();
    }

    private void updateView() {
        if (listAdapterCartFirebase.getItemCount() == 0) {
            //cart is empty
            cart_status.setText("Cart is empty");
            btn_empty_cart.setVisibility(View.GONE);
            calculation_layout.setVisibility(View.INVISIBLE);
            cart_status.setVisibility(View.VISIBLE);
        } else {
            //cart is not empty
            btn_empty_cart.setVisibility(View.VISIBLE);
            calculation_layout.setVisibility(View.VISIBLE);
            cart_status.setVisibility(View.GONE);

            for (int i = 0; i < listAdapterCartFirebase.getItemCount(); i++) {
                Item item = listAdapterCartFirebase.getItem(i);

                price_total += item.getPrice() * item.getQuantity();
            }

            totalTextView.setText(String.valueOf(price_total));
        }
    }

    private void initializeAds() {
        MobileAds.initialize(this, initializationStatus -> {

        });

        mAdView = findViewById(R.id.adViewItems);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void emptyCart() {

        for (int i = 0; i < listAdapterCartFirebase.getItemCount(); i++) {
            Item item = listAdapterCartFirebase.getItem(i);

            items_ref.child(item.getName()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        Item snapshot_item = dataSnapshot.getValue(Item.class);
                        if (item != null) {
                            int added_qty_result = item.getQuantity() + snapshot_item.getQuantity();
                            items_ref.child(item.getName()).setValue(new Item(item.getName(), item.getPrice(), item.getCostPrice(), added_qty_result));
                        }
                    }
                    cart_ref.removeValue();
                    finish();
                }
            });
        }

        /*for (Item curr_item : cartedItem) {
            items_ref.child(curr_item.getName()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        Item item = dataSnapshot.getValue(Item.class);
                        if (item != null) {
                            int added_qty_result = curr_item.getQuantity() + item.getQuantity();
                            items_ref.child(curr_item.getName()).setValue(new Item(curr_item.getName(), curr_item.getPrice(), added_qty_result));
                        } else {
                            Log.e(TAG, "add to cart is null 158");
                        }
                    } else {
                        items_ref.child(curr_item.getName()).setValue(curr_item);
                    }
                    cart_ref.removeValue();
                } else {
                    Log.e(TAG, "empty cart unsuccessful");
                }
            });
        }
        clearArrays();
        cartedItem.clear();*/
    }

    private void checkOut() {
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");

        for (int i = 0; i < listAdapterCartFirebase.getItemCount(); i++) {
            Item item = listAdapterCartFirebase.getItem(i);

            firebaseDatabaseHelper
                    .getBusinessTransactionHistoryRef(cUser.getBusiness_code())
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)))
                    .child((Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "-" + firebaseDatabaseHelper.getDayOfWeek(0))
                    .child(time_format.format(Calendar.getInstance().getTime()))
                    .child(item.getName()).setValue(item).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseDatabaseHelper
                                    .getBusinessTransactionHistoryRef(cUser.getBusiness_code())
                                    .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                                    .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                                    .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)))
                                    .child((Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "-" + firebaseDatabaseHelper.getDayOfWeek(0))
                                    .child(time_format.format(Calendar.getInstance().getTime()))
                                    .child(item.getName()).child("transactionDate").setValue(ServerValue.TIMESTAMP);
                        }
                    });
            //revamp database structure for transaction history
        }

        /*for (Item item: cartedItem) {
            firebaseDatabaseHelper
                    .getBusinessTransactionHistoryRef(cUser.getBusiness_code())
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)))
                    .child((Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "-" + firebaseDatabaseHelper.getDayOfWeek(0))
                    .child(time_format.format(Calendar.getInstance().getTime()))
                    .child(item.getName()).setValue(item);

            firebaseDatabaseHelper
                    .getBusinessTransactionHistoryRef(cUser.getBusiness_code())
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1))
                    .child(Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)))
                    .child((Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "-" + firebaseDatabaseHelper.getDayOfWeek(0))
                    .child(time_format.format(Calendar.getInstance().getTime()))
                    .child(item.getName()).child("transactionDate").setValue(ServerValue.TIMESTAMP);
        }*/
        /*clearArrays();*/

        cart_ref.removeValue();
        finish();
    }

    private void calculate() {
        double customer_payment = 0;
        changeTextView.setText("");
        btn_checkout.setVisibility(View.GONE);
        String tempPayment = customerPayment.getText().toString().trim();
        if(!isEmpty(tempPayment)){
            customer_payment = Double.parseDouble(tempPayment);
        }
        double result = customer_payment - price_total;
        if(result < 0){
            Toast.makeText(CartActivity.this,"Missing " + ( -1 * result) + " Pesos", Toast.LENGTH_SHORT).show();
        }else{
            changeTextView.setText("₱" + String.valueOf(result));
            btn_checkout.setVisibility(View.VISIBLE);
        }
    }

    public void clearArrays() {
        cartedItem.clear();
        cartedItemName.clear();
        cartedItemPrice.clear();
        cartedItemQty.clear();
    }

    /*@Override
    public void onItemClick(int position, String productName, String productPrice, String productQty) {
        createCartPopupWindow(productName, Double.parseDouble(productPrice), Integer.parseInt(productQty));
    }*/

    @Override
    public void onItemClick(int position, Item item) {
        createCartPopupWindow(item);
    }

    private void createCartPopupWindow(Item item) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_cart, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        cart_activity_layout.post(() -> popupWindow.showAtLocation(cart_activity_layout, Gravity.TOP, 0, 0));

        String item_name;
        double item_price;
        int item_quantity;

        item_name = item.getName();
        item_price = item.getPrice();
        item_quantity = item.getQuantity();

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

        items_ref.child(item_name).get().addOnCompleteListener(task -> {
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
                    items_ref.child(item_name).child("quantity").setValue(current_item[0].getQuantity() + item_quantity);
                    cart_ref.child(item_name).removeValue();
                } else {
                    items_ref.child(item_name).child("quantity").setValue((current_item[0].getQuantity() + item_quantity) - new_qty_value);
                    cart_ref.child(item_name).child("quantity").setValue(new_qty_value);
                }
                popupWindow.dismiss();
            }
        });

        btn_delete.setOnClickListener(view -> {
            cart_ref.child(item_name).removeValue();
            items_ref.child(item_name).child("quantity").setValue(current_item[0].getQuantity() + item_quantity);
            popupWindow.dismiss();
        });

        btn_close.setOnClickListener(view -> popupWindow.dismiss());
    }
}