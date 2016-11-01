package com.android.surveillance.detector;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.camera.hip.dragonfly.HipCameraService;
import com.android.surveillance.SurveillanceService.Environment;

public class PictureRecorder extends Recorder implements Detector.DetectorCallback, Worker {
    private static final boolean DEBUG = true;
    private static final String TAG = "Surveillance:PictureRecorder";

    Environment mEnvi;
    private long mIntervalTimestamp;
    private Context mContext;

    public PictureRecorder(Context context, Environment envi) {
        mContext = context;
        mEnvi = envi;
    }

    @Override
    public void onTriggered(int from) {
        int interval = modeInterval();
        if ( DEBUG ) {
            Log.d(TAG, "onTriggered from:"+from+" mEnvi.mInterval:"+interval+" actual elapsed:"+(System.currentTimeMillis() - mIntervalTimestamp));
        }
        if ((System.currentTimeMillis() - mIntervalTimestamp) > interval) {
            mIntervalTimestamp = System.currentTimeMillis();
            tackPicture();
        }
    }

    private void tackPicture() {
        if ( DEBUG ) {
            Log.d(TAG, "tackPicture");
        }
        Intent intent = new Intent("com.android.mycamera.command");
        intent.putExtra("command_key", "s_take_picture");
        //intent.setClass(mContext, HipCameraService.class);
        //mContext.startService(intent);
        mContext.sendBroadcast(intent);
    }

    private int modeInterval() {
        if (mEnvi.mDetectMode == 0) {
            return mEnvi.mAudioInterval;
        } else {
            return mEnvi.mMotionInterval;
        }
    };

    @Override
    public void start() {
        if ( DEBUG ) {
            Log.d(TAG, "start()");
        }
    }

    @Override
    public void stop() {
        if ( DEBUG ) {
            Log.d(TAG, "stop()");
        }
        mIntervalTimestamp = 0;
    }

    @Override
    public void notifyCameraStatus(int status) {
        
    }

}
