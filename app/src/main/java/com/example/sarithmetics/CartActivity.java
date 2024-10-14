package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements ListAdapterCartFirebase.OnItemClickListener {
    private static final String TAG = "firebaseDatabase CartAct";
    LinearLayout cart_activity_layout, calculation_layout, checkout_view, receipt_view;
    ArrayList<String> cartedItemName, cartedItemPrice, cartedItemQty;
    ArrayList<Item> cartedItem, items;
    //ListAdapterItem listAdapterItem;
    RecyclerView cart_rv, receipt_rv;
    ImageView back, btn_empty_cart, qr_code;
    TextView totalTextView, changeTextView, receipt_tq, receipt_subtotal, receipt_total, receipt_customer_payment, receipt_customer_change;
    Button btn_calculate, btn_checkout;
    EditText customerPayment;
    TextView cart_status, receipt_info;
    Button receipt_btn_ok;
    double price_total;

    /*database*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    User cUser;
    DatabaseReference cart_ref, items_ref;
    Query cart_query, receipt_query;
    ListAdapterCartFirebase listAdapterCartFirebase;
    ListAdapterReceiptFirebase listAdapterReceiptFirebase;

    //ads
    AdView mAdView;

    /*Loading system*/
    SystemLoading systemLoading;

    /*private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
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
            .build();*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initializeAds();

        //*database*/
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        /*Sets up internet monitoring*/
        /*ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);*/
        FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    //connected
                    //startNoConnectionActivity();
                    startActivity(new Intent(getApplicationContext(), NoConnectionActivity.class));
                    finish();
                } else {
                    // not connected
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*id hook*/
        cart_rv = findViewById(R.id.recyclerViewCart);
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
        qr_code = findViewById(R.id.receipt_qr_code);

        price_total = 0;

        /*Views*/
        checkout_view = findViewById(R.id.checkout_view);
        receipt_view = findViewById(R.id.receipt_view);

        /*Receipt view*/
        receipt_info = findViewById(R.id.receipt_info);
        receipt_tq = findViewById(R.id.receipt_tq);
        receipt_subtotal = findViewById(R.id.receipt_subtotal);
        receipt_total = findViewById(R.id.receipt_total);
        receipt_customer_payment = findViewById(R.id.receipt_customer_payment);
        receipt_customer_change = findViewById(R.id.receipt_customer_change);
        receipt_btn_ok = findViewById(R.id.receipt_btn_ok);
        receipt_rv = findViewById(R.id.receipt_rv);

        /*ArrayList hook*/
        items = new ArrayList<>();
        cartedItemName = new ArrayList<>();
        cartedItemPrice = new ArrayList<>();
        cartedItemQty = new ArrayList<>();
        cartedItem = new ArrayList<>();
        cartedItem.clear();

        /*Loading system*/
        systemLoading = new SystemLoading(CartActivity.this);
        systemLoading.startLoadingDialog();

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
            hideKeyboard(view);
            checkOut();
        });

        back.setOnClickListener(view -> finish());

        btn_empty_cart.setOnClickListener(view -> {
            emptyCart();
        });

    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            /*Dismiss loading*/
            systemLoading.dismissDialog();
        });
    }

    private void setUpCartList() {
        cart_rv.setLayoutManager(new LinearLayoutManager(CartActivity.this));
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
        cart_rv.setAdapter(listAdapterCartFirebase);
        listAdapterCartFirebase.startListening();
    }

    private void setUpReceiptList(String key) {
        receipt_rv.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        receipt_query = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).child(key).child("items");
        FirebaseRecyclerOptions<Item> options2 =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(receipt_query, Item.class)
                        .build();
        listAdapterReceiptFirebase = new ListAdapterReceiptFirebase(options2, cUser);
        receipt_rv.setAdapter(listAdapterReceiptFirebase);
        listAdapterReceiptFirebase.startListening();
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

            price_total = 0;

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
                            items_ref.child(item.getName()).setValue(new Item(item.getName(), item.getPrice(), item.getCost_price(), added_qty_result));
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

        String key;

        MyTransaction transaction;

        double customer_change;
        double customer_payment;
        double subtotal;
        int item_count;
        Object transaction_date;
        List<Item> items;

        DatabaseReference ref = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).push();
        key = ref.getKey();

        customer_change = Double.parseDouble(changeTextView.getText().toString().trim().replaceAll("[^\\d.]", ""));
        customer_payment = Double.parseDouble(customerPayment.getText().toString().trim());
        subtotal = 0.0;
        item_count = 0;
        transaction_date = ServerValue.TIMESTAMP;
        items = new ArrayList<>();

        for (int i = 0; i < listAdapterCartFirebase.getItemCount(); i++) {
            Item item = listAdapterCartFirebase.getItem(i);

            items.add(new Item(item.getName(), item.getPrice(), item.getQuantity()));

            subtotal += item.getPrice() * item.getQuantity();
            item_count += item.getQuantity();
        }

        transaction = new MyTransaction(customer_change, customer_payment, subtotal, item_count, true, transaction_date, items, cUser.getFirst_name() + " " + cUser.getLast_name());

        cart_ref.removeValue();

        ref.setValue(transaction);

        openReceipt(key);
        /*displayReceipt(key);*/
    }

    private void displayReceipt(String key) {
        /*Function Tag*/
        String FUNCTION_TAG = "displayReceipt";

        /*Switch views*/
        checkout_view.setVisibility(View.GONE);
        receipt_view.setVisibility(View.VISIBLE);

        /*Populate data*/
        DatabaseReference ref = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code());

        ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MyTransaction transaction = snapshot.getValue(MyTransaction.class);

                    Date date = new Date(((Number)transaction.getTransaction_date()).longValue());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formatted_date = sdf.format(date);

                    setUpReceiptList(key);

                    receipt_tq.setText(transaction.getItem_count() + " Item(s)");
                    receipt_subtotal.setText("₱" + transaction.getSubtotal());
                    receipt_total.setText("₱" + transaction.getSubtotal());
                    receipt_customer_payment.setText("₱" + transaction.getCustomer_payment());
                    receipt_customer_change.setText("₱" + transaction.getCustomer_change());
                    receipt_info.setText(formatted_date + " " + key);

                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    String link = "https://sarithmetics-receipt.vercel.app/" + key + "/" + cUser.getBusiness_code();

                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(link, BarcodeFormat.QR_CODE, 300, 300);

                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        qr_code.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e(FUNCTION_TAG, "snapshot doesn't exist\n" + snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*ref
                .child(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double customer_change = snapshot.child("customerChange").getValue(Double.class);
                        double customer_payment = snapshot.child("customerPayment").getValue(Double.class);
                        double subtotal = snapshot.child("subtotal").getValue(Double.class);
                        long transaction_date = snapshot.child("transactionDate").getValue(Long.class);
                        int item_count = snapshot.child("itemCount").getValue(Integer.class);

                        Date date = new Date(transaction_date);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String formatted_date = sdf.format(date);

                        setUpReceiptList(key);

                        receipt_tq.setText(item_count + "Item(s)");
                        receipt_subtotal.setText("₱" + subtotal);
                        receipt_total.setText("₱" + subtotal);
                        receipt_customer_payment.setText("₱" + customer_payment);
                        receipt_customer_change.setText("₱" + customer_change);
                        receipt_info.setText(formatted_date + " " + key);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

        receipt_btn_ok.setOnClickListener(view -> {
            finish();
        });
    }

    /*{
        key: {
            "customerChange": 44,
            "customerPayment": 200,
            "items": {
                "Bean": {
                    "costPrice": 0,
                    "name": "Bean",
                    "price": 60,
                    "quantity": 2
                },
                "Coke": {
                    "costPrice": 0,
                    "name": "Coke",
                    "price": 12,
                    "quantity": 3
                }
            },
            "subtotal": 156,
            "transactionDate": 1726745914518
        }
    }*/

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
        popupWindow.setElevation(10);
        cart_activity_layout.post(() -> popupWindow.showAtLocation(cart_activity_layout, Gravity.CENTER, 0, 0));

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

    private void openReceipt(String key) {
        Intent intent = new Intent(CartActivity.this, ReceiptActivity.class);
        intent.putExtra("key", key);
        startActivity(intent);
        finish();
    }
}