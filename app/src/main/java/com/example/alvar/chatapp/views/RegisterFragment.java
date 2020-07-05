package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.Toast;

import com.example.alvar.chatapp.Activities.RegisterActivity;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_STATUS;
import static com.example.alvar.chatapp.Utils.Constant.DEFAULT_THUMBNAIL;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "RegisterFragment";
    
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //UI elements
    private TextInputLayout usernameTextField, emailTextField, passwordTextField, repeatPasswordTextField;
    private Button registerBtn;
    private Toolbar toolbarRegister;
    private ProgressBar registerProgressBar;
    private CoordinatorLayout coordinatorLayout;
    private View viewLayout;
    //Vars
    private String username, email, password, repeatPassword, deviceToken;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        initFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewLayout = view;
        bindUI(view);
        setToolbar(getString(R.string.register), view, true);
        registerBtn.setOnClickListener(this);
    }

    /**
     * init firebase services
     */

    private void initFirebase() {

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference("Users");

    }


    /**
     * We bind every UI element here
     */
    private void bindUI(View view) {
        usernameTextField = view.findViewById(R.id.nameTextInput);
        emailTextField = view.findViewById(R.id.emailTextInput);
        passwordTextField = view.findViewById(R.id.passwordInputText);
        registerBtn = view.findViewById(R.id.btnRegister);
        toolbarRegister = view.findViewById(R.id.toolbarRegister);
        repeatPasswordTextField = view.findViewById(R.id.repeatPasswordTextInput);
        registerProgressBar = view.findViewById(R.id.registerProgressBar);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
    }

    /**
     * Create toolbar and it's detail
     */
    private void setToolbar(String title, View view, Boolean backOption) {
        toolbarRegister = view.findViewById(R.id.toolbarRegister);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarRegister);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    /**
     * When "Create Account" button has been pressed
     */
    private void registerBtnPress() {

        //here we get user's device token
        deviceToken = FirebaseInstanceId.getInstance().getToken();

        username = usernameTextField.getEditText().getText().toString().trim();
        email = emailTextField.getEditText().getText().toString().trim();
        password = passwordTextField.getEditText().getText().toString().trim();
        repeatPassword = repeatPasswordTextField.getEditText().getText().toString().trim();

        //if there's a mandatory field empty
        if (username.equals("") || email.equals("") || password.equals("") || repeatPassword.equals("")) {
            Log.i(TAG, "onClick: enter this if statement");
            SnackbarHelper.showSnackBarShort(coordinatorLayout, getString(R.string.noEmptyField));
        }
        else if (!password.equals(repeatPassword)) {
            Log.i(TAG, "onClick: check repeat password matches");
            SnackbarHelper.showSnackBarShort(coordinatorLayout, getResources().getString(R.string.password_check));
            //if password length is less than 6 characters
        }
        else if (password.length() <= 6) {
            Log.i(TAG, "onClick: check password length is longer than 7 digits");
            //inform user password's minimum length is 6
            SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.password_Length));
        }
        else {
            Log.i(TAG, "onClick: Register New User method called");
            registerNewUSer(email, password);
            //show progress bar
            ProgressBarHelper.showProgressBar(registerProgressBar);
        }
    }

    /**
     * Method in charge of creating new account
     */
    private void registerNewUSer(final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "createUserWithEmail:success");
                            //register info in real-time database
                            saveInfoInDataBase();
                        } else {

                            String error = task.getException().getMessage();
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "createUserWithEmail:failure" + error);
                            //show error message
                            Toast.makeText(getContext(), error , Toast.LENGTH_SHORT).show();
                            //hide progress bar
                            ProgressBarHelper.hideProgressBar(registerProgressBar);
                        }


                    }
                });
    }

    /**
     * this method is the one in charge of saving the info into the database
     */
    private void saveInfoInDataBase(){

        //we get user unique ID in Firebase
        String currentUserID = mAuth.getCurrentUser().getUid();

        //Init User object and set it's values to be saved into the db
        User user = new User(username, email, password, DEFAULT_STATUS, DEFAULT_IMAGE, DEFAULT_THUMBNAIL, deviceToken, currentUserID);

        //save info into database and we do a last check
        dbUsersNodeRef.child(currentUserID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //if everything goes well
                if (task.isSuccessful()) {
                    //show welcome message to user
                    navigateWithOutStack(viewLayout, R.id.homeFragment);
                } else {
                    Log.i(TAG, "onComplete: error" + task.getException()); 
                    SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.server_error)); 
                    ProgressBarHelper.hideProgressBar(registerProgressBar);
                }

            }
        });
    
    
    }

    /**
     * navigate cleaning the stack
     * @param layout
     */
    private void navigateWithOutStack(View view, int layout){

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();

        Navigation.findNavController(view).navigate(layout, null, navOptions);

    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnRegister){
            registerBtnPress();
        }

    }
}
