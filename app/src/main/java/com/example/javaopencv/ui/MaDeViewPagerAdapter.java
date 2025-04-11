package com.example.javaopencv.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MaDeViewPagerAdapter extends FragmentStateAdapter {
    private MaDeTabFragment maDeTabFragment;
    private DapAnTabFragment dapAnTabFragment;
    private final Bundle parentArgs;

    public MaDeViewPagerAdapter(@NonNull Fragment fragment, Bundle args) {
        super(fragment);
        // Sử dụng trực tiếp Bundle truyền vào thay vì tạo bản sao mới,
        // điều này đảm bảo rằng các giá trị ban đầu (như questionCount) sẽ không bị thay đổi.
        parentArgs = (args != null) ? args : new Bundle();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            if (maDeTabFragment == null) {
                maDeTabFragment = new MaDeTabFragment();
                maDeTabFragment.setArguments(parentArgs);
            }
            return maDeTabFragment;
        } else {
            if (dapAnTabFragment == null) {
                dapAnTabFragment = new DapAnTabFragment();
                dapAnTabFragment.setArguments(parentArgs);
            }
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
