package com.llui.idrink.Interfaces;
/**
 * Interface definition for callbacks to be invoked when various actions are performed on measurements.
 * Implement this interface to handle click events on measurement items.
 */
public interface OnMeasurementClickListener {
        void onMeasurementClick(int position);
        void onPlusMeasurementClick(int position);
        void onCommentClick(int position);
}
