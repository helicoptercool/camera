package com.android.surveillance;

import com.android.camera.hip.dragonfly.HipCameraService;
import com.android.camera.hip.dragonfly.HipParamters;
import com.android.camera.hip.dragonfly.HipCameraService.LocalBinder;
import com.android.surveillance.DiskMonitor;
import com.android.surveillance.detector.AudioDetector;
import com.android.surveillance.detector.Detector;
import com.android.surveillance.detector.PictureRecorder;
import com.android.surveillance.detector.Recorder;
import com.android.surveillance.detector.VideoRecorder;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

public class SurveillanceService extends Service implements Detector.DetectorCallback{
    private static final boolean DEBUG = true;
    private static final String LOGTAG = "SurveillanceService";

    public static final int TYPE_AUDIO              = 1;
    public static final int TYPE_MOTION             = 2;

    public static final int ACTION_TACKPICTURE      = 0;
    public static final int ACTION_RECVIDEO         = 1;

    public static final int RETCODE_ERROR           = -1;

    public static final int EVENT_SET_DETECT_AUDIO  = 1;
    public static final int EVENT_SET_DETECT_MOTION = 2;
    public static final int EVENT_SET_AUTODEL       = 3;
    public static final int EVENT_VIDEO_RECODER_ALARMING = 4;
    public static final int EVENT_CHECK_MEMORY      = 5;
    public static final int EVENT_TEM_CRITICAL = 6;

    public static final String PATH_PICTURE         = "/picture";
    public static final String PATH_VIDEO           = "/video";
    public static final String VIDEO_FILE_EXTENSION = "mp4";

    public static final String KEYS_GET_AUDIO_DETECT    = "get_audio_detect";
    public static final String KEYS_GET_MOTION_DETECT   = "get_motion_detect";
    public static final String KEYS_GET_AUTO_DEL        = "get_auto_del";
    public static final String KEYS_SET_AUDIO_DETECT    = "set_audio_detect";
    public static final String KEYS_SET_MOTION_DETECT   = "set_motion_detect";

    public static final String KEYS_SET_DETECT_MODE    = "set_detect_mode";
    public static final String KEYS_GET_MOTECT_MODE     = "get_detect_mode";
    public static final String KEYS_SET_AUTO_DEL        = "set_auto_del";
    public static final String INTENT_ACTION_ON_MOTION_TRIGGERED = "intent_action_set_on_triggered";

    public static final String RESULT_ACTION = "com.android.surveillance.command.result";
    public static final String INTENT_ACTION = "com.android.surveillance.command";
    public static final String INTENT_BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
    public static final String INTENT_TEMPERATURE = "com.hip.dragonfly.action.EVENT_CATEMP";
    public static final String KEYS_TYPE        = "key_type";
    public static final String KEYS_ENABLE      = "trigger";
    public static final String KEYS_LEVEL       = "level";
    public static final String KEYS_ACTION      = "action";
    public static final String KEYS_RECLEN      = "reclen";
    public static final String KEYS_INTERVAL    = "interval";
    public static final String KEYS_AUTODEL     = "autdel";

    public static final String KEYS_MODE        = "mode";
    public static final String KEYS_AUDDET      = "auddet";
    public static final String KEYS_MOTDET      = "motdet";
    public static final String KEYS_DETECT_MODE    = "svlmod";

    public static final String PREF_KEY_AUDIO_TRIGGER     = "audio_tigger";
    public static final String PREF_KEY_AUDIO_LEVEL       = "audio_level";
    public static final String PREF_KEY_AUDIO_ACTION      = "audio_action";
    public static final String PREF_KEY_MOTION_TRIGGER    = "motion_tigger";
    public static final String PREF_KEY_MOTION_LEVEL      = "motion_level";
    public static final String PREF_KEY_MOTION_ACTION     = "motion_action";
    public static final String PREF_KEY_AUDIO_RECLEN      = "audio_reclen";
    public static final String PREF_KEY_MOTION_RECLEN     = "motion_reclen";
    public static final String PREF_KEY_AUDIO_INTERVAL    = "audio_interval";
    public static final String PREF_KEY_MOTION_INTERVAL   = "motion_interval";

