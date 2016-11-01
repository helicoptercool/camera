package com.android.surveillance.detector;

import com.android.surveillance.SurveillanceService.Environment;

public abstract class Detector implements Worker {
    Environment mEnvi;
    DetectorCallback mCallback;

    public interface DetectorCallback {
        void onTriggered(int from);
    }

    public Detector(Environment envi, DetectorCallback cb) {
        mEnvi = envi;
        mCallback = cb;
    }
}
