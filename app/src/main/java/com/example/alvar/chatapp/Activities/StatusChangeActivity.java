package com.example.alvar.chatapp.Activities;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusChangeActivity extends AppCompatActivity {

    private static final String TAG = "StatusChangePage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    //UI elements
    private TextInputLayout statusChangeTxt;
    private Button statusChangeButton;
    private Toolbar statusPageToolbar;
    private CoordinatorLayout statusCoordinatorLayout;
    //Vars
    private String userID;
    //Const firebase database
    private static final String DATABASE_NODE = "Users";
    private static final String DATABASE_CHILD_STATUS = "status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_change);
        //bind UI elements
        bindUI();
        //init firebase services
        initFirebase();
        //init toolbar
        setToolbar(getString(R.string.statusToolbar));
        currentStatus();


        statusChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save button
                statusChanged();
            }
        });
    }


    /**
     * method in charge of initialize UI elements
     */
    private void bindUI(){
        statusChangeTxt = findViewById(R.id.statusChangeText);
        statusChangeButton = findViewById(R.id.statusChangeBtnSave);
        statusCoordinatorLayout = findViewById(R.id.statusCoordinatorLayout);
    }
    /**
     Create toolbar and it's detail
     */
    private void setToolbar(String title){
        statusPageToolbar = findViewById(R.id.statusToolbar);
        setSupportActionBar(statusPageToolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * this method is in charge of showing the current status in the status change page.
     */
    private void currentStatus() {
        //get intent from "Settings Activity"
        String currentStatus = getIntent().getStringExtra("currentStatus");
        //set value into the editText
        statusChangeTxt.getEditText().setText(currentStatus);
    }

    /**
     method in charge of initialize firebase service
     */
    private void initFirebase(){
        //init Firebase auth
        mAuth = FirebaseAuth.getInstance();
        //we get current user logged in
        currentUser = mAuth.getCurrentUser();
        //save unique UID from user logged-in to a var type String named "userID"
        userID = currentUser.getUid();
        //init Firebase database
        database = FirebaseDatabase.getInstance();
        //init database reference and we aim to the users data by passing "userID" as child.
        mRef = database.getReference(DATABASE_NODE).child(userID);
        Log.i(TAG, "initFirebase: userid: " + userID);
    }
    private void statusChanged() {

        String status = statusChangeTxt.getEditText().getText().toString();

        mRef.child(DATABASE_CHILD_STATUS).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.i(TAG, "onComplete: change done correctly");
                    //show confirmation message to user
                    SnackbarHelper.showSnackBarLong(statusCoordinatorLayout, getString(R.string.status_updated));
                } else{
                    //we save error thrown by firebase and save it into var "error"
                    String statusChangeError = task.getException().getMessage();
                    //show error message to user
                    Toast.makeText(StatusChangeActivity.this, statusChangeError , Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: error" + statusChangeError);
                }
            }
        });
    }
}
