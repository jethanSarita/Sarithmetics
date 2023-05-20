package com.example.sarithmetics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginRegisterActivity extends AppCompatActivity {

    TextView tvRegister, tvLogin;
    LinearLayout loginLayout, registerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        /*hook*/
        tvRegister = findViewById(R.id.tvRegister);
        tvLogin = findViewById(R.id.tvLogin);

        loginLayout = findViewById(R.id.loginLayout);
        registerLayout = findViewById(R.id.registerLayout);

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginLayout.setVisibility(view.GONE);
                registerLayout.setVisibility(view.VISIBLE);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginLayout.setVisibility(view.VISIBLE);
                registerLayout.setVisibility(view.GONE);
            }
        });

    }
}