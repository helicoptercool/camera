package com.android.surveillance.detector;

import android.content.Context;
import android.media.AudioSystem;
import android.os.PowerManager;
import android.util.Log;

import com.android.surveillance.SurveillanceService;
import com.android.surveillance.SurveillanceService.Environment;

public class AudioDetector extends Detector {
    private static final boolean DEBUG = true;
    private static final String TAG = "Surveillance:AudioDetector";
    private final static int LOOK_PERIOD = 250; //ms
    boolean mRunning = false;
    private PowerManager.WakeLock mWakeLock;
    Context mContext;

    public AudioDetector(Environment envi, DetectorCallback cb, Context context) {
        super(envi, cb);
        mContext = context;
    }

    @Override
    public void start() {
        if ( DEBUG ) {
            Log.d(TAG, "start()");
        }

        if ( mRunning ) {
            return;
        }
        mRunning = true;
        AudioRecodeManager.onStartAudioDetection();
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Surveillance:AudioDetector");
            mWakeLock.setReferenceCounted(false);
        }
        mWakeLock.acquire();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while( mRunning ) {
                    AudioSystem.setParameters("AudioDetectSetLevel="+mEnvi.mAudioLevel);
                    AudioSystem.setParameters("AudioDetectEnable=1");
                    String reportStr = AudioSystem.getParameters("AudioDetectReportEvent");
                    if ( DEBUG ) {
                        Log.d(TAG, " reportStr:"+reportStr);
                    }
                    if ( reportStr != null && reportStr.endsWith("1") && mEnvi.mAudioEnable) {
                        mCallback.onTriggered(SurveillanceService.TYPE_AUDIO);
                    }
                    synchronized(this) {
                        try {
                            this.wait(LOOK_PERIOD);
                        } catch (InterruptedException ex) {
                            // run again
                        }
                    }
                }
            }
        }, "AudioDetector.start");
        thread.start();
    }

    @Override
    public void stop() {
        if ( DEBUG ) {
            Log.d(TAG, "stop()");
        }
        mRunning = false;
        try {
            this.notifyAll();
        } catch(IllegalMonitorStateException e) {
        }
        AudioSystem.setParameters("AudioDetectDisable=1");
        AudioRecodeManager.onStopAudioDetection();
        if ( mWakeLock != null ) {
            mWakeLock.release();
        }
    }
}
