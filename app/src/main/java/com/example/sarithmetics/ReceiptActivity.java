package com.example.sarithmetics;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReceiptActivity extends AppCompatActivity {

    private static final String TAG = "Receipt Activity";

    /*Components*/
    TextView receipt_info, receipt_subtotal, receipt_total, receipt_customer_payment, receipt_customer_change, receipt_tq, receipt_title;
    Button receipt_btn_ok, receipt_btn_delete;
    RecyclerView receipt_rv;
    ImageView back_btn, qr_code;
    LinearLayout receipt_cash_change_layout;
    FrameLayout receipt_layout, gray_overlay;

    /*Firebase*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    DatabaseReference history_ref;
    Query receipt_query;
    ListAdapterReceiptFirebase listAdapterReceiptFirebase;
    int subscription_type;

    /*Session*/
    User cUser;

    /*Logic*/
    boolean currently_maxed = false;

    /*Network System*/
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

    /*Loading System*/
    SystemLoading systemLoading;

    /*Previous Data*/
    Bundle bundle;
    String key;

    /*Back*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.receipt_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*Text Views*/
        receipt_info = findViewById(R.id.receipt_main_info);
        receipt_subtotal = findViewById(R.id.receipt_main_subtotal);
        receipt_total = findViewById(R.id.receipt_main_total);
        receipt_customer_payment = findViewById(R.id.receipt_main_customer_payment);
        receipt_customer_change = findViewById(R.id.receipt_main_customer_change);
        receipt_tq = findViewById(R.id.receipt_tq);
        receipt_title = findViewById(R.id.receipt_main_title);

        /*Layout Views*/
        receipt_cash_change_layout = findViewById(R.id.receipt_main_cash_change_view);
        /*Main View*/
        receipt_layout = findViewById(R.id.receipt_layout);
        gray_overlay = findViewById(R.id.gray_overlay);

        /*Buttons*/
        receipt_btn_ok = findViewById(R.id.receipt_main_btn_ok);
        receipt_btn_delete = findViewById(R.id.receipt_main_btn_delete);
        back_btn = findViewById(R.id.toolbar_back);

        /*Qr code*/
        qr_code = findViewById(R.id.receipt_main_qr_code);

        /*List*/
        receipt_rv = findViewById(R.id.receipt_main_rv);

        /*Firebase*/
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        /*Network System*/
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);

        /*Loading system*/
        systemLoading = new SystemLoading(ReceiptActivity.this);
        systemLoading.startLoadingDialog();

        /*Previous Data*/
        bundle = getIntent().getExtras();
        key = bundle.getString("key");

        /*Back*/
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
                finish();
            }
        });

        /*Get Current User Info*/
        getCurrentUserInformation();

        /*Button Functions*/
        receipt_btn_ok.setOnClickListener(view -> {
            finish();
        });

        receipt_btn_delete.setOnClickListener(view -> {
            firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).child(key).
                    removeValue();
            finish();
        });

        back_btn.setOnClickListener(view -> {
            finish();
        });
    }

    private void getCurrentUserInformation() {
        firebaseDatabaseHelper.getCurrentUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    cUser = snapshot.getValue(User.class);
                    history_ref = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code());

                    setUpReceiptList();

                    populateViewData();

                    getSubscriptionType();

                    /*Dismiss loading*/
                    systemLoading.dismissDialog();
                } else {
                    Log.e(TAG, "Snapshot doesn't exist: " + snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Data cancelled " + error);
            }
        });

    }

    private void getSubscriptionType() {
        String FUNCTION_TAG = "getSubscriptionType";
        firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code()).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e(FUNCTION_TAG, "Snapshot doesn't exist");
                }

                subscription_type = snapshot.getValue(Integer.class);

                checkLimit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkLimit() {
        String FUNC_TAG = "checkLimit";

        long limit = Subscription.getLimit(subscription_type, Subscription.TRANSACTION);

        firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e(FUNC_TAG, "Snapshot doesn't exits");
                    return;
                }

                long node_count = snapshot.getChildrenCount();

                if (node_count > limit) {
                    currently_maxed = true;
                    transactionMaxOpenPopup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void transactionMaxOpenPopup() {
        String FUNC_TAG = "transactionMaxOpenPopup";

        gray_overlay.setVisibility(View.VISIBLE);
        gray_overlay.setOnTouchListener((v, event) -> true);


        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_transaction_max, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = false;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setElevation(10);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        receipt_layout.post(() -> popupWindow.showAtLocation(receipt_layout, Gravity.CENTER, 0, 0));

        Button delete, upgrade;

        delete = popupView.findViewById(R.id.transaction_max_delete_btn);
        upgrade = popupView.findViewById(R.id.transaction_max_upgrade_btn);

        delete.setOnClickListener(view -> {
            gray_overlay.setVisibility(View.GONE);
            deleteOldestTransaction();
            popupWindow.dismiss();
        });

        upgrade.setOnClickListener(view -> {
            Request request = Subscription.createCheckOutRequest();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(FUNC_TAG, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = "Empty";

                    if (response.body() != null) {
                        result = response.body().string();
                    }
                    Log.i(FUNC_TAG, result);
                    String id = Subscription.parseCheckOutID(result);
                    String url = Subscription.parseCheckOutUrl(result);
                    Uri uri = Uri.parse(url);
                    Log.i(FUNC_TAG, "LINK: " + url);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));

                    firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code()).child("current_checkout_id").setValue(id);
                    firebaseDatabaseHelper.getSubscriptionRef(cUser.getBusiness_code()).child("checkout_expiration").setValue(Subscription.getUnixOneMinuteExpiry());
                }
            });


        });
    }

    private void deleteOldestTransaction() {
        String FUNC_TAG = "deleteOldestTransaction";

        if (!currently_maxed) {
            return;
        }

        history_ref.orderByChild("transaction_date").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e(FUNC_TAG, "Snapshot doesn't exist");
                }

                for (DataSnapshot curr_snap : snapshot.getChildren()) {
                    String transaction_key = curr_snap.getKey();
                    Long transaction_date = curr_snap.child("transaction_date").getValue(Long.class);

                    history_ref.child(transaction_key).removeValue();

                    Toast.makeText(ReceiptActivity.this, "Deleted oldest transaction to make room for new one", Toast.LENGTH_SHORT).show();

                    Log.e(FUNC_TAG, transaction_key + " = " + key);

                    if (transaction_key.equals(key)) {
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        finish();
    }

    private void handleBack() {
        deleteOldestTransaction();
        Toast.makeText(ReceiptActivity.this, "Deleted oldest transaction to make room for new one", Toast.LENGTH_SHORT).show();
    }

    private void populateViewData() {
        firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyTransaction transaction = snapshot.getValue(MyTransaction.class);

                Date date = new Date(((Number)transaction.getTransaction_date()).longValue());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formatted_date = sdf.format(date);

                receipt_tq.setText(transaction.getItem_count() + " Item(s)");
                receipt_subtotal.setText("₱" + transaction.getSubtotal());
                receipt_total.setText("₱" + transaction.getSubtotal());
                receipt_customer_payment.setText("₱" + transaction.getCustomer_payment());
                receipt_customer_change.setText("₱" + transaction.getCustomer_change());
                receipt_info.setText(key + "\n" + formatted_date + "\n" + transaction.getEmployee_name());

                if (!transaction.isIs_out()) {
                    receipt_title.setText("Restock Receipt");
                    receipt_cash_change_layout.setVisibility(View.GONE);
                } else {
                    receipt_title.setText("Checkout Receipt");
                }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUpReceiptList() {
        receipt_rv.setLayoutManager(new LinearLayoutManager(ReceiptActivity.this));
        receipt_query = firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).child(key).child("items");
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(receipt_query, Item.class)
                        .build();
        listAdapterReceiptFirebase = new ListAdapterReceiptFirebase(options, cUser);
        receipt_rv.setAdapter(listAdapterReceiptFirebase);
        listAdapterReceiptFirebase.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        deleteOldestTransaction();
    }
}