package com.example.alvar.chatapp.Utils;

import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class SnackbarHelper {

    public static Snackbar showSnackBarShort(CoordinatorLayout coordinatorLayout, String title){
        Snackbar snackbar = Snackbar.make( coordinatorLayout, title, Snackbar.LENGTH_SHORT);
        View snackView = snackbar.getView();
        TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.MAGENTA);
        snackbar.show();

        return  snackbar;
    }

    public static Snackbar showSnackBarLong(CoordinatorLayout coordinatorLayout, String title){
        Snackbar snackbar = Snackbar.make( coordinatorLayout, title, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.MAGENTA);
        snackbar.show();

        return  snackbar;
    }



}
