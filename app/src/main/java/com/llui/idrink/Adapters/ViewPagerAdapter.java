package com.llui.idrink.Adapters;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.llui.idrink.Fragments.InstructionFragment1;
import com.llui.idrink.Fragments.InstructionFragment2;
import com.llui.idrink.Fragments.InstructionFragment3;
import com.llui.idrink.Fragments.InstructionFragment4;

/**
 * ViewPagerAdapter is an Adapter for the pages of the InstructionActivity.
 * It holds a fragment of instruction.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ViewPager2 viewPager, Button okButton) {
        super(fragmentActivity);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 3) { // Last page index
                    okButton.setVisibility(View.VISIBLE);
                } else {
                    okButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new InstructionFragment1();
            case 1:
                return new InstructionFragment2();
            case 2:
                return new InstructionFragment3();
            case 3:
                return new InstructionFragment4();
        }
        return new InstructionFragment1();
    }

    @Override
    public int getItemCount() {
        return 4; // Number of pages
    }
}
