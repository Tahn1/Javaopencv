package com.example.javaopencv.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.javaopencv.ui.DapAnTabFragment;
import com.example.javaopencv.ui.MaDeTabFragment;

public class MaDeViewPagerAdapter extends FragmentStateAdapter {

    private MaDeTabFragment maDeTabFragment;
    private DapAnTabFragment dapAnTabFragment;

    public MaDeViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        maDeTabFragment = new MaDeTabFragment();
        dapAnTabFragment = new DapAnTabFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return maDeTabFragment;
        } else {
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
