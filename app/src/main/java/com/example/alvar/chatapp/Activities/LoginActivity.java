package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //UI elements
    private TextInputLayout usernameLogin, passwordLogin;
    private ProgressBar loginProgressBar;
    private CoordinatorLayout coordinatorLayout;

    //vars
    private Button btnLogin;
    private String email, password;
    private TextView txtCreateAccount, forgotPasswordtxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //firebase service init
        mAuth = FirebaseAuth.getInstance();
        BindUI();
        goToRegister();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = usernameLogin.getEditText().getText().toString().trim();
                password = passwordLogin.getEditText().getText().toString().trim();

                if (email.equals("") ||  password.equals("")){
                    Log.i(TAG, "btnLogin clicked, some field is empty");
                    //Show info using snackbar
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.noEmptyField));
                } else {
                    Log.i(TAG, "btnLogin clicked no filed empty, proceed to call signIn method");
                    ProgressBarHelper.showProgressBar(loginProgressBar);
                    sigIn(email, password);
                }



            }
        });

        forgotPasswordtxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
                startActivity(i);
            }
        });


    }
    private void BindUI(){
        usernameLogin = findViewById(R.id.loginName);
        passwordLogin = findViewById(R.id.loginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);
        forgotPasswordtxt = findViewById(R.id.forgotPasswordText);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //if user is signed in we take the user to main activity
        if (currentUser != null){
            Intent intentToMain = new Intent (this,MainActivity.class);
            startActivity(intentToMain);
            finish();       //he cannot go back to login page
            Log.i(TAG, "onStart: onStart method called");
        }

    }

    private void sigIn(String email, String password){

        email = usernameLogin.getEditText().getText().toString().trim();
        password = passwordLogin.getEditText().getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithEmail:success");
                            user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "login with " + user, Toast.LENGTH_SHORT).show();
                            goToMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            String error = task.getException().getMessage();
                            Log.i(TAG, "signInWithEmail:failure : " + error);
                            Toast.makeText(LoginActivity.this, error , Toast.LENGTH_LONG).show();
                            //indicates wonrg credentials
                         //   SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.loginError));
                            //dismiss progressBar
                            ProgressBarHelper.hideProgressBar(loginProgressBar);

                        }

                        // ...
                    }
                });
    }

    private void goToRegister(){
        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }

    private void goToMain(){
        Intent intentToMain = new Intent (LoginActivity.this,MainActivity.class);
        startActivity(intentToMain);
        finish();
    }


}
