package com.example.sarithmetics;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class NoConnectionActivity extends AppCompatActivity {

    Button btn_check_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_no_connection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_check_connection = findViewById(R.id.btn_check_connection);

        btn_check_connection.setOnClickListener(view -> {
            if (checkConnection()) {
                //Toast.makeText(getApplicationContext(), "Connected to internet", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Not connected to internet", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnectedOrConnecting();
    }
}