package com.example.alvar.chatapp.Utils;

import android.graphics.Color;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {


    //this is the blue color used in the app as "PrimaryColorDark" = "#00509f";

    //this method show snackbar for a long period
    public static Snackbar showSnackBarLong(CoordinatorLayout coordinatorLayout, String title){
        Snackbar snackbar = Snackbar.make( coordinatorLayout, title, Snackbar.LENGTH_SHORT);
        View snackView = snackbar.getView();
        TextView textView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackView.setBackgroundColor(Color.parseColor("#00509f"));
        snackbar.show();

        return  snackbar;
    }

    //this method show snackbar for a short period
    public static Snackbar showSnackBarShort(CoordinatorLayout coordinatorLayout, String title){
        Snackbar snackbar = Snackbar.make( coordinatorLayout, title, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        TextView textView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackView.setBackgroundColor(Color.parseColor("#00509f"));
        snackbar.show();

        return  snackbar;
    }


    //this method show background-red snackbar for a long period
    public static Snackbar showSnackBarLongRed(CoordinatorLayout coordinatorLayout, String title){
        Snackbar snackbar = Snackbar.make( coordinatorLayout, title, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        TextView textView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackView.setBackgroundColor(Color.parseColor("#ff5521"));
        snackbar.show();

        return  snackbar;
    }




}
