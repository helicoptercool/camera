package com.android.surveillance.detector;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;

import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.hip.observer.HipCameraObserver;
import com.android.surveillance.SurveillanceService;
import com.android.surveillance.SurveillanceService.Environment;

public class MotionDetector  {//extends Detector{
    private static final boolean DEBUG = true;
    private static final String TAG = "Surveillance:MotionDetector";

    private static final float THRESHOLD_TRIGGER = (float) 0.0;
    private boolean isInitEnable = false;
    private int mPreviewWidth = 1728;
    private int mPreviewHeight = 1728;
    private Context mContext;
    private Environment mEnvi = new Environment();
    private CameraProxy mCameraDevice;
    private Parameters MDParameters;
    public static final String KEY_MOTION_DETECT = "motion_detect";
    public static final String KEY_SENSITIVITY = "sensitivity";

    public MotionDetector(Context context) {
        mContext = context;
    }
    Camera.MotionDetectCallback mMotionDetectCallback = new Camera.MotionDetectCallback() {
        public void onTriggerMotionDetect() {
            Log.d(TAG, "Callback onTriggered");
            if (mEnvi.mMotionEnable) {
                MotionDetector.this.onTriggered();
            }
        };
    };

    public void setDevices(CameraProxy cameraDevice) {
        Log.d(TAG, "setDevices");
        mCameraDevice = cameraDevice;
        if (mCameraDevice == null) {
            return;
        }
        Log.d(TAG, "Set mMotionDetectCallback  mCameraDevice= " + mCameraDevice);
        mCameraDevice.setMotionDetectCallback(mMotionDetectCallback);
//        MDParameters = mCameraDevice.getParameters();
    }

    public void passParameters(Parameters p) {
        Log.d(TAG, "passParameters");
        MDParameters = p;
    }

    public void onTriggered() {
        Intent intent = new Intent(SurveillanceService.INTENT_ACTION_ON_MOTION_TRIGGERED, null, mContext, SurveillanceService.class);
        mContext.startService(intent);
    }

    public void setPreviewSize(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
    }

    public void setIntent(Intent intent) {
        Bundle motdet = intent.getBundleExtra(SurveillanceService.KEYS_MOTDET);
        if (motdet == null) {
            Log.d(TAG, "motdet is null");
            return ;
        } else {
            Log.d(TAG," Bundle auddet = " + motdet.toString());
            mEnvi.mMotionEnable = motdet.getBoolean(SurveillanceService.KEYS_ENABLE, false);
            mEnvi.mMotionLevel = motdet.getInt(SurveillanceService.KEYS_LEVEL, -1);
            Bundle action = motdet.getBundle(SurveillanceService.KEYS_ACTION);
            Log.d(TAG," Bundle action = " + action.toString());
            mEnvi.mMotionAction = action.getInt(SurveillanceService.KEYS_MODE, -1);
            mEnvi.mMotionReclen = action.getInt(SurveillanceService.KEYS_RECLEN, -1);
            mEnvi.mMotionInterval = action.getInt(SurveillanceService.KEYS_INTERVAL, -1);
        }
        Log.d(TAG, "setIntent() enable:"+mEnvi.mMotionEnable+" level:"+mEnvi.mMotionLevel
                + " action:" + mEnvi.mMotionAction + " reclen:" + mEnvi.mMotionReclen + " interval:"
                + mEnvi.mMotionInterval);
        if (mEnvi.mMotionLevel < 1) {
            mEnvi.mMotionLevel = 1;
        } else if (mEnvi.mMotionLevel > 10) {
            mEnvi.mMotionLevel = 10;
        }
        if (mEnvi.mMotionInterval < 1) {
            mEnvi.mMotionInterval = 1000;
        }
        if (MDParameters == null) {
            Log.d(TAG,"MDParameters:null");
            return;
        }
        if (mEnvi.mMotionEnable) {
            Log.d(TAG,"MDParameters:set motion and sensitivity");
            MDParameters.set(KEY_MOTION_DETECT, 1);
            MDParameters.set(KEY_SENSITIVITY, mEnvi.mMotionLevel);
            mCameraDevice.setParameters(MDParameters);
        }else {
            MDParameters.set(KEY_MOTION_DETECT, 2);
            mCameraDevice.setParameters(MDParameters);
        }
    }

    public void setEable(Boolean enable) {
        Log.d(TAG, "setEable ==" + enable);
        if (enable) {
            MDParameters.set(KEY_MOTION_DETECT, 1);
            MDParameters.set(KEY_SENSITIVITY, mEnvi.mMotionLevel);
            mCameraDevice.setParameters(MDParameters);
        } else {
            MDParameters.set(KEY_MOTION_DETECT, 2);
            mCameraDevice.setParameters(MDParameters);
        }
    }


    public boolean isEnable() {
        return mEnvi.mMotionEnable;
    }
//    @Override
//    public void start() {
//        if ( DEBUG ) {
//            Log.d(TAG, "start()");
//        }
//
//    }
//
//    @Override
//    public void stop() {
//        if ( DEBUG ) {
//            Log.d(TAG, "stop()");
//        }
//    }
}
