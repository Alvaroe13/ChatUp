package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    //this is just a test for the new branch and old branch check out

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);
        bindUI();
        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

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
    }

    private void sendEmailrecovery() {
        String emailtxt = recoverPassword.getEditText().getText().toString();
        if (emailtxt.equals("")) {

            Toast.makeText(this, "You must insert your email address", Toast.LENGTH_SHORT).show();
        } else{
            mAuth.sendPasswordResetEmail(emailtxt).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RecoverPasswordActivity.this, "Watch you mailbox", Toast.LENGTH_SHORT).show();
//                        Intent i = new Intent(RecoverPasswordActivity.this, LoginActivity.class);
//                        startActivity(i);
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
