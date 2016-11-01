package com.android.surveillance.detector;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecodeManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "Surveillance:AudioRecodeManager";

    private AudioRecord mAudioRecord;
    private static final int mHz = 8000;
    private static final int BUFFER_SIZE = 102400;
    private boolean mRunning = false;
    private final static int LOOK_PERIOD = 50; //ms
    private Object mWaitLock = new Object();
    private boolean mIsInPreview = false;
    private boolean mIsInAudioDetection = false;
    private boolean mEverStop = true;

    private static AudioRecodeManager mAudioRecodeManager;
    private AudioRecodeManager() {
    }

    private static synchronized AudioRecodeManager getInstance() {
        if ( mAudioRecodeManager == null ) {
            mAudioRecodeManager = new AudioRecodeManager();
        }
        return mAudioRecodeManager;
    }

    public String toString() {
        return "mIsInPreview:"+mIsInPreview+" mIsInAudioDetection:"+mIsInAudioDetection+" mEverStop:"+mEverStop+" mAudioRecord:"+mAudioRecord;
    }

    private void beforePreviewStart() {
        if ( DEBUG ) {
            Log.d(TAG, "beforePreviewStart() "+this);
        }
        if ( mIsInPreview ) {
            return;
        }
        if ( mAudioRecord != null ) {
            stopAudioRecode();
        }
        mIsInPreview = true;
    }

    private void afterPreviewStop() {
        if ( DEBUG ) {
            Log.d(TAG, "afterPreviewStop() "+this);
        }
        mIsInPreview = false;
        if ( mIsInAudioDetection ) {
            startAudioRecode();
        }
    }

    private void startAudioDetection() {
        if ( DEBUG ) {
            Log.d(TAG, "startAudioDetection() "+this);
        }
        mIsInAudioDetection = true;
        startAudioRecode();
    }

    private void startAudioRecode() {
        if ( DEBUG ) {
            Log.d(TAG, "startAudioRecode() "+this);
        }

        if ( mIsInPreview || mAudioRecord != null ) {
            return;
        }

        if ( mAudioRecord == null ) {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, mHz,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecord.getMinBufferSize(mHz,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT) * 10);
        }
        final byte[] byteData = new byte[BUFFER_SIZE];
        mAudioRecord.startRecording();
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRunning = true;
                while( mRunning ) {
                    int ret = mAudioRecord.read(byteData, 0, BUFFER_SIZE);
                    if ( ret == AudioRecord.ERROR_INVALID_OPERATION || ret == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, " ret val for read() is:"+ret);
                    }
                    synchronized(mWaitLock) {
                        try {
                            mWaitLock.wait(LOOK_PERIOD);
                        } catch (InterruptedException ex) {
                            // run again
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private void stopAudioDetection() {
        if ( DEBUG ) {
            Log.d(TAG, "stopAudioDetection() "+this);
        }
        stopAudioRecode();
        mIsInAudioDetection = false;
    }

    private void stopAudioRecode() {
        if ( DEBUG ) {
            Log.d(TAG, "stopAudioRecode() "+this);
        }
        
        mRunning = false;
        try {
            mWaitLock.notifyAll();
        } catch(IllegalMonitorStateException e) {
        }
        if ( mAudioRecord != null ) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            mEverStop = true;
        }
    }

    public static void onBeforePreviewStart() {
        getInstance().beforePreviewStart();
    }

    public static void onAfterPreviewStop() {
        getInstance().afterPreviewStop();
    }

    public static void onStartAudioDetection() {
        getInstance().startAudioDetection();
    }

    public static void onStopAudioDetection() {
        getInstance().stopAudioDetection();
    }

    public static boolean getEverStop() {
        if ( getInstance().mEverStop ) {
            if ( DEBUG ) {
                Log.d(TAG, "getEverStop() is true");
            }
            getInstance().mEverStop = false;
            return true;
        }
        return false;
    }
}