    public static final String PREF_KEY_AUTDEL            = "autdel";
    public static final String PREF_KEY_MODE              = "detect_mode";
    private int mServiceId = -1;
    private static final int CLIENT_TEM_CRITICAL = 2;
    private Boolean mCatempFlag = false;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private WakeLock mWakeLock;

    public Environment mEnvi = new Environment();

    private AudioDetector mAudioDetector;
//    private MotionDetector mMotionDetector; // implement in HipParamters.java
    private VideoRecorder mVideoRecorder;
    private PictureRecorder mPictureRecoder;
    private DiskMonitor mDiskMonitor;
    private boolean mIsAutoDel;

    private final IBinder mBinder = new LocalBinder();

    public static class Environment {
        public boolean mAudioEnable = false;
        public boolean mMotionEnable = false;
        public int mAudioLevel = 1;
        public int mMotionLevel = 1;
        public int mAudioAction = 0;
        public int mMotionAction = 0;
        public int mAudioReclen = 5000;
        public int mMotionReclen = 5000;
        public int mAudioInterval = 1000;
        public int mMotionInterval = 1000;
        public int mDetectMode  = 0;
        @Override
        public String toString() {
            return "Environment:"+" mAudioEnable:"+mAudioEnable+" mMotionEnable:"+mMotionEnable+" mAudioLevel:"+mAudioLevel
                    +" mMotionLevel:"+mMotionLevel+" mAudioAction:"+mAudioAction+" mMotionAction:"+mMotionAction
                    +" mAudioReclen:"+mAudioReclen+" mMotionReclen:"+mMotionReclen+" mAudioInterval:"+mAudioInterval
                    +"mMotionInterval: "+mMotionInterval +" mDetectMode:"+mDetectMode;
        }
    }

    @Override
    public void onCreate() {
        Log.d(LOGTAG,"onCreate");
        super.onCreate();
        HandlerThread thread = new HandlerThread("SurveillanceService");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mAudioDetector = new AudioDetector(mEnvi, this, this);
//        mMotionDetector = new MotionDetector(mEnvi, this);
        mVideoRecorder = new VideoRecorder(this, mEnvi, mServiceHandler);
        mPictureRecoder = new PictureRecorder(this, mEnvi);
        mDiskMonitor = new DiskMonitor(this, mServiceHandler);
//       init();
    }


