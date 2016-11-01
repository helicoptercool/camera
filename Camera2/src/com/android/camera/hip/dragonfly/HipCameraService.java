
package com.android.camera.hip.dragonfly;

import android.app.CameraLed;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

import com.android.camera.CameraLog;
import com.android.camera.app.CameraApp;

/**
 * @author duzc
 */
public class HipCameraService extends Service {

    private static String TAG = "HipCameraService";
    private static String CAMERA_PACKAGE_NAME = "com.android.camera2";
    //private static String CAMERA_HIP_ACTIVITY = "com.android.camera.hip.dragonfly.HipCameraActivity";
    private static String CAMERA_HIP_ACTIVITY = "com.android.mycamera.MyCamera";

    private final IBinder mBinder = new LocalBinder();
    private Listener mListener;
    private ServiceHandler mServiceHandler;

    private Looper mServiceLooper;
    private Intent mPendingAction;
    private boolean mIsEnableAction = true;
    private boolean mIsEnableActionRecord = true;
    private final CameraHandler mCameraHandler = new CameraHandler();
    private CameraLed mCameraLed = new CameraLed();
    private static boolean isLed_EVENT_ONREADY = false;
    private CameraApp camApp;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public HipCameraService getService() {
            return HipCameraService.this;
        }
    }

    @Override
    public void onCreate() {
        CameraLog.d(TAG, "HipCameraService onCreate");
        camApp = (CameraApp) this.getApplication();
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_DISPLAY);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        CameraLog.d(TAG, "HipCameraService onStartCommand");
        if (intent != null && intent.getAction() != null) {
            CameraLog.v(TAG, "intent: #" + intent.getAction() + " flag: " + flag + " startId:"
                    + startId);
        }
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        CameraLog.d(TAG, " onDestroy ");
    }

    public void setListener(Listener l) {
        mListener = l;
        CameraLog.d(TAG, "HipCameraService Listener:" + mListener);
    }

    public void setCamereaHandler(Handler h) {
        CameraLog.d(TAG, "HipCameraService setCamereaHandler!");
    }

    public void callBack(String command, boolean fail, int error) {
        boolean isAutoPreivew = HipSettingPreferences.getAutoPreview(this);
        boolean isMotionD = HipSettingPreferences.getMotionDetector(this);
        CameraLog.d(TAG, "HipCameraService callBack! command = " + command + " , fail = " + !fail
                + " , isAutoPreivew = " + isAutoPreivew + "  , isMotionD = " + isMotionD);
        if (command.equals(HipParamters.TAKE_PICTURE)) {
            sendIntent(HipParamters.RESULT_KEY, fail ? HipParamters.RESULT_OK
                    : HipParamters.RESULT_FAIL, error, HipParamters.TAKE_PICTURE);
            if(isMotionD) return;
            if (isAutoPreivew) {
                if (mListener != null) {
                    mListener.pauseCamera();
                }
            } else {
                if (mListener != null) {
                    mListener.startPreivew();
                }
            }
        } else if (command.equals(HipParamters.RECORD)) {
            sendIntent(HipParamters.RESULT_KEY, fail ? HipParamters.RESULT_OK
                    : HipParamters.RESULT_FAIL, error, HipParamters.RECORD);
            if(isMotionD) return;
            if (isAutoPreivew) {
                if (mListener != null) {
                    mListener.pauseCamera();
                }
            }
        }

    }
    public void excute4Activity(Intent intent) {
        CameraLog.d(TAG, " #excute4Activity ");
        if (HipParamters.START_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.TAKE_PICTURE.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.S_TAKE_PICTURE.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.RECORD.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.S_RECORD.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.AUTO_START_PREVIEW.equals(intent
                        .getStringExtra(HipParamters.COM_KEY))) {
            Message msg = mServiceHandler.obtainMessage();
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
        }
    }

    public interface Listener {
        public int queryCameraStatus();

        public boolean isPreview();

        public boolean isRecording();

        public boolean isCaptrue();

        public void setInitialState();

        public void startPreivew();

        public long getRecordingTime();

        public void excuteRecordCommand(boolean f);

        public void excuteTakePictueCommand();

        public void refreshCamera();

        public void stopCamera();

        public void pauseCamera();

        public boolean surFaceViewReady();

        public void excuteAction(Intent intent);

    }

    private int onQueryCameraStatus() {
        if (mListener == null){
            CameraLog.d(TAG, "onQueryCameraStatus mListener = " + mListener);
            return HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY;
        }
        return mListener.queryCameraStatus();
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;
            if (intent == null || intent.getAction() == null) {
                return;
            }
            CameraLog.d(TAG, "ServiceHandler handleMessage intent =" + intent.getAction());

            if (HipParamters.REQUEST_ACTION.equals(intent.getAction())) {
                CameraLog.e(
                        TAG,
                        "action command  ：" + HipParamters.COM_KEY + " = "
                                + intent.getStringExtra(HipParamters.COM_KEY));
                executeOnExecutorCommand(intent);
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                HipSettingPreferences.initParamterSettings(HipCameraService.this);
                mServiceHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        CameraLog.e(TAG, "Intent.ACTION_BOOT_COMPLETED !  , isLed_EVENT_ONREADY = "
                                + isLed_EVENT_ONREADY);
                        if (!isLed_EVENT_ONREADY && mCameraLed != null) {
                            mCameraLed.setStatus(HipParamters.EVENT_ONREADY);
                            isLed_EVENT_ONREADY = true;
                        }
                        if (isLed_EVENT_ONREADY) {
                            sendCameraReady();
                        }
                    }
                }, 15 * 1000);
            } else if (HipParamters.CAMERA_ACTION.equals(intent.getAction())) {
                executeOnExecutorAction(intent);
            } else if (HipParamters.ENNABLE_DETECT_ACTION.equals(intent.getAction())) {
                executeOnMotionAction(intent);
            } else if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())){
                CameraLog.d(TAG, "sdcard Mounting! mCameraLed:" + mCameraLed
                        + " , isLed_EVENT_ONREADY = " + isLed_EVENT_ONREADY);
                if (!isLed_EVENT_ONREADY) {
                    mServiceHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            CameraLog.e(TAG, "Intent.ACTION_BOOT_COMPLETED !");
                            if (mCameraLed != null) {
                                mCameraLed.setStatus(HipParamters.EVENT_ONREADY);
                                isLed_EVENT_ONREADY = true;
                                sendCameraReady();
                            }
                        }
                    });
                }
            }
        }
    }

    private void startCamera(Intent intent) {
        Intent in = new Intent();
        in.setClassName(CAMERA_PACKAGE_NAME, CAMERA_HIP_ACTIVITY);
        in.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NEW_TASK);
        in.putExtra(HipParamters.COM_KEY, intent.getStringExtra(HipParamters.COM_KEY));
        if(intent.hasExtra(HipParamters.RECORD)){
            in.putExtra(HipParamters.RECORD, intent.getBooleanExtra(HipParamters.RECORD, false));
        }
        in.putExtra("action", intent.getAction());
        startActivity(in);
        CameraLog.d(TAG, " camera isn't opened, server try to open Camera.");
    }

    private void excuteAction(Intent intent) {
        mListener.excuteAction(intent);
    }

    private void refreshCamera() {
        if (mListener != null) {
            CameraLog.d(TAG, " It is to refresh the camera device .");
            mListener.refreshCamera();
        }
    }

    private boolean isEnableCamera() {
        if (mListener != null) {
            return mListener.isRecording() ? false : !mListener.isCaptrue();
        } else {
            return true;
        }
    }

    private void executeOnMotionAction(Intent intent) {
        int switch_mode = HipSettingPreferences.getMode(HipCameraService.this);
        if(switch_mode != HipParamters.SURVEILLANCE_MODE ){
            return;
        }
        if(intent.hasExtra("catemp") && !intent.hasExtra("enable")){
            CameraLog.d(TAG, "Surveillance send success!");
            excuteAction(intent);
            return;
        }
        if(!intent.hasExtra(HipParamters.KEYS_MOTDET)){
            return;
        }
        int status = onQueryCameraStatus();
        boolean isEnable = intent.getBooleanExtra("enable", true);
        HipSettingPreferences.saveMotionDetector(HipCameraService.this, isEnable);

        CameraLog
                .e(TAG, "#executeOnMotionAction , status = " + status + ", isEnable = " + isEnable);
        if(isEnable){
            switch (status) {
                case HipParamters.CAMERA_ACTIVITY_STATUS_START: {
                    CameraLog.e(TAG, "SURVEILLANCE_MODE activtiy is START !");
                    Message msg = mServiceHandler.obtainMessage();
                    msg.obj = intent;
                    mServiceHandler.sendMessageDelayed(msg, 500);
                }
                    return;
                case HipParamters.CAMERA_ACTIVITY_STATUS_STOP: {
                    CameraLog.e(TAG, "SURVEILLANCE_MODE activtiy is STOP !");
                    Intent in = new Intent(HipParamters.REQUEST_ACTION);
                    in.putExtra(HipParamters.COM_KEY, HipParamters.AUTO_START_PREVIEW);
                    startCamera(in);
                    Message msg = mServiceHandler.obtainMessage();
                    msg.obj = intent;
                    mServiceHandler.sendMessageDelayed(msg, 1000);
                    return;
                }
                case HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE:
                    CameraLog.e(TAG, "SURVEILLANCE_MODE activtiy is ACTIVE !");
                    break;
                case HipParamters.CAMERA_ACTIVITY_STATUS_PAUSE: {
                    CameraLog.e(TAG, "SURVEILLANCE_MODE activtiy is PAUSE !");
                    Message msg = mServiceHandler.obtainMessage();
                    msg.obj = intent;
                    mServiceHandler.sendMessageDelayed(msg, 500);
                }
                    return;
                case HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY:
                    CameraLog.e(TAG, "SURVEILLANCE_MODE activtiy is DESTROY !");
                    startCamera(intent);
                    mPendingAction = intent;
                    return;
                default:
                    break;
            }
            if(!mListener.isPreview()){
                mListener.startPreivew();
            }
            excuteAction(intent);
        } else {
            excuteAction(intent);
            if (mListener != null && HipSettingPreferences.getAutoPreview(HipCameraService.this))
                mListener.pauseCamera();
        }
    }

    private void executeOnExecutorAction(Intent intent) {
        int status = onQueryCameraStatus();
        CameraLog.d(TAG, "executeOnExecutorAction Camera activity status = " + status);
        if (HipParamters.STOP_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            if (status == HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
                CameraLog.d(TAG, "Camera activity STOP_PREVIEW , mCameraHandler = "
                        + mCameraHandler);
                if (mCameraHandler != null) {
                    mCameraHandler.sendEmptyMessage(HipParamters.STOP_PREVIEW_MSG);
                }
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK,
                        HipParamters.NO_ERROR_CODE, HipParamters.STOP_PREVIEW);
            } else if (HipParamters.START_PREVIEW.equals(intent
                    .getStringExtra(HipParamters.COM_KEY))) {
                if (status != HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
                    startCamera(intent);
                } else {
                    sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK,
                            HipParamters.NO_ERROR_CODE, HipParamters.START_PREVIEW);
                }
            }else if (HipParamters.ZSL.equals(intent
                    .getStringExtra(HipParamters.COM_KEY))) {
                Intent in = HipSettingPreferences.setZSL(this, intent);
                sendBroadcast(in);
            }else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL, HipParamters.ERROR_CODE1,
                        intent.getStringExtra(HipParamters.COM_KEY));
            }

        }
    }

    private void executeOnExecutorCommand(Intent intent) {
        final int status = onQueryCameraStatus();
        CameraLog.d(TAG, "executeOnExecutorCommand Camera activity status = " + status);
        int switch_mode = HipSettingPreferences.getMode(this);
        String command = intent.getStringExtra(HipParamters.COM_KEY);
        CameraLog.d(TAG, "Camera activity status = " + status + " , switch_mode = " + switch_mode + " , command = "+command);
        boolean isFreshCamera = false;

        if (HipParamters.CAMERA_LOGIN.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            sendBroadcast(HipSettingPreferences.setOpenCamera(this));
//            startCamera(intent);
            return;
        } else if (HipParamters.CAMERA_LOGOUT.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            sendBroadcast(HipSettingPreferences.setCloseCamera(this));
            if (mListener != null) {
                mListener.stopCamera();
            }
        } else if (HipParamters.START_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.AUTO_START_PREVIEW.equals(intent
                        .getStringExtra(HipParamters.COM_KEY))) {
            // if (status != HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
            // startCamera();
            // } else {
            // sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK,
            // HipParamters.NO_ERROR_CODE, HipParamters.START_PREVIEW);
            // }
            if (HipParamters.START_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
                sendBroadcast(HipSettingPreferences.setAutoPreview(this, true));
            }
            if (status != HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
                startCamera(intent);
                mPendingAction = intent;
                return;
            } else {
                if (mListener.surFaceViewReady()) {
                    if (!mListener.isRecording() && !mListener.isCaptrue()) {
                        mListener.startPreivew();
                    }
                } else {
                    CameraLog.d(TAG, "Camera activity surface isn't ready");
                    Message msg = mServiceHandler.obtainMessage();
                    msg.obj = intent;
                    mServiceHandler.sendMessageDelayed(msg, 100);
                    return;
                }
            }

        } else if (HipParamters.STOP_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))
                || HipParamters.AUTO_STOP_PREVIEW.equals(intent
                        .getStringExtra(HipParamters.COM_KEY))) {
            if(HipParamters.STOP_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))){
                sendBroadcast(HipSettingPreferences.setAutoPreview(this, false));
            }else {
                if(!HipSettingPreferences.getAutoPreview(this)){
                    return;
                }
            }

            if((switch_mode != HipParamters.SURVEILLANCE_MODE) ||
               (!HipSettingPreferences.getMotionDetector(this))){  // Motion Detecting , don't stop preview.
                if(mListener != null){
                    if(mListener.isCaptrue()){// taking picture , don't stop preview.
                        return;
                    }
                    if(mListener.isRecording()){// recording video , don't stop preview.
                        return;
                    }
                    mListener.pauseCamera();
                    return;
                }else {
                    return;
                }
            }
        } else if (HipParamters.GET_CAMERA_MODE.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            sendBroadcast(HipSettingPreferences.getSwitchMode(this));
            new RecoveryCamera(status, switch_mode).start();
        } else if (HipParamters.GET_CAMERA_PRIVIEW.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            boolean isPreview = false;
            if (mListener != null) {
                isPreview = mListener.isPreview();
            }
            sendIntent(HipParamters.MODE, isPreview, HipParamters.NO_ERROR_CODE,
                    HipParamters.GET_CAMERA_PRIVIEW);
        } else if (HipParamters.GET_SWITCH_MODE.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            sendBroadcast(HipSettingPreferences.getSwitchMode(this));
            new RecoveryCamera(status, switch_mode).start();
        } else if (HipParamters.START.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            if (status != HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
                startCamera(intent);
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK,
                        HipParamters.NO_ERROR_CODE, HipParamters.START_PREVIEW);
            }
        } else if (HipParamters.STOP.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            if (mListener != null) {
                mListener.stopCamera();
            }
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK, HipParamters.NO_ERROR_CODE,
                    HipParamters.STOP);
        } else if (HipParamters.GET_RECORD_STATE
                .equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            getRecordState();
        } else if (HipParamters.SET_PARAMETERS.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setParameters(this, intent);
                sendBroadcast(in);
                if (intent.hasExtra(HipParamters.PICTURE_SIZE)) {
                    Intent in1 = HipSettingPreferences.getMEMPIC(this);
                    sendBroadcast(in1);
                } else if (intent.hasExtra(HipParamters.VIDEO_SIZE)){
                    Intent in2 = HipSettingPreferences.getMEMVIO(this);
                    sendBroadcast(in2);
                }
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_PARAMETERS);
            }
            new RecoveryCamera(status, switch_mode).start();

        } else if (HipParamters.GET_PARAMETERS.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getParameters(this, intent);
            sendBroadcast(in);
        } else if (HipParamters.SET_PICTURE_LAPSE.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setPictureLapseTime(this, intent);
                sendBroadcast(in);
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_PICTURE_LAPSE);
            }
        } else if (HipParamters.GET_PICTURE_LAPSE.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getPictureLapseTime(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.SET_PICTURE_BURST.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setPictureBurst(this, intent);
                sendBroadcast(in);
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_PICTURE_LAPSE);
            }

        } else if (HipParamters.GET_PICTURE_BURST.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getPictureBurst(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.SET_PICTURE_LAPSE_BURST.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setPictureLapseBurst(this, intent);
                sendBroadcast(in);
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_PICTURE_LAPSE);
            }

        } else if (HipParamters.GET_PICTURE_LAPSE_BURST.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getPictureLapseBurst(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.SET_VIDEO_LAPSE.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setVideoLapsetime(this, intent);
                sendBroadcast(in);
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_PICTURE_LAPSE);
            }

        } else if (HipParamters.GET_VIDEO_LAPSE.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getVideoLapsetime(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.SWITCH_MODE.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            boolean isSwitchEnable = true;
            if(mListener != null){
                if(mListener.isRecording() || mListener.isCaptrue()){
                    isSwitchEnable =false;
                }
            }
            if (isSwitchEnable) {
                Intent in = HipSettingPreferences.setSwitchMode(this, intent);
                sendBroadcast(in);
                mCameraHandler.post(new Runnable(){

                    @Override
                    public void run() {
                        Intent inMem;
                        inMem = HipSettingPreferences.getMEMPIC(HipCameraService.this);
                        sendBroadcast(inMem);
                        inMem = HipSettingPreferences.getMEMVIO(HipCameraService.this);
                        sendBroadcast(inMem);
                    }
                });
                if (mListener != null) {
                    mListener.setInitialState();
                }
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SWITCH_MODE);
            }

        } else if (HipParamters.REMAINING_VIDEO.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getRemaingVideo(this);
            sendBroadcast(in);
        } else if (HipParamters.REMAINING_PICTURE.equals(intent
                .getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getRemaingPictrue(this);
            sendBroadcast(in);

        } else if (HipParamters.GENERAL.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getGeneralIntent(this, intent);
            sendBroadcast(in);
            new RecoveryCamera(status, switch_mode).start();

        } else if (HipParamters.STILL.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getStillIntent(this, intent);
            sendBroadcast(in);
            new RecoveryCamera(status, switch_mode).start();

        } else if (HipParamters.SETTING.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getSettingsIntent(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.BASSTA.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getBasstaIntent(this, intent, mListener);
            sendBroadcast(in);
            new RecoveryCamera(status, switch_mode).start();

        } else if (HipParamters.SET_AUTO_FLIP.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setAutoFlip(this, intent);
                sendBroadcast(in);
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_PICTURE_LAPSE);
            }

        } else if (HipParamters.GET_AUTO_FLIP.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getAutoFlip(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.GET_GPS.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getGPS(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.MEMPIC.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getMEMPIC(this);
            sendBroadcast(in);

        } else if (HipParamters.MEMVIO.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getMEMVIO(this);
            sendBroadcast(in);

        } else if (HipParamters.TAKE_PICTURE.equals(command)
                || HipParamters.S_TAKE_PICTURE.equals(command)) {
            if (mIsEnableAction == false) {
                CameraLog.e(TAG, "TAKE_PICTURE command is too short1 !");
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE2, HipParamters.TAKE_PICTURE);
                return;
            }
            if (mIsEnableActionRecord == false) {
                CameraLog.e(TAG, "Record case take picture command is too short1 !");
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE2, HipParamters.TAKE_PICTURE);
                return;
            }
            if (mListener != null && mListener.isCaptrue()) {
                CameraLog.e(TAG, "Taking pictue , TAKE_PICTURE command  invlid !");
                return;
            }
            if (HipParamters.TAKE_PICTURE.equals(command)) {
                switch (switch_mode) {
                    case HipParamters.SURVEILLANCE_MODE: {
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                                HipParamters.ERROR_CODE8, HipParamters.TAKE_PICTURE);
                    }
                        return;
                    case HipParamters.CAMERA_MODE:
                    case HipParamters.VIDEO_MODE:
                    case HipParamters.PIC_TIME_LAPSE_MODE:
                    case HipParamters.PIC_BURST_MODE:
                        break;
                }
            }else {
                if(switch_mode != HipParamters.SURVEILLANCE_MODE){
                    return;
                }
            }
            CameraLog.e(TAG, "TAKE_PICTURE mListener =" + mListener);
            if (mListener != null) {
                switch (status) {
                    case HipParamters.CAMERA_ACTIVITY_STATUS_START:
                        CameraLog.e(TAG, "TAKE_PICTURE activtiy is START !");
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_STOP:
                        CameraLog.e(TAG, "TAKE_PICTURE activtiy is STOP !");
                        startCamera(intent);
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE:
                        CameraLog.e(TAG, "TAKE_PICTURE activtiy is ACTIVE !");
                        break;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_PAUSE:
                        CameraLog.e(TAG, "TAKE_PICTURE activtiy is PAUSE !");
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY:
                        CameraLog.e(TAG, "TAKE_PICTURE activtiy is DESTROY !");
                        startCamera(intent);
                        mPendingAction = intent;
                        return;
                    default:
                        break;
                }
                mIsEnableAction = false;
                mCameraHandler.sendEmptyMessageDelayed(HipParamters.SET_ENABLE_CAMERA_ACTION,
                        HipParamters.TAKE_PIC_AND_RECORD_INTERVAL);
                if (mListener.surFaceViewReady()) {
                    CameraLog.e(TAG, "TAKE_PICTURE excuteTakePictueCommand()!");
                    mListener.excuteTakePictueCommand();
                } else {
                    CameraLog.e(TAG, "surFaceView isn't Ready !");
                    Message msg = mServiceHandler.obtainMessage();
                    msg.obj = intent;
                    mServiceHandler.sendMessageDelayed(msg, 100);
                    return;
                }
            } else {
                startCamera(intent);
                mPendingAction = intent;
                return;
            }
        } else if (HipParamters.RECORD.equals(command) || HipParamters.S_RECORD.equals(command)) {
            boolean isRequestRecord = intent.getBooleanExtra(HipParamters.RECORD, false);
            boolean isRecording = mListener == null ? false : mListener.isRecording();

            CameraLog.e(TAG, "  #record , isRequestRecord = " + isRequestRecord
                    + " ,  isRecording = " + isRecording  + "  , mIsEnableActionRecord = "+mIsEnableActionRecord);
            if (mIsEnableActionRecord == false) {
                if (isRequestRecord) {
                    if (!isRecording) {
                        camApp.sendVideoAction("excuteRecord", true);
                        camApp.sendVideoAction("excuteRecord", false);
                    }
                } else {
                    if (!isRecording) {
                        Message msg = mServiceHandler.obtainMessage();
                        msg.obj = intent;
                        mServiceHandler.sendMessageDelayed(msg, 300);
                    }
                    CameraLog.e(TAG, "  #record , isRequestRecord = " + isRequestRecord
                            + " ,  isRecording = " + isRecording + " , delay 300ms");
                }
                sendIntentSurveillance(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.RECORD, HipParamters.ERROR_CODE2);
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE2, HipParamters.RECORD);
                return;
            }
