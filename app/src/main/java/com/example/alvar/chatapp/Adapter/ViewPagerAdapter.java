package com.example.alvar.chatapp.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.ContactsFragment;
import com.example.alvar.chatapp.Fragments.RequestsFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "ViewPagerAdapter";

    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> fragmentTitle = new ArrayList<>();

    /**
     * constructor
     * @param fm
     */
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     *  Position of fragment
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {

//        //Inicializamos los fragments del viewPager
//        switch (position){
//                case 0:
//                Log.i(TAG, " requestsFragment ");
//                RequestsFragment requestsFragment = new RequestsFragment();
//                return requestsFragment;
//                case 1:
//                Log.i(TAG, " chatsFragment ");
//                ChatsFragment chatsFragment = new ChatsFragment();
//                return chatsFragment;
//                case 2:
//                Log.i(TAG, " contactsFragment ");
//                ContactsFragment contactsFragment = new ContactsFragment();
//                return contactsFragment;
//            default:
//        return null;
//        }
        return fragmentList.get(position);
    }

    /**
     * we save in this method the size of fragment
     * @return
     */
    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitle.get(position);
    }

    /**
     * method in charge of adding a new fragment!
     * @param fragment
     */
    public void addFragment(Fragment fragment, String title ){
        fragmentList.add(fragment);
        fragmentTitle.add(title);

    }

}


//
