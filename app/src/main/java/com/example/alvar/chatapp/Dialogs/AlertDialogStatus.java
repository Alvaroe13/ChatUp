package com.example.alvar.chatapp.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alvar.chatapp.Activities.StatusChangeActivity;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AlertDialogStatus extends DialogFragment {

    private static final String TAG = "AlertDialogStatus";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    //ui
    private View statusChangeView;
    private LayoutInflater viewInflater;
    private AlertDialog.Builder builder;
    private EditText changeStatusField;
    //Vars
    private String userID;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //init firebase method
        initFirebase();
        return showAlertDialog();
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
     * this method inflates the Alert Dialog layout
     * @return
     */
    private AlertDialog showAlertDialog() {

        //Create dialog builder
        builder = new AlertDialog.Builder(getActivity());
        viewInflater= getActivity().getLayoutInflater();
        statusChangeView = viewInflater.inflate(R.layout.status_change_layout, null);
        changeStatusField = statusChangeView.findViewById(R.id.changeStatusField);
        builder.setView(statusChangeView);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newStatus = changeStatusField.getText().toString();

                if (TextUtils.isEmpty(newStatus)){
                    Toast.makeText(statusChangeView.getContext(), "Field can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                   newStatus = changeStatusField.getText().toString();
                    statusChanged(newStatus);
                }

            }
        });

        return builder.create();
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
                    Log.i(TAG, "onComplete: updated successfully ");
                } else{
                    //we save error thrown by firebase and save it into var "error"
                    String statusChangeError = task.getException().getMessage();
                    //show error message to user
                    Log.i(TAG, "onComplete: error" + statusChangeError);
                }
            }
        });
    }





}
