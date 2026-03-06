package com.llui.idrink.Utils;

import android.animation.TimeAnimator;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.Button;

import com.llui.idrink.R;

public class ScanButtonAnimator implements TimeAnimator.TimeListener {
    private static final int MAX_LEVEL = 10000; // The maximum level for the ClipDrawable
    private final int animationDuration;
    private ClipDrawable mClipDrawable;
    private Button scanButton;
    private TimeAnimator mAnimator;
    private int mCurrentLevel;

    public ScanButtonAnimator(Button button, int animationDuration) {
        this.animationDuration = animationDuration;
        this.scanButton = button;
        LayerDrawable layerDrawable = (LayerDrawable) button.getBackground();
        this.mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);
        this.mAnimator = new TimeAnimator();
        this.mAnimator.setTimeListener(this);
        this.mCurrentLevel = 0;
    }

    public void startAnimation() {
        mCurrentLevel = 0;
        scanButton.setEnabled(false);
        scanButton.setText(R.string.scanning);
        mAnimator.start();
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        if (totalTime >= animationDuration) {
            mClipDrawable.setLevel(MAX_LEVEL);
            mAnimator.cancel();
            scanButton.setEnabled(true);
            scanButton.setText(R.string.scan);
        } else {
            mCurrentLevel = (int) ((totalTime / (float) animationDuration) * MAX_LEVEL);
            mClipDrawable.setLevel(mCurrentLevel);
        }
    }
}
