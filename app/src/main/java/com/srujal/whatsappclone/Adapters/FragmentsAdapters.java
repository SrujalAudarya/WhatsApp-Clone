package com.srujal.whatsappclone.Adapters;

import android.provider.CallLog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.srujal.whatsappclone.Fragments.CallsFragment;
import com.srujal.whatsappclone.Fragments.ChatsFragment;
import com.srujal.whatsappclone.Fragments.StatusFragment;

public class FragmentsAdapters extends BaseAdapter {

    public FragmentsAdapters() {

    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
