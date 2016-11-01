package com.android.surveillance.detector;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.android.camera.hip.dragonfly.HipCameraService;
import com.android.camera.hip.observer.HipCameraObserver;
import com.android.surveillance.SurveillanceService;
import com.android.surveillance.SurveillanceService.Environment;
import com.android.surveillance.SurveillanceService.ServiceHandler;

public class VideoRecorder extends Recorder implements Detector.DetectorCallback, Worker {
    private static final boolean DEBUG = true;
    private static final String TAG = "Surveillance:VideoRecorder";

    private ServiceHandler mServiceHandler;
    Environment mEnvi;
    private long mIntervalTimestamp;
    public boolean mIsRecording = false;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private int mDelay = 800;
    private int mReclen = 0;
    private int mCameraStatus = HipCameraObserver.EVENT_VIDEO_RECORD_STOP;
    public long timeRecor = 0;
    public VideoRecorder(Context context, Environment envi, ServiceHandler handler) {
        mContext = context;
        mEnvi = envi;
        mServiceHandler = handler;
    }
    @Override
    public void onTriggered(int from) {
        int interval = modeInterval();
        if ( DEBUG ) {
            Log.d(TAG, "onTriggered from:"+from+" mEnvi.mInterval:"+interval+" actual elapsed:"+(System.currentTimeMillis() - mIntervalTimestamp));
        }
        if ( (System.currentTimeMillis() - mIntervalTimestamp) > interval ) {
            start();
            int time = mReclen + mDelay;
            Log.d(TAG,"time:" + time + ",mCameraStatus:" + mCameraStatus);
            if (mCameraStatus == HipCameraObserver.EVENT_VIDEO_RECORD_STARTING) {
                Log.d(TAG,"send:" + time);
                mServiceHandler.removeMessages(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING);
                mServiceHandler.sendMessageDelayed(
                        mServiceHandler
                                .obtainMessage(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING),
                                time);
            }
        }
    }

    private int modeInterval() {
        if (mEnvi.mDetectMode == 0) {
            mReclen = mEnvi.mAudioReclen;
            return mEnvi.mAudioInterval;
        } else {
            mReclen = mEnvi.mMotionReclen;
            return mEnvi.mMotionInterval;
        }
    };

    public void start() {
        if ( DEBUG ) {
            Log.d(TAG, "start() mIsRecording:"+mIsRecording);
        }
        if ( !mIsRecording ) {
            mIsRecording = true;
            if (mWakeLock == null) {
                PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Surveillance:AudioDetector");
                mWakeLock.setReferenceCounted(false);
            }
            mWakeLock.acquire();
            Log.d(TAG, "start record");
            Intent intent = new Intent("com.android.mycamera.command");
            intent.putExtra("command_key", "s_record");
            intent.putExtra("record", true);
            mContext.sendBroadcast(intent);
            timeRecor = System.currentTimeMillis();
        }
    }

    public void stop() {
        if ( DEBUG ) {
            Log.d(TAG, "stop()");
        }
        mServiceHandler.removeMessages(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING);
        if ( mIsRecording ) {
            Log.d(TAG, "stop record");
            mIsRecording = false;
            mIntervalTimestamp = System.currentTimeMillis();
            Intent intent = new Intent("com.android.mycamera.command");
            intent.putExtra("command_key", "s_record");
            intent.putExtra("record", false);
            mContext.sendBroadcast(intent);
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        timeRecor = 0;
    }

    public void finsh() {
        Log.d(TAG, "finsh");
        long time = System.currentTimeMillis() - timeRecor;
        Log.d(TAG, "time :" + time);
        if(time < 3100){
            mServiceHandler.sendEmptyMessageDelayed(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING, 3100);
            return;
        }
        mServiceHandler.removeMessages(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING);
        if (mIsRecording) {
            mIsRecording = false;
            mIntervalTimestamp = 0;
            Intent intent = new Intent("com.android.mycamera.command");
            intent.putExtra("command_key", "s_record");
            intent.putExtra("record", false);
            intent.setClass(mContext, HipCameraService.class);
            mContext.startService(intent);
//            mContext.sendBroadcast(intent);
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        timeRecor = 0;
    }
    @Override
    public void notifyCameraStatus(int status) {
        mCameraStatus = status;
        Log.d(TAG, "notifyCameraStatus ,   status = " + status);
        switch (status) {
            case HipCameraObserver.EVENT_VIDEO_RECORD_START:
                break;
            case HipCameraObserver.EVENT_VIDEO_RECORD_STARTING:
                mServiceHandler.removeMessages(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING);
                mServiceHandler.sendMessageDelayed(
                        mServiceHandler
                                .obtainMessage(SurveillanceService.EVENT_VIDEO_RECODER_ALARMING),
                        mReclen + mDelay);
                break;
            case HipCameraObserver.EVENT_VIDEO_RECORD_STOP:
                mIsRecording = false;
                break;
            case HipCameraObserver.EVENT_VIDEO_RECORD_INIT:
                break;
            default:
                break;
        }
    }

}
