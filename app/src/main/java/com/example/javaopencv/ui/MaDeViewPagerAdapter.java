package com.example.javaopencv.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MaDeViewPagerAdapter extends FragmentStateAdapter {

    private MaDeTabFragment maDeTabFragment;
    private DapAnTabFragment dapAnTabFragment;
    private final Bundle parentArgs;

    public MaDeViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        parentArgs = fragment.getArguments() != null ? fragment.getArguments() : new Bundle();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            maDeTabFragment = new MaDeTabFragment();
            maDeTabFragment.setArguments(new Bundle(parentArgs));
            return maDeTabFragment;
        } else {
            dapAnTabFragment = new DapAnTabFragment();
            dapAnTabFragment.setArguments(new Bundle(parentArgs));
            return dapAnTabFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public MaDeTabFragment getMaDeTabFragment() {
        return maDeTabFragment;
    }

    public DapAnTabFragment getDapAnTabFragment() {
        return dapAnTabFragment;
    }
}
