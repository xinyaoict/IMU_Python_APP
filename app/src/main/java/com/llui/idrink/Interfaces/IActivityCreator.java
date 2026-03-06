package com.llui.idrink.Interfaces;
/**
 * Interface defining methods for initializing, setting button listeners, and binding views.
 * Used in BaseActivity
 */
public interface IActivityCreator {
    void init();
    void listenBtn();
    void setBinding();
}
