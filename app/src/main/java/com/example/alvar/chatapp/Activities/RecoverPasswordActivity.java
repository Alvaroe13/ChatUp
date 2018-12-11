package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPasswordActivity extends AppCompatActivity {

    private static final String TAG = "RecoverPasswordActivity";

    //firebase
    private FirebaseAuth mAuth;

    //ui elements
    private TextInputLayout recoverPassword;
    private Button recoverPasswordBtn;
    private Toolbar toolbarRecovery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);
        bindUI();
        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        //init toolbar
        setToolbar(getString(R.string.toolbarRecoveryPasswordTitle));

        recoverPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailrecovery();
            }
        });
    }

    private void bindUI(){
        recoverPassword = findViewById(R.id.recoverPassword);
        recoverPasswordBtn = findViewById(R.id.recoverPasswordBtn);
        toolbarRecovery = findViewById(R.id.toolbarRecovery);
    }

    private void setToolbar(String title){
        //create toolbar
        toolbarRecovery = findViewById(R.id.toolbarRecovery);
        //we set the toolbar
        setSupportActionBar(toolbarRecovery);
        //we pass the title
        getSupportActionBar().setTitle(title);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void sendEmailrecovery() {
        //we capture email enter by user
        String emailtext = recoverPassword.getEditText().getText().toString();

        if (emailtext.equals("")) {

            Toast.makeText(this, getString(R.string.enterEmailForRecovery), Toast.LENGTH_SHORT).show();
        } else{
            mAuth.sendPasswordResetEmail(emailtext).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RecoverPasswordActivity.this, getString(R.string.recoverPassword), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(RecoverPasswordActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
