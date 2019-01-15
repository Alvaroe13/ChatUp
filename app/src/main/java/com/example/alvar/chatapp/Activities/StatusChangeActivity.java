package com.example.alvar.chatapp.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

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


        statusChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save button

                //we store in this var the new edit enter by the user
                String status = statusChangeTxt.getEditText().getText().toString() ;

                //if field is empty we make sure the user knows it
                if (status.equals("")){
                    SnackbarHelper.showSnackBarLongRed(statusCoordinatorLayout, getString(R.string.statusCannotBeEmpty));
                }
                else{
                    //we update the status
                    statusChanged(status);
                }


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
        mRef = database.getReference("Users").child(userID);
    }

    /**
     * method in charge of setting new status text in the UI
     */
    private void statusChanged(String status) {

        //we update the "status" section in the "Users" node from the db
                mRef.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.i(TAG, "onComplete: change done correctly");
                            closeKeyboard();
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

    /**
     * method in charge of closing keyboard
     */
    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
}
