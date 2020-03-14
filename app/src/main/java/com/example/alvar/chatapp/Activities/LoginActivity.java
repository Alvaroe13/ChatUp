package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //UI elements
    private TextInputLayout usernameLogin, passwordLogin;
    private ProgressBar loginProgressBar;
    private CoordinatorLayout coordinatorLayout;
    private TextView txtCreateAccount, forgotPasswordTxt;
    //vars
    private Button btnLogin;
    private String email, password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFirebase();
        BindUI();
        goToRegister();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = usernameLogin.getEditText().getText().toString().trim();
                password = passwordLogin.getEditText().getText().toString().trim();

                if (email.equals("") ||  password.equals("")){
                    Log.i(TAG, "btnLogin clicked, some field is empty");
                    //Show info using snack bar
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.noEmptyField));
                } else {
                    Log.i(TAG, "btnLogin clicked no filed empty, proceed to call signIn method");
                    ProgressBarHelper.showProgressBar(loginProgressBar);
                    sigIn(email, password); //random comment to test
                }



            }
        });

        forgotPasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
                startActivity(i);
            }
        });


    }

    /**
     * Init UI elements
     */
    private void BindUI(){
        usernameLogin = findViewById(R.id.loginName);
        passwordLogin = findViewById(R.id.loginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);
        forgotPasswordTxt = findViewById(R.id.forgotPasswordText);
    }

    /**
     * init firebase services
     */
    private void initFirebase(){
        //firebase service init
        mAuth = FirebaseAuth.getInstance();
        //firebase db init
        database = FirebaseDatabase.getInstance();
        //get access to "Users" branch of db
        dbUsersNodeRef = database.getReference().child("Users");

    }

    /**
     * check status of user as soon as the app is launched since this activity is launched firstly
     * by default (look at the androidManifest tree)
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //if user is signed in we take the user to the main activity
        if (currentUser != null){
                 goToMain();
                 Log.i(TAG, "onStart: onStart method called");
        }

    }

    /**
     * this one's in charge of signing into firebase db.
     * @param email
     * @param password
     */
    private void sigIn(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithEmail:success" );
                            getDeviceToken();


                        } else {
                            // If sign in fails, display a message to the user.

                            String error = task.getException().getMessage();
                            Log.i(TAG, "signInWithEmail:failure : " + error);
                            Toast.makeText(LoginActivity.this, error , Toast.LENGTH_LONG).show();
                            //dismiss progressBar
                            ProgressBarHelper.hideProgressBar(loginProgressBar);

                        }

                        // ...
                    }
                });
    }

    /**
     * in this method we get the mobile device token
     */
    private void getDeviceToken() {

        String currentUserID = mAuth.getCurrentUser().getUid();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        //set token value id database's token child
        dbUsersNodeRef.child(currentUserID).child("token")
                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    Toast.makeText(getApplicationContext(), getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                    goToMain();
                }
            }
        });
    }

    /**
     * takes the user to register
     */
    private void goToRegister(){
        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }

    /**
     * takes the user to the main page
     */
    private void goToMain(){
        Intent intentToMain = new Intent (LoginActivity.this,MainActivity.class);
        startActivity(intentToMain);
        finish(); //he cannot go back to login page
    }


}
