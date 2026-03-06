package com.llui.idrink.Activities;

import android.content.Intent;
import android.view.LayoutInflater;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.llui.idrink.Adapters.ViewPagerAdapter;
import com.llui.idrink.R;
import com.llui.idrink.databinding.ActivityInstructionBinding;

/**
 * InstructionActivity is an activity that displays the basic instructions for gait assessment.
 * It is automatically displayed when the app is used for the first time.
 * It can also be displayed by clicking a help button on the LoginActivity
 */
public class InstructionActivity extends BaseActivity{
    private ActivityInstructionBinding binding;
    private boolean isForcedToView;

    @Override
    public void init() {
        initPages();
    }
    private void initPages() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, viewPager,binding.okButton);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                // Ensure the position is converted to a string for setText
                tab.setText(String.valueOf(position + 1));
            }).attach();
    }
    @Override
    public void listenBtn() {
        listenOKBtn();
    }
    private void listenOKBtn() {
        binding.okButton.setOnClickListener(v -> {
            if (isForcedToView) {
                navigateToNextActivity(MenuActivity.class);
            } else {
                navigateToNextActivity(LoginActivity.class);
            }
        });
    }

    @Override
    public void setBinding() {
        binding = ActivityInstructionBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }
    @Override
    public void processReceivedIntent(Intent intent) {
        isForcedToView = intent.getBooleanExtra("isForcedToView", false);
    }
}
