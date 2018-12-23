package com.example.alvar.chatapp.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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

