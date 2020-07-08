package com.example.alvar.chatapp.views;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_EMAIL;
import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_PASSWORD;
import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_STATUS;
import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_THUMBNAIL;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneRegisterFragment extends Fragment {

    private static final String TAG = "PhoneRegisterFragment";

    //UI
    private TextInputEditText nameField, textPhoneNumber, textCode;
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

    public PhoneRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        firebaseInit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called");
        popUpInit();
        bindUI(view);
        callBacksMethod();
        sendCodeButton();
        loginButton();
    }

    /**
     * init pop up ProgressDialog
     */
    private void popUpInit() {
        popUp = new ProgressDialog(getContext());
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
     * UI elements
     */
    private void bindUI(View view){
        nameField = view.findViewById(R.id.loginPhoneNumberNameTxt);
        textPhoneNumber = view.findViewById(R.id.loginPhoneNumberTxt);
        textCode = view.findViewById(R.id.loginCodeTxt);
        btnSendCode = view.findViewById(R.id.btnSendCode);
        btnVerifyCode = view.findViewById(R.id.btnLoginCode);
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
                Toast.makeText(getContext(), "Welcome!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                //Something failed
                popUp.dismiss();

                Toast.makeText(getContext(), "Please insert a valid phone number", Toast.LENGTH_SHORT).show();

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

                Toast.makeText(getContext(), "Code sent successfully", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(getContext(), "You must insert a phone number and name.", Toast.LENGTH_SHORT).show();

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
                getActivity(),        // Activity (for callback binding)
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
                    Toast.makeText(getContext(), "Please insert code.", Toast.LENGTH_SHORT).show();
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
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            popUp.dismiss();
                            Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();

                            saveInfoInDB();

                        } else {
                            //let's get the error in a var
                            String error = task.getException().toString();
                            //show error to the user
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveInfoInDB() {


        //we get user unique ID in Firebase
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        //here we get user's device token  (NOT APPLIED TO THE APP JUST YET)
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        User user = new User(name , DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_STATUS,DEFAULT_IMAGE,DEFAULT_THUMBNAIL,deviceToken, currentUserID);

        //save info into database and we do a last check
        dbUsersNodeRef.child(currentUserID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //if everything goes well
                if (task.isSuccessful()) {
                    //show welcome message to user
                    Toast.makeText(getContext(), getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                    //we take user to main page
                  //  goToMain(); replace with nav component controller
                    //if there is a problem with the server
                } else {
                    Log.i(TAG, "onComplete: error" + task.getException());
                    String error = task.getException().toString();
                    Toast.makeText(getContext(), "Something went wrong: " + error, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}
