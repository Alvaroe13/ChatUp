package com.example.alvar.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private static final String TAG = "PhoneLoginActivity";
    //UI
    private EditText nameField, textPhoneNumber, textCode;
    private Button btnVerifyCode, btnSendCode;
    private ProgressDialog popUp;
    //Firebase elements
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    //vars
    private String name, phoneNumber, mVerificationId;
    //Constants
    private static final String DEFAULT_EMAIL = "email";
    public static final String DEFAULT_STATUS = "Hi there I am using ChatUp";
    private static final String DEFAULT_IMAGE = "image";
    private static final String DEFAULT_THUMBNAIL = "imgThumbnail";
    private static final String DEFAULT_PASSWORD = "null";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        bindUI();
        popUpInit();
        firebaseInit();
        callBacksMethod();
        sendCodeButton();
        loginButton();

    }

    /**
     * UI elements
     */
    private void bindUI(){
        nameField = findViewById(R.id.loginPhoneNumberNameTxt);
        textPhoneNumber = findViewById(R.id.loginPhoneNumberTxt);
        textCode = findViewById(R.id.loginCodeTxt);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerifyCode = findViewById(R.id.btnLoginCode);
    }

    /**
     * init pop up ProgressDialog
     */
    private void popUpInit() {
        popUp = new ProgressDialog(PhoneLoginActivity.this);
    }

    /**
     * init firebase services
     */
    private void firebaseInit() {

        //firebase authentication init.
        firebaseAuth = FirebaseAuth.getInstance();
        //init firebase real time database
        database = FirebaseDatabase.getInstance();
        //create database Tree with name "Users" and child is the user ID
        dbUsersNodeRef = database.getReference("Users");

    }


    /**
     * method in charge of verifying the code sent by Firebase
     */
    private void callBacksMethod() {

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                /*this method is called either when the user inserts the code manually or when the system automatically
                  recognizes the phone has received SMS with valid code*/
                popUp.dismiss();

                signInWithPhoneAuthCredential(credential);
                Toast.makeText(PhoneLoginActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                //Something failed
                popUp.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Please insert a valid phone number", Toast.LENGTH_SHORT).show();

                textPhoneNumber.setVisibility(View.VISIBLE);
                btnSendCode.setVisibility(View.VISIBLE);

                textCode.setVisibility(View.INVISIBLE);
                btnVerifyCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later

                popUp.dismiss();

                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code sent successfully", Toast.LENGTH_SHORT).show();

                //when send code button is pressed let's remove these fields
                textPhoneNumber.setVisibility(View.INVISIBLE);
                btnSendCode.setVisibility(View.INVISIBLE);
                //and let's show these fields
                textCode.setVisibility(View.VISIBLE);
                btnVerifyCode.setVisibility(View.VISIBLE);
            }
        };

    }

    /**
     * when send code button is pressed,
     */
    private void sendCodeButton(){

        //let's make sure these fields are invisible by default
        textCode.setVisibility(View.INVISIBLE);
        btnVerifyCode.setVisibility(View.INVISIBLE);

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = nameField .getText().toString();
                //we get the phone number and store it in a variable
                phoneNumber = textPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(name) ) {
                    Log.i(TAG, "onClick: field empty");
                    Toast.makeText(PhoneLoginActivity.this, "You must insert a phone number and name.", Toast.LENGTH_SHORT).show();

                } else{

                    Log.i(TAG, "onClick: We've sent code");

                    popUp.setTitle("Verifying phone number");
                    popUp.setMessage("Please wait while we verify phone number");
                    popUp.setCanceledOnTouchOutside(false);
                    popUp.show();;

                    sendCode();

                }
            }
        });

    }

    /**
     * method contains the logic behind sending the security code.
     */
    private void sendCode() {

        //Phone auth init   (Copied and paste from Firebase docs)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneLoginActivity.this,        // Activity (for callback binding)
                callbacks           // OnVerificationStateChangedCallbacks
        );



    }

    /**
     * when login button is pressed in case the system doesn't verify the code automatically
     */
    private void loginButton() {

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


              String verificationCode = textCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode) ){
                    Toast.makeText(PhoneLoginActivity.this, "Please insert code.", Toast.LENGTH_SHORT).show();
                } else{

                    popUp.setTitle("Verifying code");
                    popUp.setMessage("Please wait while we verify the security code");
                    popUp.setCanceledOnTouchOutside(false);
                    popUp.show();

                    //User logs in
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }


            }
        });
    }

    /**
     * method in charge of signing in.
     * @param credential
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            
                            popUp.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Done!", Toast.LENGTH_SHORT).show();

                            saveInfoInDB();

                        } else {
                                //let's get the error in a var
                            String error = task.getException().toString();
                                //show error to the user
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveInfoInDB() {


        //we get user unique ID in Firebase
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        //here we get user's device token  (NOT APPLIED TO THE APP JUST YET)
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        User user = new User(name,DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_STATUS,DEFAULT_IMAGE,DEFAULT_THUMBNAIL,deviceToken);

        //save info into database and we do a last check
        dbUsersNodeRef.child(currentUserID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //if everything goes well
                if (task.isSuccessful()) {
                    //show welcome message to user
                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                    //we take user to main page
                    goToMain();
                    //if there is a problem with the server
                } else {
                    Log.i(TAG, "onComplete: error" + task.getException());
                    String error = task.getException().toString();
                    Toast.makeText(PhoneLoginActivity.this, "Something went wrong: " + error, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    /**
     * we send the user to the main activity
     */
    private void goToMain() {

        Intent intent   = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }


}