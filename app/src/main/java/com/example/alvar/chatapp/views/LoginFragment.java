package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithOutStack;
import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithStack;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //UI elements
    private TextInputLayout usernameLogin, passwordLogin;
    private ProgressBar loginProgressBar;
    private CoordinatorLayout coordinatorLayout;
    private TextView txtCreateAccount, forgotPasswordTxt, btnPhoneLogin;
    private Button btnLogin;
    private View viewLayout;
    //vars
    private String email, password;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called!!!");
        initFirebase();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView:  called ");
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //we make the view global to access it from everywhere in the class
        viewLayout = view;

        Log.d(TAG, "onViewCreated: called as well");
        BindUI(view);
        buttonsUI();
        checkUserStatus();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * init firebase services
     */
    private void initFirebase(){ 
        mAuth = FirebaseAuth.getInstance(); 
        database = FirebaseDatabase.getInstance(); 
        dbUsersNodeRef = database.getReference().child("Users");
    }

    private void checkUserStatus(){

        //if user is signed in we take the user to the main activity
        if (currentUser != null){
            Log.i(TAG, "onStart: user sent to Home_Fragment as is signed in");
            Navigation.findNavController(viewLayout).navigate(R.id.homeFragment);
        }
    }

    /**
     * Init UI elements
     */
    private void BindUI(View view){
        usernameLogin = view.findViewById(R.id.loginName);
        passwordLogin = view.findViewById(R.id.loginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnPhoneLogin = view.findViewById(R.id.btnPhoneLogin);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        txtCreateAccount = view.findViewById(R.id.txtCreateAccount);
        forgotPasswordTxt = view.findViewById(R.id.forgotPasswordText);
    }

    private void btnLoginPressed(){

        email = usernameLogin.getEditText().getText().toString().trim();
        password = passwordLogin.getEditText().getText().toString().trim();

        if (email.equals("") || password.equals("")){
            Log.i(TAG, "btnLogin clicked, some field is empty");
            //Show info using snack bar
            SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.noEmptyField));
        } else {
            Log.i(TAG, "btnLogin clicked no filed empty, proceed to call signIn method");
            ProgressBarHelper.showProgressBar(loginProgressBar);
            sigIn(email, password);
        }

    }


    private void buttonsUI(){
        btnLogin.setOnClickListener(this);
        txtCreateAccount.setOnClickListener(this);
        forgotPasswordTxt.setOnClickListener(this);
        btnPhoneLogin.setOnClickListener(this);
    }

    /**
     * this one's in charge of signing into firebase db.
     * @param email
     * @param password
     */
    private void sigIn(String email, String password){

        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i(TAG, "signInWithEmail:success" );
                                getDeviceToken();

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i(TAG, "signInWithEmail:failure : " + task.getException().getMessage() );
                                Toast.makeText(getActivity(), task.getException().getMessage() , Toast.LENGTH_LONG).show();
                                //dismiss progressBar
                                ProgressBarHelper.hideProgressBar(loginProgressBar);
                            }

                        }
                    });

        }catch (Exception e){
            Log.e(TAG, "sigIn: error: " + e.getMessage() );
        }


    }

    /**
     * in this method we get the mobile device token
     */
    private void getDeviceToken() {

        final String currentUserID = mAuth.getCurrentUser().getUid();
        final String deviceToken = FirebaseInstanceId.getInstance().getToken();

        //set token value id database's token child
        dbUsersNodeRef.child(currentUserID).child("token")
                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    navigateWithOutStack(viewLayout , R.id.homeFragment, null);
                }
            }
        });
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnLogin:
                btnLoginPressed();
                break;
            case R.id.btnPhoneLogin:
                Log.d(TAG, "onClick: phone login button pressed");
                navigateWithStack(viewLayout, R.id.phoneRegisterFragment, null);
                break;
            case R.id.forgotPasswordText:
                Log.d(TAG, "onClick: forgot password button pressed");
                navigateWithStack(viewLayout, R.id.recoverPasswordFragment, null);
                break;
            case R.id.txtCreateAccount:
                navigateWithStack(viewLayout, R.id.registerFragment, null);
                break;

        }

    }
}
