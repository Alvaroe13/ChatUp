package com.example.alvar.chatapp.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.example.alvar.chatapp.R;

import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

public class NavHelper {



    /**
     * navigate adding to the back stack
     * @param layout
     */
    public static void navigateWithStack(View view , int layout, Bundle bundle){
        Navigation.findNavController(view).navigate(layout, bundle);
    }

    /**
     * navigate cleaning the stack
     * @param layout
     */
    public static void navigateWithOutStack(View view, int layout, Bundle bundle){

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();

        Navigation.findNavController(view).navigate(layout, bundle, navOptions);
    }

    /**
     * navigate using navigation component adding to the back stack
     * @param layout
     */
    public static void navigateWithStackActivity(Activity activity, int layout, Bundle bundle){
        Navigation.findNavController(activity, R.id.fragment).navigate(layout, bundle);
    }

    /**
     * navigate using navigation component without the back stack
     * @param layout
     */
    public static void navigateWithOutStackActivity(Activity activity, int layout, Bundle bundle){

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();

        Navigation.findNavController(activity, R.id.fragment).navigate(layout, bundle, navOptions);

    }

}
