package com.example.alvar.chatapp.Utils;

/**
 * this interface will work as a connection between activity and fragments to keep control over
 * drawer state (open/close) and pass userID when user logs in
 */
public interface DrawerLayoutHelper {

    void setDrawerLocker(boolean enabled);

    void passUserID(String userID);
}
