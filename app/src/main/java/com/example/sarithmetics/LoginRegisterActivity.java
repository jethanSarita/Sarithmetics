package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    TextView tvRegister, tvLogin;
    LinearLayout loginLayout, registerLayout;
    Button registerBtn, loginBtn;
    EditText etRegisterFirstName, etRegisterLastName, etRegisterPassword, etRegisterConfirmPassword, etLoginEmail, etLoginPassword, etEmail;
    RadioButton rbEmployee, rbBusinessOwner;

    SessionManager sessionManager;
    //firebase
    FirebaseAnalytics mFirebaseAnalytics;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        /*firebase*/
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();

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
        etEmail = findViewById(R.id.etEmail);
        loginBtn = findViewById(R.id.loginBtn);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        rbEmployee = findViewById(R.id.rbEmployee);
        rbBusinessOwner = findViewById(R.id.rbBusinessOwner);

        database = FirebaseDatabase.getInstance(DB);

        tvRegister.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tvRegister");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Register redirect button");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            openRegister(view);
        });

        tvLogin.setOnClickListener(view -> {
            openLogin(view);
        });

        registerBtn.setOnClickListener(view -> {

            String first_name = etRegisterFirstName.getText().toString();
            String last_name = etRegisterLastName.getText().toString();
            String email = etEmail.getText().toString();
            String password = etRegisterPassword.getText().toString();
            String confirm_password = etRegisterConfirmPassword.getText().toString();
            int user_type = getUserType();

            if (isEmpty(first_name) || isEmpty(last_name) || isEmpty(email) || isEmpty(password) || isEmpty(confirm_password) || user_type == -1) {
                Toast.makeText(LoginRegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (password.equals(confirm_password)) {
                //Toast.makeText(LoginRegisterActivity.this, "Account created please login", Toast.LENGTH_SHORT).show();
                createAccount(first_name, last_name, email, password, user_type);
            } else {
                Toast.makeText(LoginRegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                etRegisterPassword.setText("");
                etRegisterConfirmPassword.setText("");
            }
        });

        loginBtn.setOnClickListener(view -> {

            //MyDatabaseHelper myDB = new MyDatabaseHelper(LoginRegisterActivity.this);
            String email = etLoginEmail.getText().toString();
            String password = etLoginPassword.getText().toString();
            if (isEmpty(email) || isEmpty(password)){
                Toast.makeText(LoginRegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                signIn(email, password);
            }
        });

        if(sessionManager.getLogin()){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            sessionManager.setMainStatus(false);
            finish();
        }
    }

    private void createAccount(String first_name, String last_name, String email, String password, int user_type) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            String full_name = first_name + " " + last_name;
                            myRef = database.getReference("User");

                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(full_name)
                                    .build();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null){
                                user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated. " + user.getEmail() + " with " + user.getDisplayName());
                                            User cUser = new User();
                                            if (user_type == 0) {
                                                //Employee
                                                cUser = new User(user.getUid(), first_name, last_name, null, 0);
                                            } else if (user_type == 1) {
                                                //Business owner
                                                cUser = new User(user.getUid(), first_name, last_name, generateRandom5CharString(), 1);
                                            }
                                            myRef = database.getReference("Users");
                                            myRef.child(cUser.getUid()).setValue(cUser);
                                            updateUI(user);
                                        } else {
                                            Log.d(TAG, "User profile update error.", task.getException());
                                        }
                                    }
                                });
                            } else {
                                Log.d(TAG, "Register error, user is null");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginRegisterActivity.this, "Register failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }
    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null){
                                Log.d(TAG, "User: " + user.getDisplayName() + "\nHas been logged in");
                                updateUI(user);
                            } else {
                                Log.d(TAG, "Log in error, user null");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginRegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    private void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Email sent
                    }
                });
        // [END send_email_verification]
    }

    void openLogin(View view){
        loginLayout.setVisibility(view.VISIBLE);
        registerLayout.setVisibility(view.GONE);
    }
    void openRegister(View view){
        loginLayout.setVisibility(view.GONE);
        registerLayout.setVisibility(view.VISIBLE);
    }

    private void updateUI(FirebaseUser user) {
        sessionManager.setLogin(true);
        sessionManager.setMainStatus(false);
        sessionManager.setUser(user);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private int getUserType() {
        if (rbEmployee.isChecked()){
            return 0;
        } else if (rbBusinessOwner.isChecked()) {
            return 1;
        }
        return -1;
    }

    private String generateRandom5CharString() {
        int length = 5;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextInt(26) + 'A');
            sb.append(c);
        }
        return sb.toString();
    }
}