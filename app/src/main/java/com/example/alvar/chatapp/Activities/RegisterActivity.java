package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class RegisterActivity extends AppCompatActivity {
    //Log
    private static final String TAG = "RegisterPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //UI elements
    private TextInputLayout usernameTextField, emailTextField, passwordTextField, repeatPasswordTextField;
    private Button registerBtn;
    private Toolbar toolbarRegister;
    private ProgressBar registerProgressBar;
    private CoordinatorLayout coordinatorLayout;
    //Vars
    private String username, email, password, repeatPassword, deviceToken;
    //Constants
    private static final String DEFAULT_IMG = "image";
    public static final String DEFAULT_STATUS = "Hi there I am using ChatUp";
    private static final String DEFAULT_THUMBNAIL = "imgThumbnail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //UI elements
        BindUI();
        //init firebase services
        initFirebase();
        //toolbar
        setToolbar(getString(R.string.register), true);
        //execute registration process.
        registerBtnPress();
    }

    /**
     * We bind every UI element here
     */
    private void BindUI() {
        usernameTextField = findViewById(R.id.nameTextInput);
        emailTextField = findViewById(R.id.emailTextInput);
        passwordTextField = findViewById(R.id.passwordInputText);
        registerBtn = findViewById(R.id.btnRegister);
        toolbarRegister = findViewById(R.id.toolbarRegister);
        repeatPasswordTextField = findViewById(R.id.repeatPasswordTextInput);
        registerProgressBar = findViewById(R.id.registerProgressBar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    /**
     * Create toolbar and it's detail
     */
    private void setToolbar(String title, Boolean backOption) {
        toolbarRegister = findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbarRegister);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    /**
     * init firebase services
     */

    private void initFirebase() {

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();
        //init firebase real time database
        database = FirebaseDatabase.getInstance();
        //create database Tree with name "Users" and child is the user ID
        dbUsersNodeRef = database.getReference("Users");

    }

    /**
     * When "Create Account" button has been pressed
     */
    private void registerBtnPress() {

        //here we get user's device token
        deviceToken = FirebaseInstanceId.getInstance().getToken();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameTextField.getEditText().getText().toString().trim();
                email = emailTextField.getEditText().getText().toString().trim();
                password = passwordTextField.getEditText().getText().toString().trim();
                repeatPassword = repeatPasswordTextField.getEditText().getText().toString().trim();

                //if there's a mandatory field empty
                if (username.equals("") || email.equals("") || password.equals("") || repeatPassword.equals("")) {
                    Log.i(TAG, "onClick: enter this if statement");
                    //show info to user
                    SnackbarHelper.showSnackBarShort(coordinatorLayout, getString(R.string.noEmptyField));
                    //if password and repeat password field does NOT match
                } else if (!password.equals(repeatPassword)) {
                    Log.i(TAG, "onClick: check repeat password matches");
                    //show info to user
                    SnackbarHelper.showSnackBarShort(coordinatorLayout, getResources().getString(R.string.password_check));
                    //if password length is less than 6 characters
                } else if (password.length() <= 6) {
                    Log.i(TAG, "onClick: check password length is longer than 7 digits");
                    //inform user password's minimum length is 6
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.password_Length));
                } else {
                    Log.i(TAG, "onClick: Register New User method called");
                    //if everything goes well we create user account
                    registerNewUSer(email, password);
                    //show progress bar
                    ProgressBarHelper.showProgressBar(registerProgressBar);
                }

            }
        });
    }



    /**
     * Method in charge of creating new account
     */
    private void registerNewUSer(final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "createUserWithEmail:success");
                            //register info in real-time database
                            saveInfoInDataBase();
                        } else {

                            String error = task.getException().getMessage();
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "createUserWithEmail:failure" + error);
                            //show error message
                            Toast.makeText(RegisterActivity.this, error , Toast.LENGTH_SHORT).show();
                            //hide progress bar
                            ProgressBarHelper.hideProgressBar(registerProgressBar);
                        }


                    }
                });
    }

    /**
     * this method is the one in charge of saving the info into the database
     */
    private void saveInfoInDataBase(){

        //we get user unique ID in Firebase
        String currentUserID = mAuth.getCurrentUser().getUid();

        //Init User object and set it's values to be saved into the db
        User user = new User(username, email, password, DEFAULT_STATUS, DEFAULT_IMG, DEFAULT_THUMBNAIL, deviceToken, currentUserID);

        //save info into database and we do a last check
        dbUsersNodeRef.child(currentUserID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //if everything goes well
                if (task.isSuccessful()) {
                    //show welcome message to user
                    Toast.makeText(RegisterActivity.this, getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                    //we take user to main page
                    goToMain();
                    //if there is a problem with the server
                } else {
                    Log.i(TAG, "onComplete: error" + task.getException());
                    //show message indicating there is a problem with the server
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.server_error));
                    //hide progress bar
                    ProgressBarHelper.hideProgressBar(registerProgressBar);
                }

            }
        });

    }

    /**
     * Intent to take to main page
     */
    private void goToMain() {
        Intent intentToMain = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intentToMain);
        finish();
    }





}
