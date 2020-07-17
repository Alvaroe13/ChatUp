package com.example.alvar.chatapp.Utils;

import android.app.Activity;

public class DrawerStateHelper {

    /**
     * method used to control if drawer can open or not
     * @param activity
     * @param enabled
     */
    public static void drawerEnabled(Activity activity, boolean enabled){
        try {
            ((DrawerLocker)activity).setDrawerLocker(enabled);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