    public class LocalBinder extends Binder {
        public SurveillanceService getService() {
            return SurveillanceService.this;
        }
    }
    private void init() {
        Log.d(LOGTAG,"init");
        loadParameter();
        if ( mEnvi.mAudioEnable ) {
            mServiceHandler.sendEmptyMessage(EVENT_SET_DETECT_AUDIO);
        }

        if ( mEnvi.mMotionEnable ) {
            Intent intent = new Intent(INTENT_ACTION);
            intent.putExtra(HipParamters.COM_KEY, KEYS_SET_MOTION_DETECT);
            Bundle action = new Bundle();
            action.putInt(KEYS_MODE, mEnvi.mMotionAction);
            action.putInt(KEYS_RECLEN, mEnvi.mMotionReclen);
            action.putInt(KEYS_INTERVAL, mEnvi.mMotionInterval);
            Bundle motdet = new Bundle();
            motdet.putBoolean(KEYS_ENABLE, mEnvi.mMotionEnable);
            motdet.putInt(KEYS_LEVEL, mEnvi.mMotionLevel);
            motdet.putBundle(KEYS_ACTION, action);
            intent.putExtra(KEYS_MOTDET,motdet);

            mServiceHandler.sendMessage(Message.obtain(mServiceHandler, EVENT_SET_DETECT_MOTION, intent));
        }

        if ( mIsAutoDel ) {
            mServiceHandler.sendEmptyMessage(EVENT_SET_AUTODEL);
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOGTAG,"onStartCommand startId:"+startId+" intent:"+intent);

        mServiceId = startId;
        if ( intent == null || intent.getAction().equals(INTENT_BOOT_ACTION)) {
            init();
            return START_STICKY;
        }
        Log.d(LOGTAG,"mVideoRecorder.mIsRecording resume!" + mEnvi.mAudioEnable + ":" + mEnvi.mMotionEnable);
        if (intent.getAction().equals("com.android.surveillance.detect.short")){
            Log.d(LOGTAG,"mVideoRecorder.mIsRecording resume!");
            mVideoRecorder.mIsRecording = false;
            return START_STICKY;
        }
        String commandKey = intent.getStringExtra(HipParamters.COM_KEY);
        Log.d(LOGTAG,"commandKey = " + commandKey);
        if (KEYS_SET_DETECT_MODE.equals(commandKey) ){
            Bundle svlmode = intent.getExtras();
            if (svlmode == null){
                Log.d(LOGTAG, "svlmode is null");
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            }else{
                setDetectMode(intent);
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            }
        }else if (KEYS_GET_MOTECT_MODE.equals(commandKey)){
            Intent retintent = new Intent(RESULT_ACTION);
            retintent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            Bundle mode = new Bundle();
            mode.putInt(KEYS_DETECT_MODE,mEnvi.mDetectMode);
            retintent.putExtras(mode);
            sendBroadcast(retintent);
        }else if ( KEYS_SET_AUDIO_DETECT.equals(commandKey) ) {
            Bundle auddet = intent.getBundleExtra(KEYS_AUDDET);
            if (auddet == null) {
                Log.d(LOGTAG, "auddet is null");
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            } else {
                setDetectMode(intent);
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            }
        } else if ( KEYS_SET_MOTION_DETECT.equals(commandKey) ) {
            Bundle motdet = intent.getBundleExtra(KEYS_MOTDET);
            if (motdet == null) {
                Log.d(LOGTAG, "motdet is null");
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            } else {
                setDetectMode(intent);
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            }
        } else if ( KEYS_SET_AUTO_DEL.equals(commandKey) ) {
            setAutoDelete(intent.getBooleanExtra(KEYS_AUTODEL, false));
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        } else if ( INTENT_ACTION_ON_MOTION_TRIGGERED.equals(intent.getAction()) ) {
            onTriggered(TYPE_MOTION);
        } else if ( KEYS_GET_AUDIO_DETECT .equals(commandKey) ) {
            Intent retintent = new Intent(RESULT_ACTION);
            retintent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            Bundle action = new Bundle();
            action.putInt(KEYS_MODE, mEnvi.mAudioAction);
            action.putInt(KEYS_RECLEN, mEnvi.mAudioReclen);
            action.putInt(KEYS_INTERVAL, mEnvi.mAudioInterval);
            Bundle auddet = new Bundle();
            auddet.putBoolean(KEYS_ENABLE, mEnvi.mAudioEnable);
            auddet.putInt(KEYS_LEVEL, mEnvi.mAudioLevel);
            auddet.putBundle(KEYS_ACTION, action);
            retintent.putExtra(KEYS_AUDDET,auddet);
            sendBroadcast(retintent);

        } else if ( KEYS_GET_MOTION_DETECT.equals(commandKey) ) {
            Intent retintent = new Intent(RESULT_ACTION);
            retintent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            Bundle action = new Bundle();
            action.putInt(KEYS_MODE, mEnvi.mMotionAction);
            action.putInt(KEYS_RECLEN, mEnvi.mMotionReclen);
            action.putInt(KEYS_INTERVAL, mEnvi.mMotionInterval);
            Bundle motdet = new Bundle();
            motdet.putBoolean(KEYS_ENABLE, mEnvi.mMotionEnable);
            motdet.putInt(KEYS_LEVEL, mEnvi.mMotionLevel);
            motdet.putBundle(KEYS_ACTION, action);
            retintent.putExtra(KEYS_MOTDET,motdet);
            sendBroadcast(retintent);

        } else if ( KEYS_GET_AUTO_DEL.equals(commandKey) ) {
            sendIntent(KEYS_AUTODEL, mIsAutoDel);
        } else if ( INTENT_ACTION_ON_MOTION_TRIGGERED.equals(intent.getAction()) ) {
            onTriggered(TYPE_MOTION);
        } else if (intent.getAction().equals(INTENT_TEMPERATURE)) {
            mCatempFlag = (intent.getIntExtra("catemp", -1) == CLIENT_TEM_CRITICAL);
            Log.d(LOGTAG, "mCatempFlag = " + mCatempFlag);
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(EVENT_TEM_CRITICAL));

        } else {
            Log.w(LOGTAG, "unkow action:"+commandKey);
        }
        return START_STICKY;//super.onStartCommand(intent, flags, startId);
    }

    private void sendIntent(String key, String value) {
        Log.d(LOGTAG,"sendIntent: key:"+key+":"+value);
        Intent intent = new Intent(RESULT_ACTION);
        intent.putExtra(key, value);
        sendBroadcast(intent);
    }

    private void sendIntent(String key, boolean value) {
        Log.d(LOGTAG,"sendIntent: key:"+key+":"+value);
        Intent intent = new Intent(RESULT_ACTION);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(key, value);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(LOGTAG,"onDestroy");
        mAudioDetector.stop();
//        mMotionDetector.stop();
        mVideoRecorder.stop();
        mPictureRecoder.stop();
        mDiskMonitor.stop();
        saveParameter();
        super.onDestroy();
    }

    public void onStart(Intent intent, int startId) {
        if (DEBUG) Log.d(LOGTAG,"onStart startId:"+startId);
        super.onStart(intent, startId);
        mServiceId = startId;
    }

    @Override
    public IBinder onBind(Intent intent) {
//        if (DEBUG) Log.d(LOGTAG,"onBind() mBinder:"+mBinder);
//        mBinded = true;
        return mBinder;
//        return null;
    }

    boolean mIsEVENT_SET_DETECT_MOTION_ACTIVE = false;
    public final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "Handling incoming message: " + msg + " = " + msg.what);

            switch (msg.what) {
                case EVENT_SET_DETECT_AUDIO:
                    if ( mEnvi.mAudioEnable ) {
                        mAudioDetector.start();
                    } else {
                        mAudioDetector.stop();
                        mVideoRecorder.finsh();
                    }
                    break;

                case EVENT_SET_DETECT_MOTION:
                    Log.d(LOGTAG,"Envi.mMotionEnable = " + mEnvi.mMotionEnable);
                    if(mEnvi.mMotionEnable){
                        mIsEVENT_SET_DETECT_MOTION_ACTIVE =true;
                    } else {
                        if(!mIsEVENT_SET_DETECT_MOTION_ACTIVE ){
                            mIsEVENT_SET_DETECT_MOTION_ACTIVE = false;
                            return;
                        }
                    }
                    Intent intent  =new Intent(HipParamters.ENNABLE_DETECT_ACTION);
                    Intent in = (Intent)msg.obj;
                    intent.putExtra("enable", mEnvi.mMotionEnable);
                    intent.putExtra(KEYS_MOTDET,in.getBundleExtra(KEYS_MOTDET));
                    intent.setClass(getApplication(), HipCameraService.class);
                    startService(intent);
                    if (!mEnvi.mMotionEnable){
                        mVideoRecorder.finsh();
                    }
                    break;

                case EVENT_SET_AUTODEL:
                    Log.d(LOGTAG,"mIsAutoDel = " + mIsAutoDel);
                    if ( mIsAutoDel ) {
                        mDiskMonitor.start();
                    } else {
                        mDiskMonitor.stop();
                    }
                    break;

                case EVENT_VIDEO_RECODER_ALARMING:
                    mVideoRecorder.stop();
                    break;

                case EVENT_CHECK_MEMORY:
                    mDiskMonitor.checkMemory();
                    break;
                case EVENT_TEM_CRITICAL:
                    Log.d(LOGTAG, "mCatempFlag:" + mCatempFlag + ",mEnvi.mAudioEnable= "
                            + mEnvi.mAudioEnable
                            + ",mEnvi.mMotionEnable= " + mEnvi.mMotionEnable);
                    if (mCatempFlag) {
                        if (mEnvi.mAudioEnable) {
                            mAudioDetector.stop();
                        }
                        if (mEnvi.mMotionEnable){
                            Intent mIntent = new Intent(HipParamters.ENNABLE_DETECT_ACTION);
                            mIntent.putExtra("catemp", 0);        //  Temp High turn off motion
                            mIntent.setClass(getApplication(), HipCameraService.class);
                            startService(mIntent);
                        }
                    } else if (mEnvi.mAudioEnable) {
                        mAudioDetector.start();
                    }else if (mEnvi.mMotionEnable)
                    {
                        Intent mIntent = new Intent(HipParamters.ENNABLE_DETECT_ACTION);
                        mIntent.putExtra("catemp", 1);         //  Temp normal turn on motion
                        SurveillanceService.this.sendBroadcast(mIntent);
                    }
                    break;
            }
        }
    }

    @Override
    public void onTriggered(int from) {
        if ( DEBUG ) {
            Log.d(LOGTAG, "onTriggered("+from+")");
        }
        if ( from == TYPE_AUDIO ) {
            if ( mEnvi.mAudioAction == ACTION_TACKPICTURE ) {
                mPictureRecoder.onTriggered(from);
            } else if ( mEnvi.mAudioAction == ACTION_RECVIDEO ) {
                mVideoRecorder.onTriggered(from);
            }
        } else { // Motion
            if ( mEnvi.mMotionAction == ACTION_TACKPICTURE ) {
                mPictureRecoder.onTriggered(from);
            } else if ( mEnvi.mMotionAction == ACTION_RECVIDEO ) {
                mVideoRecorder.onTriggered(from);
            }
        }
    }

    public void setDetectMode(Intent intent) {
        String commandKey = intent.getStringExtra(HipParamters.COM_KEY);
        if (KEYS_SET_DETECT_MODE.equals(commandKey)) {
            Bundle svlmode = intent.getExtras();
            mEnvi.mDetectMode = svlmode.getInt(KEYS_DETECT_MODE, 0);
        } else if (KEYS_SET_AUDIO_DETECT.equals(commandKey)) {
            Bundle auddet = intent.getBundleExtra(KEYS_AUDDET);
            Bundle action = auddet.getBundle(KEYS_ACTION);
            mEnvi.mAudioEnable = auddet.getBoolean(KEYS_ENABLE, false);
            int audioLevel = auddet.getInt(KEYS_LEVEL, 1);
            mEnvi.mAudioLevel = audioLevel > 0 ? audioLevel : 1;
            int audioAction = action.getInt(KEYS_MODE, 0);
            mEnvi.mAudioAction = audioAction > 0 ? audioAction : 0;
            int AudioReclen = action.getInt(KEYS_RECLEN, 5000);
            mEnvi.mAudioReclen = AudioReclen > 0 ? AudioReclen : 5000;
            int audioInterval = action.getInt(KEYS_INTERVAL, 1000);
            mEnvi.mAudioInterval = audioInterval > 0 ? audioInterval : 1000;
//            mEnvi.mAudioEnable = true;
//            mEnvi.mAudioLevel = 6;
//            mEnvi.mAudioAction = 0;
//            mEnvi.mReclen = 20000;
//            mEnvi.mInterval = 5000;
            mServiceHandler.sendEmptyMessage(EVENT_SET_DETECT_AUDIO);
        } else if ( KEYS_SET_MOTION_DETECT.equals(commandKey) ) {
            Bundle motdet = intent.getBundleExtra(KEYS_MOTDET);
            Bundle action = motdet.getBundle(KEYS_ACTION);
            mEnvi.mMotionEnable = motdet.getBoolean(KEYS_ENABLE, false);
            int motionLevel = motdet.getInt(KEYS_LEVEL, 1);
            mEnvi.mMotionLevel = motionLevel > 0 ? motionLevel : 1;
            int motionAction = action.getInt(KEYS_MODE, 1);
            mEnvi.mMotionAction = motionAction > 0 ? motionAction : 0;
            int motionReclen = action.getInt(KEYS_RECLEN, 5000);
            mEnvi.mMotionReclen = motionReclen > 0 ? motionReclen : 5000;
            int motionInterval = action.getInt(KEYS_INTERVAL, 1000);
            mEnvi.mMotionInterval = motionInterval > 0 ? motionInterval : 1000;
            mServiceHandler.sendMessage(Message.obtain(mServiceHandler, EVENT_SET_DETECT_MOTION, intent));
        } else {
            Log.w(LOGTAG, "Unsupported type");
        }
        Log.d(LOGTAG, "setDetectMode mEnvi:"+mEnvi);
        saveParameter();
    }

    public void setAutoDelete(boolean autodel) {
        Log.d(LOGTAG, "setAutoDelete:"+autodel);
        mIsAutoDel = autodel;
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(EVENT_SET_AUTODEL));
        saveParameter();
    }

    private void saveParameter() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_KEY_AUDIO_TRIGGER, mEnvi.mAudioEnable);
        editor.putBoolean(PREF_KEY_MOTION_TRIGGER, mEnvi.mMotionEnable);
        editor.putInt(PREF_KEY_AUDIO_LEVEL, mEnvi.mAudioLevel);
        editor.putInt(PREF_KEY_MOTION_LEVEL, mEnvi.mMotionLevel);
        editor.putInt(PREF_KEY_AUDIO_ACTION, mEnvi.mAudioAction);
        editor.putInt(PREF_KEY_MOTION_ACTION, mEnvi.mMotionAction);
        editor.putInt(PREF_KEY_AUDIO_RECLEN, mEnvi.mAudioReclen);
        editor.putInt(PREF_KEY_MOTION_RECLEN, mEnvi.mMotionReclen);
        editor.putInt(PREF_KEY_AUDIO_INTERVAL, mEnvi.mAudioInterval);
        editor.putInt(PREF_KEY_MOTION_INTERVAL, mEnvi.mMotionInterval);
        editor.putBoolean(PREF_KEY_AUTDEL, this.mIsAutoDel);
        editor.putInt(PREF_KEY_MODE,mEnvi.mDetectMode);
        editor.apply();
        if ( DEBUG ) {
            Log.d(LOGTAG, "saveParameter() mEnvi:"+mEnvi);
        }
    }

    private void loadParameter() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mEnvi.mAudioEnable = pref.getBoolean(PREF_KEY_AUDIO_TRIGGER, false);
        mEnvi.mMotionEnable = pref.getBoolean(PREF_KEY_MOTION_TRIGGER, false);
        mEnvi.mAudioLevel = pref.getInt(PREF_KEY_AUDIO_LEVEL, 1);
        mEnvi.mMotionLevel = pref.getInt(PREF_KEY_MOTION_LEVEL, 1);
        mEnvi.mAudioAction = pref.getInt(PREF_KEY_AUDIO_ACTION, 0);
        mEnvi.mMotionAction = pref.getInt(PREF_KEY_MOTION_ACTION, 0);
        mEnvi.mAudioReclen = pref.getInt(PREF_KEY_AUDIO_RECLEN, 5000);
        mEnvi.mMotionReclen = pref.getInt(PREF_KEY_MOTION_RECLEN, 5000);
        mEnvi.mAudioInterval = pref.getInt(PREF_KEY_AUDIO_INTERVAL, 1000);
        mEnvi.mMotionInterval = pref.getInt(PREF_KEY_MOTION_INTERVAL, 1000);
        mIsAutoDel = pref.getBoolean(PREF_KEY_AUTDEL, false);
        mEnvi.mDetectMode = pref.getInt(PREF_KEY_MODE,0);
        if ( DEBUG ) {
            Log.d(LOGTAG, "loadParameter() mEnvi:"+mEnvi);
        }
    }

    private synchronized void createWakeLock() {
        // Create a new wake lock if we haven't made one yet.
        Log.d(LOGTAG, "createWakeLock mWakeLock:"+mWakeLock);
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SurveillanceService");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        // It's okay to double-acquire this because we are not using it
        // in reference-counted mode.
        Log.v(LOGTAG, "acquireWakeLock");
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        // Don't release the wake lock if it hasn't been created and acquired.
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Log.v(LOGTAG, "releaseWakeLock");
            mWakeLock.release();
        }
    }

    public Recorder getHipCameraObserver() {
        Log.v(LOGTAG, "-- + mEnvi.mMotionAction = " + mEnvi.mMotionAction
                + " , mEnvi.mDetectMode = " + mEnvi.mDetectMode);
        /*if (mEnvi.mDetectMode == 0) {
            return null;
        }*/
        if (mEnvi.mMotionEnable ) {
            return mEnvi.mMotionAction == 0 ? mPictureRecoder : mVideoRecorder;
        }
        if (mEnvi.mAudioEnable ) {
            return mEnvi.mAudioAction  == 0 ? mPictureRecoder : mVideoRecorder;
        }
        return null;
    }
}
