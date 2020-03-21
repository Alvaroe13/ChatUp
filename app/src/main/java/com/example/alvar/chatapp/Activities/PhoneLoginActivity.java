package com.example.alvar.chatapp.Activities;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.SnackbarHelper;

public class PhoneLoginActivity extends AppCompatActivity {

    private static final String TAG = "PhoneLoginActivity";

    private EditText textPhoneNumber, textCode;
    private Button btnVerifyCode, btnSendCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        bindUI();
        sendCodeButton();
        loginButton();

    }

    /**
     * UI elements
     */
    private void bindUI(){
        textPhoneNumber = findViewById(R.id.loginPhoneNumberTxt);
        textCode = findViewById(R.id.loginCodeTxt);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerifyCode = findViewById(R.id.btnLoginCode);
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

                //we get the phone number and store it in a variable
                String phoneNumber = textPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)){
                    Log.i(TAG, "onClick: field empty");
                    Toast.makeText(PhoneLoginActivity.this, "You must insert a phone number", Toast.LENGTH_SHORT).show();

                } else{
                    //when send code button is pressed let's remove these fields
                    textPhoneNumber.setVisibility(View.INVISIBLE);
                    btnSendCode.setVisibility(View.INVISIBLE);
                    //and let's show these fields
                    textCode.setVisibility(View.VISIBLE);
                    btnVerifyCode.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /**
     * when login button is pressed
     */
    private void loginButton() {

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Toast.makeText(PhoneLoginActivity.this, "To be continued...", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
