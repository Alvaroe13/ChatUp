package com.example.alvar.chatapp.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.ContactsFragment;
import com.example.alvar.chatapp.Fragments.RequestsFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "ViewPagerAdapter";


    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        //Inicializamos los fragments del viewPager
        switch (position){
            case 0:
                Log.i(TAG, " requestsFragment ");
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 1:
                Log.i(TAG, " chatsFragment ");
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                Log.i(TAG, " contactsFragment ");
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
             default:
                 return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