//            if(switch_mode == HipParamters.SURVEILLANCE_MODE && !HipSettingPreferences.getMotionDetector(this)){
//                CameraLog.e(TAG, "RECORD command  invlid !");
//                return;
//            }
            if (HipParamters.RECORD.equals(command)) {
                switch (switch_mode) {
                    case HipParamters.SURVEILLANCE_MODE: {
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                                HipParamters.ERROR_CODE8, HipParamters.TAKE_PICTURE);
                    }
                        return;
                    case HipParamters.CAMERA_MODE:
                    case HipParamters.PIC_BURST_MODE:
                        boolean key_record = intent.getBooleanExtra(HipParamters.RECORD, false);
                        CameraLog.e(TAG, "key_record:" + key_record);
                        if(!key_record){
                            return;
                        }
                        if(mListener!= null && mListener.isCaptrue()){
                            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                                    HipParamters.ERROR_CODE8, HipParamters.RECORD);
                            return;
                        }
                        Intent in = HipSettingPreferences.autoSwitchMode(this,
                                HipParamters.VIDEO_MODE);
                        sendBroadcast(in);
                        mCameraHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent inMem;
                                inMem = HipSettingPreferences.getMEMPIC(HipCameraService.this);
                                sendBroadcast(inMem);
                                inMem = HipSettingPreferences.getMEMVIO(HipCameraService.this);
                                sendBroadcast(inMem);
                            }
                        });
                        Message msg = mServiceHandler.obtainMessage();
                        msg.obj = intent;
                        mServiceHandler.sendMessageDelayed(msg, 300);
                        return;
                    case HipParamters.VIDEO_MODE:
                    case HipParamters.PIC_TIME_LAPSE_MODE:
                        break;
                }
            } else {
                if (switch_mode != HipParamters.SURVEILLANCE_MODE) {
                    return;
                }
            }
            if (mListener != null) {
                switch (status) {
                    case HipParamters.CAMERA_ACTIVITY_STATUS_START:
                        CameraLog.e(TAG, "RECORD activtiy is START !");
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_STOP:
                        CameraLog.e(TAG, "RECORD activtiy is STOP !");
                        startCamera(intent);
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE:
                        CameraLog.e(TAG, "RECORD activtiy is ACTIVE !");
                        break;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_PAUSE:
                        CameraLog.e(TAG, "RECORD activtiy is PAUSE !");
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY:
                        CameraLog.e(TAG, "RECORD activtiy is DESTROY !");
                        startCamera(intent);
                        mPendingAction = intent;
                        return;
                    default:
                        break;
                }
                mIsEnableActionRecord = false;
                mCameraHandler.sendEmptyMessageDelayed(HipParamters.SET_ENABLE_CAMERA_ACTION_RECORD,
                        HipParamters.TAKE_PIC_AND_RECORD_INTERVAL * 2);
                if (mListener.surFaceViewReady()) {
                    mListener.excuteRecordCommand(intent.getBooleanExtra(HipParamters.RECORD, false));
                } else {
                    CameraLog.e(TAG, "surFaceView isn't Ready !");
                    Message msg = mServiceHandler.obtainMessage();
                    msg.obj = intent;
                    mServiceHandler.sendMessageDelayed(msg, 100);
                    return;
                }
            } else {
                startCamera(intent);
                mPendingAction = intent;
            }
        } else if (HipParamters.VIDEOSETING.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            Intent in = HipSettingPreferences.getVideoSetting(this, intent);
            sendBroadcast(in);

        } else if (HipParamters.SET_CAMERA_MODE.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
            if (isEnableCamera()) {
                Intent in = HipSettingPreferences.setSwitchMode(this, intent);
                sendBroadcast(in);
                isFreshCamera = true;
            } else {
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE4, HipParamters.SET_CAMERA_MODE);
            }
        } else {
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL, HipParamters.ERROR_CODE1,
                    intent.getStringExtra(HipParamters.COM_KEY));
        }
        if (isFreshCamera) {
            isFreshCamera = false;
            refreshCamera();
        }
    }

    public void getRecordState() {
        if (mListener != null) {
            boolean isRecording = mListener.isRecording();
            CameraLog.e(TAG, "getRecordState() isRecording =" + isRecording);
            sendRecordingTime(HipParamters.RECORD_STATE,isRecording);
        } else {
            sendRecordingTime(HipParamters.RECORD_STATE, false);
        }
    }

    public void excutePendingAction() {
        if (mPendingAction != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.obj = mPendingAction;
            mServiceHandler.sendMessageDelayed(msg, 100);
            mPendingAction = null;
        }
    }

    private void sendIntent(String key, String value, int error, String command) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(key, value);
        intent.putExtra("command", command);
        intent.putExtra(HipParamters.RESULT_ERROR, error);
        CameraLog.e(TAG, "key:" + key + " , value = " + value + " ,  command=" + command
                + " , error = " + error);
        sendBroadcast(intent);
    }

    private void sendIntent(String key, String value, String action, String filename) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(key, value);
        intent.putExtra("action", action);
        intent.putExtra("timestamp",filename);
        sendBroadcast(intent);
    }

    private void sendIntentSurveillance(String key, String value,String command,int error) {
        CameraLog.e(TAG, "sendIntentSurveillance()");
        Intent intent = new Intent(HipParamters.SURVEILLANCE_SHORT_ACTION);
        intent.putExtra(key, value);
        intent.putExtra("command", command);
        intent.putExtra(HipParamters.RESULT_ERROR, error);
        CameraLog.e(TAG, "key:" + key + " , value = " + value + " ,  command=" + command
                + " , error = " + error);
        sendBroadcast(intent);
    }

    private void sendIntent(String key, boolean value, int error, String command) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(key, value);
        intent.putExtra("command", command);
        intent.putExtra(HipParamters.RESULT_ERROR, error);
        CameraLog.e(TAG, "key:" + key + " , value = " + value + " ,  command=" + command
                + " , error = " + error);
        sendBroadcast(intent);
    }

    private void sendRecordingTime(String key, Boolean value) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(key, value);
        if (value) {
            long time = SystemClock.uptimeMillis() - mListener.getRecordingTime();
            intent.putExtra(HipParamters.RECORDING_TIME, time);
        }
        intent.putExtra("command", key);
        sendBroadcast(intent);
    }

    private class RecoveryCamera extends Thread {

        final int mStatus;
        final int mMode;

        public RecoveryCamera(int status_camera, int mode) {
            mStatus = status_camera;
            mMode = mode;
        }

        @Override
        public void run() {
            boolean isAutoPreview = (HipSettingPreferences.get4Setting(
                    HipCameraService.this,
                    HipParamters.AUTO_PREVIEW) == HipParamters.OPT_0);
            boolean isMotionDetect = HipSettingPreferences.getMotionDetector(HipCameraService.this);
            CameraLog.d(TAG, "Camera activity status = " + mStatus
                    + "  ,   isAutoPreview = " + isAutoPreview + " mMode = "
                    + mMode + "  , isMotionDetect = " + isMotionDetect);
            if (mMode == HipParamters.SURVEILLANCE_MODE) {
                if (isMotionDetect) {
                    if (mStatus == HipParamters.CAMERA_ACTIVITY_STATUS_STOP
                            || mStatus == HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY) {
                        Intent in = new Intent(HipParamters.ENNABLE_DETECT_ACTION);
                        in.putExtra("enable", isMotionDetect);
                        in.putExtra(HipParamters.KEYS_MOTDET, "motdet");
                        excuteAction(in);
                    }
                } else {
                    if (isAutoPreview) {
                        if (mStatus == HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
                            Intent in = new Intent(HipParamters.REQUEST_ACTION);
                            in.putExtra(HipParamters.COM_KEY, HipParamters.AUTO_STOP_PREVIEW);
                            sendBroadcast(in);
                        }
                    } else {
                        if (mStatus == HipParamters.CAMERA_ACTIVITY_STATUS_STOP
                                || mStatus == HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY) {
                            Intent in = new Intent(HipParamters.REQUEST_ACTION);
                            in.putExtra(HipParamters.COM_KEY, HipParamters.AUTO_START_PREVIEW);
                            sendBroadcast(in);
                        }
                    }
                }
            } else {
                switch (mStatus) {
                    case HipParamters.CAMERA_ACTIVITY_STATUS_STOP:
                    case HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY:
                        if (!isAutoPreview) {
                            Intent in = new Intent(HipParamters.REQUEST_ACTION);
                            in.putExtra(HipParamters.COM_KEY, HipParamters.AUTO_START_PREVIEW);
                            sendBroadcast(in);
                        }
                        return;
                    case HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE:
                        if (isAutoPreview) {
                            Intent in = new Intent(HipParamters.REQUEST_ACTION);
                            in.putExtra(HipParamters.COM_KEY, HipParamters.AUTO_STOP_PREVIEW);
                            sendBroadcast(in);
                        }
                    default:
                        return;
                }
            }
        }
    }

    private class CameraHandler extends Handler {
        public CameraHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HipParamters.SET_ENABLE_CAMERA_ACTION:
                    CameraLog.e(TAG, "SET_ENABLE_CAMERA_ACTION");
                    mIsEnableAction = true;
                    break;
                case HipParamters.SET_ENABLE_CAMERA_ACTION_RECORD:
                    CameraLog.e(TAG, "SET_ENABLE_CAMERA_ACTION_RECORD");
                    mIsEnableActionRecord = true;
                    break;
                case HipParamters.CAMERA_ACTION_DELAY:
                    CameraLog.e(TAG, "CAMERA_ACTION_DELAY");
                    break;
                case HipParamters.CAMERA_ACTION_START:
                    Intent in = new Intent(HipParamters.REQUEST_ACTION);
                    in.putExtra(HipParamters.COM_KEY, HipParamters.AUTO_START_PREVIEW);
                    CameraLog.e(TAG, "  SURVEILLANCE_MODE : " + HipParamters.AUTO_START_PREVIEW + "  action = " +in.getAction());
                    Message message = mServiceHandler.obtainMessage();
                    message.obj = in;
                    mServiceHandler.sendMessage(message);
                    break;
                default:
                    return;
            }
        }
    }
    private void sendCameraReady(){
        Intent in = new Intent(HipParamters.HIP_CAMRRA_SERVER_ONREADY);
        sendBroadcast(in);
    }
}
