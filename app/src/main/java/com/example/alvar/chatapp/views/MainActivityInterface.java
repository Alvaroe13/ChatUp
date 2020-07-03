package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Here we're going to place all method to set a communication with Activity
 * (Fragment to Activity communication).
 */
public interface MainActivityInterface {

    //method used in "AllUsersFragment" to open user's profile
    void inflateFragment(@NonNull Fragment fragment, @NonNull String contactID);



}
