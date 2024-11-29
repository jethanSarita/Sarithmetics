package com.example.sarithmetics;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String BUILD_KEY = "m£43a4QPD+Zm";

    TextView tvRegister, tvLogin;
    LinearLayout loginLayout, registerLayout;
    Button registerBtn, loginBtn;
    EditText etRegisterFirstName, etRegisterLastName, etRegisterPassword, etRegisterConfirmPassword, etLoginEmail, etLoginPassword, etEmail;
    RadioButton rbEmployee, rbBusinessOwner;
    RandomHelper randomHelper;

    SessionManager sessionManager;
    //firebase
    //FirebaseAnalytics mFirebaseAnalytics;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    /*Loading System*/
    SystemLoading systemLoading;

    //Network update reactions
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
            Log.e("MyNetTest", "LogRegActivity - OnLost");
            Intent intent = new Intent(getApplicationContext(), NoConnectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };

    //Network request
    NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        //checkInternet();

        /*firebase*/
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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

        randomHelper = new RandomHelper();

        database = FirebaseDatabase.getInstance(DB);

        /*Loading System*/
        systemLoading = new SystemLoading(LoginRegisterActivity.this);

        tvRegister.setOnClickListener(view -> {
            GeneralHelper.hideKeyboard(view);
            openRegister(view);
        });

        tvLogin.setOnClickListener(view -> {
            GeneralHelper.hideKeyboard(view);
            openLogin(view);
        });

        registerBtn.setOnClickListener(view -> {
            GeneralHelper.hideKeyboard(view);
            registerUser();
        });

        loginBtn.setOnClickListener(view -> {
            GeneralHelper.hideKeyboard(view);
            loginUser();
        });

        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);
        //connectivityManager



        if (!checkConnection()) {
            Intent intent = new Intent(getApplicationContext(), NoConnectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            systemLoading.startLoadingDialog();
            checkSession();
        }
    }

    /*private boolean checkInternet() {
        if (checkConnection()) {
            Toast.makeText(getApplicationContext(), "Connected to internet", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Not connected to internet", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), NoConnectionActivity.class));
            finish();
            return false;
        }
    }*/

    private void checkSession() {
        database.getReference("build_key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getValue(String.class) == null) {
                    return;
                }

                String current_build_key = snapshot.getValue(String.class);

                if (BUILD_KEY.equals(current_build_key)) {
                    if(sessionManager.getLogin()){
                        sessionManager.setMainStatus(0);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(getApplicationContext(), DemoEndActivity.class));
                    finish();
                }

                systemLoading.dismissDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();
        if (isEmpty(email) || isEmpty(password)){
            Toast.makeText(LoginRegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            systemLoading.startLoadingDialog();
            signIn(email, password);
        }
    }

    private void registerUser() {
        String first_name = etRegisterFirstName.getText().toString().trim();
        String last_name = etRegisterLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();
        String confirm_password = etRegisterConfirmPassword.getText().toString().trim();
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
    }

    private void createAccount(String first_name, String last_name, String email, String password, int user_type) {
        // [START create_user_with_email]
        systemLoading.startLoadingDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Account creation success
                        // Update UI with the newly created user's information

                        Log.d(TAG, "createUserWithEmail:success");

                        String full_name = first_name + " " + last_name;
                        myRef = database.getReference("User");

                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(full_name)
                                .build();

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null){
                            user.updateProfile(profileChangeRequest).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    randomHelper.generateUniqueBusinessCode(new RandomHelper.OnUniqueIdGenerated() {
                                        @Override
                                        public void onGenrated(String unique_id) {
                                            Log.d(TAG, "User profile updated. " + user.getEmail() + " with " + user.getDisplayName());

                                            User cUser = getUser(unique_id);

                                            myRef = database.getReference("Users");
                                            myRef.child(cUser.getUid()).setValue(cUser);

                                            //Check if business owner
                                            if (!(cUser.getBusiness_code().equals("null"))) {
                                                //Yes
                                                //Populate businesses ref
                                                database.getReference("businesses").child(cUser.getBusiness_code()).child("punch in code").setValue(randomHelper.generateRandom5NumberCharString());
                                                database.getReference("businesses").child(cUser.getBusiness_code()).child("subscription").child("type").setValue(0);
                                            }

                                            systemLoading.dismissDialog();
                                            updateUI(user);
                                        }

                                        @NonNull
                                        private User getUser(String unique_id) {
                                            User cUser = new User();

                                            if (user_type == 0) {
                                                //Employee
                                                cUser = new User(user.getUid(), first_name, last_name, "null", 0);
                                            } else if (user_type == 1) {
                                                //Business owner
                                                cUser = new User(user.getUid(), first_name, last_name, unique_id, 1);
                                                /*Might need to add a query to check if the business code already exists*/
                                            }
                                            return cUser;
                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });
                                } else {
                                    Log.d(TAG, "User profile update error.", task1.getException());
                                }
                            });
                        } else {
                            Log.d(TAG, "Register error, user is null");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        systemLoading.dismissDialog();
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginRegisterActivity.this, "Register failed.",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(null);
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
                                //systemLoading.dismissDialog();
                                updateUI(user);
                            } else {
                                Log.d(TAG, "Log in error, user null");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            systemLoading.dismissDialog();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginRegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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
        loginLayout.setVisibility(View.VISIBLE);
        registerLayout.setVisibility(View.GONE);
    }
    void openRegister(View view){
        loginLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.VISIBLE);
    }

    private void updateUI(FirebaseUser user) {
        sessionManager.setLogin(true);
        sessionManager.setMainStatus(0);
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

    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }

        return networkInfo.isConnectedOrConnecting();
    }
}