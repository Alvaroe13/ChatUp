package com.example.alvar.chatapp.views;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithOutStack;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecoverPasswordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RecoverPasswordFragment";

    //firebase
    private FirebaseAuth mAuth;

    //ui elements
    private TextInputLayout recoverPassword;
    private Button recoverPasswordBtn;
    private Toolbar toolbarRecovery;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;
    private View viewLayout;

    public RecoverPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_recover_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewLayout = view;
        Log.d(TAG, "onViewCreated: called");
        bindUI(view);
        setToolbar(getString(R.string.toolbarRecoveryPasswordTitle), view);
        recoverPasswordBtn.setOnClickListener(this);
        toolBarClick();
    }

    private void bindUI(View view){
        recoverPassword = view.findViewById(R.id.recoverPassword);
        recoverPasswordBtn = view.findViewById(R.id.recoverPasswordBtn);
        toolbarRecovery = view.findViewById(R.id.toolbarRecovery);
        progressBar = view.findViewById(R.id.recoveryProgressBar);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
    }

    private void setToolbar(String title, View view){
        //create toolbar
        toolbarRecovery = view.findViewById(R.id.toolbarRecovery);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarRecovery);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void toolBarClick(){
        toolbarRecovery.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: back button toolbar clicked");
                try {
                    getActivity().onBackPressed();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * method in charge of sending to the user an email to recover password
     */
    private void sendEmailRecovery() {
        //we capture email enter by user
        String emailText = recoverPassword.getEditText().getText().toString();

        if (emailText.equals("")) {
            //show message to the user
            SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.enterEmailForRecovery));
            //hide progressbar
            ProgressBarHelper.hideProgressBar(progressBar);
        } else{
            mAuth.sendPasswordResetEmail(emailText).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        //show confirmation message to the user
                        try {
                            SnackbarHelper.showSnackBarLong(coordinatorLayout, getContext().getString(R.string.check_your_inbox));
                            ProgressBarHelper.hideProgressBar(progressBar);
                            closeKeyboard();
                        }catch (NullPointerException e){
                            Log.e(TAG, "onComplete: " + e.getMessage());
                        }

                    } else {
                        //we save the exception message generated by firebase
                        String error = task.getException().getMessage();
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                        //hide progressbar
                        ProgressBarHelper.hideProgressBar(progressBar);
                    }
                }
            });
        }
    }

    /**
     * method in charge of closing keyboard
     */
    private void closeKeyboard(){
        try {

            View view = getActivity().getCurrentFocus();
            if (view != null){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        } catch (NullPointerException e){
            Log.e(TAG, "closeKeyboard: " + e.getMessage() );
        }

    }


    @Override
    public void onClick(View v) {

        if (v.getId() ==  R.id.recoverPasswordBtn ){
            //show progressBar
            ProgressBarHelper.showProgressBar(progressBar);
            //execute email recovery method
            sendEmailRecovery();
        }

    }


}
