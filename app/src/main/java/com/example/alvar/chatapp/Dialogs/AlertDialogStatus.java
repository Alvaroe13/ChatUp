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

import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AlertDialogStatus extends DialogFragment {

    private static final String TAG = "AlertDialogStatus";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    private FirebaseUser currentUser;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userDocRef;
    //ui
    private View statusChangeView;
    private LayoutInflater viewInflater;
    private AlertDialog.Builder builder;
    private EditText changeStatusField;
    //Vars
    private String currentUserID;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //init firebase method
        initFirebase();
        initFirestore();
        return showAlertDialog();
    }

    /**
     method in charge of initialize firebase service
     */
    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference(getString(R.string.users_ref)).child(currentUserID);
    }

    private void initFirestore(){
        mDb = FirebaseFirestore.getInstance();
        userDocRef = mDb.collection(getString(R.string.users_ref)).document(currentUserID);
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

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newStatus = changeStatusField.getText().toString();

                if (TextUtils.isEmpty(newStatus)){
                    Toast.makeText(statusChangeView.getContext(), getString(R.string.noEmptyFieldAllowed), Toast.LENGTH_SHORT).show();
                } else {
                   newStatus = changeStatusField.getText().toString();
                    statusChanged(newStatus);
                    updateFirestore(newStatus);
                    Toast.makeText(getActivity(), getString(R.string.status_updated), Toast.LENGTH_SHORT).show();
                }

            }
        });

        return builder.create();
    }


    /**
     * method in charge of updating user's status in firebase
     */
    private void statusChanged(final String status) {

        //we update the "status" section in the "Users" node from the db
        dbUsersNodeRef.child(getString(R.string.status_db)).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.i(TAG, "onComplete: updated in firebase ");

                } else{
                    //we save error thrown by firebase and save it into var "error"
                    String statusChangeError = task.getException().getMessage();
                    Log.d(TAG, "onComplete: error" + statusChangeError);
                }
            }
        });
    }

    /**
     * method in charge of updating user's status in firestore
     */
    private void updateFirestore(String status) {

        userDocRef.update(getString(R.string.status_db), status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onComplete: status updated in firestore");
                }

            }
        });

    }


}
