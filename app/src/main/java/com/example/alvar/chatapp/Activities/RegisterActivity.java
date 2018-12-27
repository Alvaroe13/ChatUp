package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class RegisterActivity extends AppCompatActivity {
    //Log
    private static final String TAG = "RegisterPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    //UI elements
    private TextInputLayout usernameTextField, emailTextField, passwrodTextField, repeatPasswordTextField;
    private Button registerBtn;
    private Toolbar toolbarRegister;
    private ProgressBar registerProgressBar;
    private CoordinatorLayout coordinatorLayout;
    //Vars
    private String username, email, password, repeatPassword;
    //Constants
    private static final String DEFAULT_IMG = "image";
    public static final String DEFAULT_STATUS = "Hi there I am using ChatUp";
    private static final String DEFAULT_THUMBNAIL = "imgThumbnail";
    private static final String DATABASE_TREE_NAME = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //init firebase auth
        mAuth = FirebaseAuth.getInstance();
        //init firebase real time database
        database = FirebaseDatabase.getInstance();
        //UI elements
        BindUI();
        //toolbar
        setToolbar(getString(R.string.register), true);
        //execute registration process.
        registerBtnPress();
    }

    /**
     * When "Create Account" button has been pressed
     */
    private void registerBtnPress() {
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameTextField.getEditText().getText().toString().trim();
                email = emailTextField.getEditText().getText().toString().trim();
                password = passwrodTextField.getEditText().getText().toString().trim();
                repeatPassword = repeatPasswordTextField.getEditText().getText().toString().trim();
                    //if there's a mandatory field empty
                if (username.equals("") || email.equals("") || password.equals("")  ){
                    Log.i(TAG, "onClick: enter this if statement");
                    //show info to user
                    SnackbarHelper.showSnackBarShort(coordinatorLayout, getString(R.string.noEmptyField));
                    //if password and repeat password field does not match
                } else if ( !password.equals(repeatPassword)  ){
                    Log.i(TAG, "onClick: check repeat password matches");
                    //show info to user
                    SnackbarHelper.showSnackBarShort(coordinatorLayout, getResources().getString(R.string.password_check));
                    //if password lenght is less than 6 characters
                } else if (password.length() <= 6  ){
                    Log.i(TAG, "onClick: check password lenght is longer than 7 digs");
                    //inform user the min length is 6
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.password_Length));
                } else{
                    Log.i(TAG, "onClick: Register New User method called");
                    //if everything goes well we create user account
                    registerNewUSer(username, email,password);
                    //hide progress bar
                    ProgressBarHelper.showProgressBar(registerProgressBar);
                }

            }
        });
    }

    /**
     We bind every UI element here
     */
    private void BindUI(){
        usernameTextField = findViewById(R.id.nameTextInput);
        emailTextField = findViewById(R.id.emailTextInput);
        passwrodTextField = findViewById(R.id.passwordInputText);
        registerBtn = findViewById(R.id.btnRegister);
        toolbarRegister = findViewById(R.id.toolbarRegister);
        repeatPasswordTextField = findViewById(R.id.repeatPasswordTextInput);
        registerProgressBar = findViewById(R.id.registerProgressBar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }
   /**
    Create toolbar and it's detail
    */
    private void setToolbar(String title, Boolean backOption){
        toolbarRegister = findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbarRegister);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }
    /**
     Method in charge of creating new account
     */
    private void registerNewUSer(final String username, final String email, final String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "createUserWithEmail:success");
                            //register info in real-time database
                            createNewAccount();
                        }else {
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "createUserWithEmail:failure" + task.getException());
                            //show error message to user
                            SnackbarHelper.showSnackBarLong(coordinatorLayout,getString(R.string.loginError ));
                            //hide progress bar
                            ProgressBarHelper.hideProgressBar(registerProgressBar);
                        }


                    }
                });
    }

    /**
     * Intent to take to main page
     */
    private void goToMain(){
        Intent intentToMain = new Intent (RegisterActivity.this,MainActivity.class);
        startActivity(intentToMain);
        finish();
    }

    private void createNewAccount(){

        //check current user logged in
        currentUser = mAuth.getCurrentUser();
        //we get user unique ID in Firebase
        String userID = currentUser.getUid();
        //create database Tree with name "Users" and child is the user ID
        mRef = database.getReference("Users").child(userID);
        //Init user object and set it's values to be save into the db
        User user = new User( username, email, password, DEFAULT_STATUS, DEFAULT_IMG, DEFAULT_THUMBNAIL );

        //save info into database and we do a last check
        mRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //if everything goes well
                if (task.isSuccessful()) {
                    //show welcome message to user
                    Toast.makeText(RegisterActivity.this, getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                    //we take user to main page
                    goToMain();
                //if there is a problem with the server
                }else{
                    Log.i(TAG, "onComplete: error" + task.getException());
                    //show message indicating there is a problem with the server
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.server_error));
                    //hide progress bar
                    ProgressBarHelper.hideProgressBar(registerProgressBar);
                }

            }
        });

    }





}
