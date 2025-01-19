package com.srujal.whatsappclone.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.srujal.whatsappclone.Fragments.CallsFragment;
import com.srujal.whatsappclone.Fragments.ChatsFragment;
import com.srujal.whatsappclone.Fragments.StatusFragment;

public class FragmentsAdapters extends FragmentPagerAdapter {

    public FragmentsAdapters(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
            case 0:
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}
