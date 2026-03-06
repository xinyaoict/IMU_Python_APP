package com.llui.idrink.Recorders;

import android.util.Log;
import java.util.concurrent.CountDownLatch;

/**
 * This class manages the synchronization of multiple recording processes.
 * It provides a mechanism to initialize and retrieve a RecordingSynchronizer,
 * which uses a CountDownLatch to coordinate the start of recording across
 * multiple recorders.
 */
public class RecordingSynchronizerManager {
    private static final String TAG = "RecordingSynchronizerManager";
    private static RecordingSynchronizer synchronizer;

    public static synchronized void initialize(int numRecorders) {
        if (synchronizer == null || synchronizer.getNumRecorders() != numRecorders) {
            synchronizer = new RecordingSynchronizer(numRecorders);
            Log.i(TAG, "Initialized RecordingSynchronizer with " + numRecorders + " recorders.");
        } else {
            Log.w(TAG, "RecordingSynchronizer already initialized with " + synchronizer.getNumRecorders() + " recorders.");
            synchronizer.initLatch();
        }
    }

    public static RecordingSynchronizer getSynchronizer() {
        if (synchronizer == null) {
            Log.e(TAG, "Synchronizer not initialized. Call initialize() first.");
            throw new IllegalStateException("Synchronizer not initialized. Call initialize() first.");
        }
        return synchronizer;
    }

    public static void registerObserver(RecordingObserver observer) {
        synchronizer.registerObserver(observer);
        Log.d(TAG, "Observer registered: " + observer.getClass().getName());
    }
    public static class RecordingSynchronizer {
        private final int numRecorders;
        private CountDownLatch latch;
        private RecordingObserver observer;

        public RecordingSynchronizer(int numRecorders) {
            this.numRecorders = numRecorders;
            initLatch();
            Log.i(TAG, "RecordingSynchronizer created with " + numRecorders + " recorders.");
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public int getNumRecorders() {
            return numRecorders;
        }

        public synchronized void registerObserver(RecordingObserver observer) {
            this.observer = observer;
            Log.d(TAG, "Observer added: " + observer.getClass().getName());
        }

        private synchronized void notifyObserver() {
            if (observer != null) {
                observer.onAllRecordersReady();
                Log.d(TAG, "Observer notified: " + observer.getClass().getName());
            }
        }

        public void countDown() {
            latch.countDown();
            Log.d(TAG, "CountDownLatch decremented. Remaining count: " + latch.getCount());
            if (latch.getCount() == 0) {
                Log.i(TAG, "All recorders are ready. Notifying observer.");
                notifyObserver();
            }
        }

        private void initLatch() {
            latch = new CountDownLatch(numRecorders);
        }
    }

    public interface RecordingObserver {
        void onAllRecordersReady();
    }
}
