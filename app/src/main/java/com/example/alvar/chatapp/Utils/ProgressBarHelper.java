package com.example.alvar.chatapp.Utils;

import android.view.View;
import android.widget.ProgressBar;

public class ProgressBarHelper {

    //Methods to call progressbar in different views

    public static ProgressBar showProgressBar(ProgressBar progressBar ){
        progressBar.setVisibility(View.VISIBLE);
        return  progressBar;
    }

    public static ProgressBar  hideProgressBar(ProgressBar progressBar){
        progressBar.setVisibility(View.INVISIBLE);
        return  progressBar;
    }

}
