package com.llui.idrink.Utils;

public class HighPrecisionTimer {
    private long startTime;
    protected void setStartTime() {
        startTime = System.nanoTime();
    }
    public long getStartTime() {
        return startTime;
    }
}
