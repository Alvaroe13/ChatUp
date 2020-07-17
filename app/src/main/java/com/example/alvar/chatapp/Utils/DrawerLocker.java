package com.example.alvar.chatapp.Utils;

/**
 * this interface will work as a connection between activity and fragments to keep control over
 * drawer state (open/close)
 */
public interface DrawerLocker {

    void setDrawerLocker(boolean enabled);
}
