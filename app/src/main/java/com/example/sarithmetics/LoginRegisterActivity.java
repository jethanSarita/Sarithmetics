package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginRegisterActivity extends AppCompatActivity {

    TextView tvRegister, tvLogin;
    LinearLayout loginLayout, registerLayout;
    Button registerBtn, loginBtn;
    EditText etRegisterFirstName, etRegisterLastName, etRegisterPassword, etRegisterConfirmPassword, etLoginFirstName, etLoginPassword;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);



        sessionManager = new SessionManager(getApplicationContext());

        /*hook*/
        tvRegister = findViewById(R.id.tvRegister);
        tvLogin = findViewById(R.id.tvLogin);
        registerBtn = findViewById(R.id.registerBtn);
        loginLayout = findViewById(R.id.loginLayout);
        registerLayout = findViewById(R.id.registerLayout);
        etRegisterFirstName = findViewById(R.id.etRegisterFirstName);
        etRegisterLastName = findViewById(R.id.etRegisterLastName);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        loginBtn = findViewById(R.id.loginBtn);
        etLoginFirstName = findViewById(R.id.etLoginFirstName);
        etLoginPassword = findViewById(R.id.etLoginPassword);


        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegister(view);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLogin(view);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(LoginRegisterActivity.this);
                String firstName = etRegisterFirstName.getText().toString();
                String lastName = etRegisterLastName.getText().toString();
                String password = etRegisterPassword.getText().toString();
                String confirmPassword = etRegisterConfirmPassword.getText().toString();
                if(isEmpty(firstName) || isEmpty(lastName) || isEmpty(password) || isEmpty(confirmPassword)) {
                    Toast.makeText(LoginRegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }else if(password.equals(confirmPassword)){
                    myDB.registerAccount(etRegisterFirstName.getText().toString(), etRegisterLastName.getText().toString(), password);
                    Toast.makeText(LoginRegisterActivity.this, "Account created please login", Toast.LENGTH_SHORT).show();
                    openLogin(view);
                }else{
                    Toast.makeText(LoginRegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                    etRegisterPassword.setText("");
                    etRegisterConfirmPassword.setText("");
                }
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(LoginRegisterActivity.this);
                String user = etLoginFirstName.getText().toString();
                if(myDB.loginAccount(user, etLoginPassword.getText().toString())){
                    sessionManager.setLogin(true);
                    sessionManager.setUsername(user);
                    sessionManager.setMainStatus(false);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }else{
                    Toast.makeText(LoginRegisterActivity.this, "Wrong name or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if(sessionManager.getLogin()){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            sessionManager.setMainStatus(false);
            finish();
        }
    }
    void openLogin(View view){
        loginLayout.setVisibility(view.VISIBLE);
        registerLayout.setVisibility(view.GONE);
    }
    void openRegister(View view){
        loginLayout.setVisibility(view.GONE);
        registerLayout.setVisibility(view.VISIBLE);
    }
}