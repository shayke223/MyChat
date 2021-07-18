package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * The class is extending the FragmentPagerAdapter in order to set the behavior of the pages
 * Must create a class like this if we want to use fragments.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Get a number of tab and returns the associated fragment(New class that I've created)
     * @param position Which one of the tabs
     * @return
     */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position)
        {
            case 0: return new RequestsFragment();
            case 1: return new ChatsFragment();
            case 2: return new FriendsFragment();
            default: return null;
        }
    }

    /**
     *
     * @return number of fragments
     */
    @Override
    public int getCount() {
        return 3; //number of tabs
    }
    /**
     * For each fragment, define the title.
     * @param position
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position)
        {
            case 0: return "Requests";
            case 1: return "Chats";
            case 2: return "Friends";
            default: return null;
        }

    }
}
