package com.example.sarithmetics;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

public class ReceiptActivity extends AppCompatActivity {

    private static final String TAG = "Receipt Activity";

    /*Components*/
    TextView receipt_info, receipt_subtotal, receipt_total, receipt_customer_payment, receipt_customer_change, receipt_tq;
    Button receipt_btn_ok;
    RecyclerView receipt_rv;
    ImageView back_btn, qr_code;

    /*Firebase*/
    FirebaseDatabaseHelper firebaseDatabaseHelper;
    DatabaseReference history_ref;
    Query receipt_query;
    ListAdapterReceiptFirebase listAdapterReceiptFirebase;

    /*Session*/
    User cUser;

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
    LoadingSystem loadingSystem;

    /*Previous Data*/
    Bundle bundle;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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

        /*Buttons*/
        receipt_btn_ok = findViewById(R.id.receipt_main_btn_ok);
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
        loadingSystem = new LoadingSystem(ReceiptActivity.this);
        loadingSystem.startLoadingDialog();

        /*Previous Data*/
        bundle = getIntent().getExtras();
        key = bundle.getString("key");

        /*Get Current User Info*/
        getCurrentUserInformation();

        /*Button Functions*/
        receipt_btn_ok.setOnClickListener(view -> {
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

                    /*Dismiss loading*/
                    loadingSystem.dismissDialog();
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

    private void populateViewData() {
        firebaseDatabaseHelper.getBusinessTransactionHistoryRef(cUser.getBusiness_code()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //qr_code

                MyTransaction transaction = snapshot.getValue(MyTransaction.class);

                receipt_tq.setText(transaction.getItem_count() + " Item(s)");
                receipt_subtotal.setText("₱" + transaction.getSubtotal());
                receipt_total.setText("₱" + transaction.getSubtotal());
                receipt_customer_payment.setText("₱" + transaction.getCustomer_payment());
                receipt_customer_change.setText("₱" + transaction.getCustomer_change());
                receipt_info.setText(transaction.getTransaction_date() + " " + key);

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
}