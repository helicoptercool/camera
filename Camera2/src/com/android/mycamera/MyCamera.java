package com.android.mycamera;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.CameraLed;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.android.camera.CameraHolder;
import com.android.camera.CameraLog;
import com.android.camera.CameraManager.CameraPictureCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraManager.CameraShutterCallback;
import com.android.camera.LocationManager;
import com.android.camera.Storage;
import com.android.camera.app.CameraApp;
import com.android.camera.hip.dragonfly.HipCameraService;
import com.android.camera.hip.dragonfly.HipParamters;
import com.android.camera.hip.dragonfly.HipParamters.VIDEO_RECORD_STATE;
import com.android.camera.hip.dragonfly.HipSettingPreferences;
import com.android.camera.hip.observer.HipCameraObservable;
import com.android.camera.hip.observer.HipCameraObserver;
import com.android.camera.util.BitmapUtils;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;
import com.android.surveillance.SurveillanceService;
import com.android.surveillance.detector.AudioRecodeManager;
import com.android.surveillance.detector.MotionDetector;
import com.gatt.led.aidl.IRcLedService;

public class MyCamera extends Activity {

    private static final int MY_CAMERA_ID = 0;
    private static final int MENU_FIRST = 0;

    private static  int MODE_STATE = HipParamters.CAMERA_MODE;

    public static final String TAG = "MyCamera";

    // private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;
    // private SurfaceTexture mSurfaceTexture;
    private TextView mRecordingTimeView;
    private TextView mRecorTimeLapseTable;
    private static CameraProxy mCameraDevice;
    private CameraLed mCameraLed;
    private int mDisplayRotation;
    private int mDisplayOrientation;
    private int mCameraDisplayOrientation;
    private final CameraHandler mHandler = new CameraHandler();
    private MediaRecorder mMediaRecorder;
    private LocationManager mLocationManager;
    private static CamcorderProfile mProfile;
    private Parameters mParameters;
    private static long mStartRecordingTime = 0;
    private int mOrientation = 0;
    private VIDEO_RECORD_STATE mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
    private AngleListener mAngleListener;

    private IntentFilter mHipActionIntentFilter = new IntentFilter(HipParamters.CAMERA_ACTION);

    //camera settings
    //picture lapse
    private boolean picture_lapse_enable = false;
    private long picture_lapse_time = 0;
    //picture burst
    private boolean picture_burst_enable = false;
    private int picture_burst_rate =0;
    private int mBurstNum = 0;
    private int mSavedNum = 0;
    //picture lapse burst
    private boolean picture_lapse_burst_enable = false;
    //video lapse
    private boolean video_lapse_enable = false;
    private long video_lapse_time = 0;
    private long mVideo_lase_time = 0;

    private boolean isAuto_flip = false;

    private boolean isRecording = false;
    private boolean isCapture = false;
    private boolean isPreview = false;
    private boolean isStopLapsePic = false;
    private boolean isZSL = true;

    private BluetoothHeadset mBluetoothHeadset;
    private MotionDetector mMotionDetector;
    private HipCameraObservable  mObservable;

    private int mTargetWidth = 0;

    private int mTargetHight = 0;

    private int mCameraActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY;

    private HipCameraService mHipCameraService;
    private IRcLedService ledService;

    private SurveillanceService mSurveillanceService;

    private long mRecordTime = 0;

    private Boolean engps = true;

    private int x = 0;

    private boolean isTopTrigger = true;
    private boolean isBottomTrigger = true;
    private int mDirection = -1;// 0 top 1 bottom

    private static boolean mIsDebug = false;

    private static String mVideoExcuteActiontimeStamp = "";
    private static String mPictrueExcuteActiontimeStamp = "";
    private static String mStoragePictrueFile = "";
    private static String mStorageVideoFile = "";
    private static String mStorageThumbFile = "";
    private static int mTakPicIndex = 0;

    private int mTiltAngle = 0;

    private Intent mExcuteIntent = null;

    final Object waitDoneLock = new Object();

    private HipCameraObservable mCameraObservable;
    private CameraApp camApp;

    private static ExecutorService FULL_IMAGE_TASK_EXECUTOR;
    private static ExecutorService THUMB_IMAGE_TASK_EXECUTOR;
    static {
        FULL_IMAGE_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
        THUMB_IMAGE_TASK_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(10);
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_FIRST, 0, "拍照");
        menu.add(Menu.NONE, MENU_FIRST+1, 0, "录像");
        menu.add(Menu.NONE, MENU_FIRST+2, 0, "九连拍");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        CameraLog.d(TAG, "onNewIntent intent:"+intent);
        if(intent != null){
            mExcuteIntent = new Intent(HipParamters.REQUEST_ACTION);
            mExcuteIntent.putExtra(HipParamters.COM_KEY, intent.getStringExtra(HipParamters.COM_KEY));
            if(intent.hasExtra(HipParamters.RECORD)){
                mExcuteIntent.putExtra(HipParamters.RECORD, intent.getBooleanExtra(HipParamters.RECORD, false));
            }
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        CameraLog.d(TAG, "onCreate()");
        Window win = getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        win.setAttributes(params);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mycamera);
        mRecordingTimeView = (TextView)findViewById(R.id.record_time);
        mRecorTimeLapseTable = (TextView)findViewById(R.id.video_time_lapse_label);
        // mTextureView = (TextureView)findViewById(R.id.preview);
        // mTextureView.setSurfaceTextureListener(this);
        mSurfaceView = (SurfaceView) findViewById(R.id.preview);
        SurfaceHolder holder = mSurfaceView.getHolder();
        camApp = (CameraApp) this.getApplication();
        holder.addCallback(mSurfaceCallback);
        mCameraLed = new CameraLed();
        mLocationManager = new LocationManager(this);
        mMotionDetector = new MotionDetector(this);
        mObservable = new HipCameraObservable();
        mAngleListener = new AngleListener();
        isAuto_flip = HipSettingPreferences.get4Setting(this, HipParamters.AUTO_FLIP_STATE) == HipParamters.OPT_0;
        CameraLog.e(TAG, "onCreate isAuto_flip: " + isAuto_flip);
        if(isAuto_flip){
            mAngleListener.enabel();
        }else{
            mAngleListener.disable();
        }
        //open camera
        openCamera();

        mTargetWidth = getWindowManager().getDefaultDisplay().getWidth();
        mTargetHight = getWindowManager().getDefaultDisplay().getHeight();
        setPerview(0,0);
        /*mTargetWidth = 1920;
        mTargetHight = 1080;*/
        CameraLog.e(TAG, "mTargetWidth = " + mTargetWidth + ",mTargetHight:" + mTargetHight);
        mCameraActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_START;
        camApp.setActivityStatus(mCameraActivityStatus);
        bindHipCameraService();
        bindLedService();
        registerReceiver(mHipActionBroadcast, mHipActionIntentFilter);
/*        Intent mIntent = this.getIntent();
        setMotionDetector(mIntent);*/

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addDataScheme("file");
        registerReceiver(broadcastRec, intentFilter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        mCameraObservable = new HipCameraObservable();
    }

    @Override
    public void onResume() {
        CameraLog.d(TAG, "onResume()  mCameraDevice = " + mCameraDevice);
        /*if (Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            mAutoRotateScreen = false;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            mAutoRotateScreen = true;
        }*/
        // open camera
        openCamera();
        super.onResume();
        AudioRecodeManager.onBeforePreviewStart();
        initBT();
        intGPS();
        IntentFilter intentFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBluetoothHandsetBroadcastReceiver, intentFilter);
        mCameraActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE;
        camApp.setActivityStatus(mCameraActivityStatus);
        if(isAuto_flip){
            mAngleListener.enabel();
        }else{
            mAngleListener.disable();
        }
        isCapture = false;
        isRecording = false;
        camApp.setCapture(isCapture);
        camApp.setRecording(isRecording);
        camApp.setVideoThumb(false);
    }

/*    private void setMotionDetector(Intent intent) {
        CameraLog.d(TAG, "setMotionDetector intent:" + intent);
        // Intent intent = this.getIntent();
        if (intent != null) {
            if (SurveillanceService.KEYS_SET_MOTION_DETECT.equals(intent
                    .getStringExtra(HipParamters.COM_KEY))) {
                mMotionDetector.setIntent(intent);
            }
        }
    }*/

    @Override
    public void onPause() {
        CameraLog.d(TAG, "onPause()");

        // reset flag
        if (mHandler.hasMessages(HipParamters.UPDATE_RECORD_TIME)) {
            mHandler.removeMessages(HipParamters.UPDATE_RECORD_TIME);
        }
        if (mHandler.hasMessages(HipParamters.VIDEO_CORDING)) {
            mHandler.removeMessages(HipParamters.VIDEO_CORDING);
        }
        isCapture = isRecording = false;
        camApp.setCapture(isCapture);
        camApp.setRecording(isRecording);
        camApp.setLongRecTime(0);
        camApp.setVideoThumb(false);

        mAngleListener.disable();
        mCameraLed.setStatus(HipParamters.EVENT_VIDEO_RECORD_STOP);
        if (isRecording) {
            mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
            stopVideoRecording();
        }
        if(isCapture){
            mHandler.sendEmptyMessage(HipParamters.STOP_TAKE_PICTURE_LAPSE);
        }
        releaseMediaRecorder();
        super.onPause();

        closeCamera();
        AudioRecodeManager.onAfterPreviewStop();
        if (mBluetoothHeadset != null && mBluetoothHeadset.getConnectedDevices().size() > 0) {
            mBluetoothHeadset.disconnectAudio();
        }
        unregisterReceiver(mBluetoothHandsetBroadcastReceiver);
        mCameraActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_PAUSE;
        camApp.setActivityStatus(mCameraActivityStatus);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CameraLog.d(TAG, "onSaveInstanceState() mCameraDevice = " + mCameraDevice);
    }
    @Override
    public void onStop() {
        CameraLog.d(TAG, "onStop() mCameraDevice = " + mCameraDevice);
        mCameraActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_STOP;
        camApp.setActivityStatus(mCameraActivityStatus);
        isCapture = false;
        isRecording = false;
        camApp.setRecording(false);
        camApp.setCapture(false);
        isPreview = false;
        if (mLocationManager != null) {
            CameraLog.d(TAG, " onStop recordLocation false" );
            mLocationManager.recordLocation(false);
        }
        super.onStop();
    }

    @Override
    public void finish() {
        CameraLog.d(TAG, "finish()");
        mCameraLed.setStatus(HipParamters.EVENT_VIDEO_RECORD_STOP);
        camApp.setActivityStatus(mCameraActivityStatus);
        this.moveTaskToBack(true);
    };

    @Override
    public void onDestroy() {
        CameraLog.d(TAG, "onDestroy()");
        mCameraActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY;
        camApp.setActivityStatus(mCameraActivityStatus);
        unBindHipCameraService();
        unBindLedService();
        unregisterReceiver(mHipActionBroadcast);
        unregisterReceiver(broadcastRec);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        CameraLog.e(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        setDisplayOrientation();

        mTargetWidth = getWindowManager().getDefaultDisplay().getWidth();
        mTargetHight = getWindowManager().getDefaultDisplay().getHeight();
        /*mTargetWidth = 1920;
        mTargetHight = 1080;*/
        CameraLog.e(TAG, "mTargetWidth = " + mTargetWidth + ",mTargetHight:" + mTargetHight);

        setPerview(0,0);
    }

    private void setDisplayOrientation() {
        mDisplayRotation = CameraUtil.getDisplayRotation(this);
        mDisplayOrientation = CameraUtil.getDisplayOrientation(mDisplayRotation, 0);
        mCameraDisplayOrientation = mDisplayOrientation;
        // Change the camera display orientation
        if (mCameraDevice != null) {
            CameraLog.d(TAG, "mDisplayRotation:" + mDisplayRotation + ",mCameraDisplayOrientation:" + mCameraDisplayOrientation);
            CameraLog.d(TAG,"mDirection:" + mDirection + ",isAuto_flip:" + isAuto_flip);
            if (mDirection == 0) {
                if (!mHandler.hasMessages(HipParamters.TOP_TRIGGER)) {
                    mHandler.sendEmptyMessage(HipParamters.TOP_TRIGGER);
                }
            } else if (mDirection == 1) {
                if (isAuto_flip) {
                    if (!mHandler.hasMessages(HipParamters.BOTTOM_TRIGGER)) {
                        mHandler.sendEmptyMessage(HipParamters.BOTTOM_TRIGGER);
                    }
                } else {
                    if (!mHandler.hasMessages(HipParamters.TOP_TRIGGER)) {
                        mHandler.sendEmptyMessage(HipParamters.TOP_TRIGGER);
                    }
                }
            }
            //mCameraDevice.setDisplayOrientation(mCameraDisplayOrientation);
        }
    }

    private CameraPictureCallback mBurstCameraPictureCallback = new CameraPictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, CameraProxy camera) {
            mTakPicIndex++;
//            new SaveTakePictrueImageTask(data, mTakPicIndex).execute(mPictrueExcuteActiontimeStamp);
            new SaveTakePictrueImageTask(data, mTakPicIndex).executeOnExecutor(FULL_IMAGE_TASK_EXECUTOR,
                    mPictrueExcuteActiontimeStamp);

            boolean isDebug = isDebug();
            if (!isDebug) {
                new SaveThumbImageTask(data, mTakPicIndex).executeOnExecutor(
                        THUMB_IMAGE_TASK_EXECUTOR, mPictrueExcuteActiontimeStamp);
            }
            if (mBurstNum == 1) {
                mHipCameraService.callBack(HipParamters.TAKE_PICTURE, true, HipParamters.NO_ERROR_CODE);
                CameraLog.e(TAG, "takePicture ok");
                mTakPicIndex = 0;
                mPictrueExcuteActiontimeStamp = "";
                isCapture = false;
            } else {
                mHandler.sendEmptyMessage(HipParamters.TAKE_PICTURE_DALAY);
                isCapture = true;
                mBurstNum --;
            }
            CameraLog.e(TAG, "mBurstCameraPictureCallback is calling mTakPicIndex = "+ mTakPicIndex + " mBurstNum = "+mBurstNum);
        }

    };

    private class SaveTakePictrueImageTask extends AsyncTask<String, Integer, Boolean> {

        private byte[] mData;
        private int mIndex = 0;

        protected SaveTakePictrueImageTask(byte[] data, int index) {
            mData = data;
            mIndex = index;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String mSavePath;
            String nSavePath = "";
            mSavePath = mStoragePictrueFile;
            boolean isDebug = isDebug();
            if(isDebug){
                mSavePath = mSavePath.replace("sdcard1", "sdcard0");
                nSavePath = mSavePath;
                CameraLog.e(TAG, "mSavePath1 =" + mSavePath);
            }
            if (mIndex > -1) {
                if (params[0] != null && params[0] != "") {
                    mSavePath = mStoragePictrueFile + File.separator + params[0];
                }else {
                    mSavePath = mStoragePictrueFile;
                }
                File f = new File(mSavePath);
                if (!f.exists()) {
                    sendMediaAddIntent(HipParamters.MEDIA_TYPE_FOLDER, mSavePath, params[0]);
                    f.mkdirs();
                }
                if (mIndex < 10) {
                    mSavePath += File.separator + params[0] + "_0" + mIndex + ".jpg";
                } else {
                    mSavePath += File.separator + params[0] + "_" + mIndex + ".jpg";
                }
            } else {
                File f = new File(mSavePath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                mSavePath += File.separator + params[0] + ".jpg";
            }
            if(MODE_STATE == HipParamters.CAMERA_MODE){
                if(isDebug){
                    int donutjpg = android.os.SystemProperties.getInt("debug.camera.donutjpg", 0);
                    CameraLog.d(TAG, "donutjpg:" + donutjpg);
                    File mediaFile = null;
                    if(donutjpg == 0){
                        mediaFile = new File(nSavePath);
                        if (!mediaFile.exists()) {
                            mediaFile.mkdirs();
                        }
                        mSavePath = mediaFile.getAbsolutePath() + File.separator + "unwrap.jpg";
                    }else if(donutjpg == 1){
                        mediaFile = new File(nSavePath);
                        if (!mediaFile.exists()) {
                            mediaFile.mkdirs();
                        }
                        mSavePath = mediaFile.getAbsolutePath() + File.separator + "donut.jpg";
                    }
                }
            }
            CameraLog.e(TAG, "mSavePath2 = " + mSavePath);
            Storage.writeFile(mSavePath, mData);
            sendMediaAddIntent(HipParamters.MEDIA_TYPE_PICTRUE, mSavePath, params[0] + ".jpg");
            CameraLog.e(TAG, "SaveTakePictrueImageTask  mSavePath = "+mSavePath + " ,params[0] = " +params[0]);

            Intent intent = HipSettingPreferences.reducePictureNumberAction(MyCamera.this);
            sendBroadcast(intent);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            CameraLog.e(TAG, "SaveTakePictrueImageTask is ok");
        }
    }

    private class SaveThumbImageTask extends AsyncTask<String, Integer, Boolean> {

        private byte[] mData;
        private int mIndex = 0;

        protected SaveThumbImageTask(byte[] data, int index) {
            mData = data;
            mIndex = index;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String mSavePath;
            String mSaveThumbPath;
            mSaveThumbPath = mSavePath = mStorageThumbFile;
            if (mIndex > -1) {
                if (params[0] != null && params[0] != "") {
                    mSavePath = mStorageThumbFile + File.separator + params[0];
                }else {
                    mSavePath = mStorageThumbFile;
                }
                if (mIsDebug) {
                    mSavePath = mSavePath.replace("sdcard1", "sdcard0");
                }
                File f = new File(mSavePath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                if (mIndex < 10) {
                    mSavePath += File.separator + params[0] + "_0" + mIndex + ".jpg";
                } else {
                    mSavePath += File.separator + params[0] + "_" + mIndex + ".jpg";
                }
                mSaveThumbPath += File.separator + params[0];

            } else {
                File f = new File(mSavePath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                mSavePath += File.separator + params[0] + ".jpg";
            }
            CameraLog.e(TAG, "mSavePath=" + mSavePath);
            Storage.writeThumbFile(mSavePath, mData);
            CameraLog.e(TAG, "saveThumbImageTask  mSaveThumbPath =" + mSaveThumbPath+ " ,params[0]:" + params[0]);
            writeThumbFile(mSaveThumbPath,mSavePath);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            CameraLog.e(TAG, "saveThumbImageTask is ok");
        }
    }

    private class SaveCalibrationImageTask extends AsyncTask<String, Integer, Boolean> {

        private byte[] mData;

        protected SaveCalibrationImageTask(byte[] data) {
            mData = data;
        }

        protected Boolean doInBackground(String... params) {
            String mSavePath = null;
            int donutjpg = android.os.SystemProperties.getInt("debug.camera.donutjpg", 0);
            File mediaFile = null;
            if(donutjpg == 0){
                mediaFile = new File(Storage.INTERALE_DCIM);
                if(!mediaFile.exists()){
                    mediaFile.mkdirs();
                }
                mSavePath = Storage.INTERALE_DCIM + File.separator + "unwrap.jpg";
            }else if(donutjpg == 1){
                mediaFile = new File(Storage.INTERALE_DCIM);
                if(!mediaFile.exists()){
                    mediaFile.mkdirs();
                }
                mSavePath = Storage.INTERALE_DCIM + File.separator + "donut.jpg";
            }
            CameraLog.e(TAG, "mSavePath2 = " + mSavePath);
            if(mSavePath != null){
                Storage.writeFile(mSavePath, mData);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            CameraLog.e(TAG, "SaveCalibrationImageTask is ok");
        }
    }

    private final class PictureCallback implements CameraPictureCallback {
        @Override
        public void onPictureTaken(final byte[] data, CameraProxy camera) {
            new SaveTakePictrueImageTask(data, -1).executeOnExecutor(FULL_IMAGE_TASK_EXECUTOR,
                    mPictrueExcuteActiontimeStamp);
            boolean isDebug = isDebug();
            if (!isDebug) {
                new SaveThumbImageTask(data, -1).executeOnExecutor(THUMB_IMAGE_TASK_EXECUTOR,
                        mPictrueExcuteActiontimeStamp);
            }
            isCapture = false;
            mTakPicIndex =0;
            CameraLog.e(TAG, "PictureCallback is calling !");
            mHipCameraService.callBack(HipParamters.TAKE_PICTURE, true, HipParamters.NO_ERROR_CODE);
        };
    }

    private final class PictureSnapshotCallback implements CameraPictureCallback {
        @Override
        public void onPictureTaken(final byte[] data, CameraProxy camera) {
            new SaveTakePictrueImageTask(data, -1).executeOnExecutor(FULL_IMAGE_TASK_EXECUTOR,
                    mPictrueExcuteActiontimeStamp);
            boolean isDebug = isDebug();
            if (!isDebug) {
                new SaveThumbImageTask(data, -1).executeOnExecutor(THUMB_IMAGE_TASK_EXECUTOR,
                        mPictrueExcuteActiontimeStamp);
            }
            camApp.setLiveShot(false);
            mTakPicIndex = 0;
            CameraLog.e(TAG, "PictureSnapshotCallback is calling !");
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK, HipParamters.NO_ERROR_CODE);
        };
    }

    private final class PictureCallback4VideoThumb implements CameraPictureCallback {
        @Override
        public void onPictureTaken(final byte[] data, CameraProxy camera) {
            boolean isDebug = isDebug();
            camApp.setVideoThumb(false);
            if (!isDebug) {
                new SaveThumbImageTask(data, -1).execute(mVideoExcuteActiontimeStamp);
            }
            CameraLog.e(TAG, "PictureCallback4VideoThumb is calling !");
        };
    }

    private final class PictureCallbackCalibration implements CameraPictureCallback {
        @Override
        public void onPictureTaken(final byte[] data, CameraProxy camera) {
            new SaveCalibrationImageTask(data).execute();
//            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK,HipParamters.NO_ERROR_CODE);
            mHipCameraService.callBack(HipParamters.TAKE_PICTURE, true, HipParamters.NO_ERROR_CODE);
            CameraLog.e(TAG, "PictureCallbackCalibration is calling !");
        };
    }

    private void lapseTakePicture() {
        CameraLog.e(TAG, "# lapseTakePictureset jpeg mOrientation = " + mOrientation);
        mParameters.setRotation(mOrientation);
        if (mCameraDevice != null) {
            CameraLog.e(TAG, "#lapseTakePictureset takePicture start");
            mParameters.setZSLMode("on");
            mParameters.setCameraMode(1);
            mCameraDevice.setParameters(mParameters);
            if (MODE_STATE == HipParamters.PIC_TIME_LAPSE_MODE) {
                mCameraDevice.takePicture(mHandler, new ShutterCallback(), null, null,
                        mLapsePictureCallback);
            } else if (MODE_STATE == HipParamters.PIC_BURST_TIME_LAPSE_MODE) {
                mCameraDevice.takePicture(mHandler, new ShutterCallback(), null, null,
                        mLapseBurstPictureCallback);
            }

            mCameraLed.setStatus(HipParamters.EVENT_TAKE_PICTURE_START);
        } else {
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL, HipParamters.ERROR_CODE2);
        }
    }

    private CameraPictureCallback  mLapsePictureCallback = new CameraPictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, CameraProxy camera) {
            CameraLog.e(TAG, "mLapsePictureCallback  onPictureTaken " + ",isStopLapsePic = " + isStopLapsePic + ",picture_lapse_time" + picture_lapse_time);
            mTakPicIndex++;
            new SaveTakePictrueImageTask(data, mTakPicIndex).executeOnExecutor(
                    FULL_IMAGE_TASK_EXECUTOR, mPictrueExcuteActiontimeStamp);
            boolean isDebug = isDebug();
            if (!isDebug) {
                new SaveThumbImageTask(data, mTakPicIndex).executeOnExecutor(
                        THUMB_IMAGE_TASK_EXECUTOR, mPictrueExcuteActiontimeStamp);
            }
            //force stop taking picture lapse time .
            if (isStopLapsePic) {
                isStopLapsePic = false;
                isCapture = false;
                mTakPicIndex =0;
                mPictrueExcuteActiontimeStamp = "";
                return;
            }
            mHandler.sendEmptyMessageDelayed(HipParamters.START_TAKE_PICTURE_LAPSE,picture_lapse_time);
        }
    };

    private CameraPictureCallback mLapseBurstPictureCallback = new CameraPictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, CameraProxy camera) {
            CameraLog.e(TAG, "mLapseBurstPictureCallback  onPictureTaken mBurstNum = " + mBurstNum  + "   isStopLapsePic = " + isStopLapsePic);
            mTakPicIndex++;
            new SaveTakePictrueImageTask(data, mTakPicIndex).executeOnExecutor(
                    FULL_IMAGE_TASK_EXECUTOR, mPictrueExcuteActiontimeStamp);
            boolean isDebug = isDebug();
            if(!isDebug){
                new SaveThumbImageTask(data, mTakPicIndex).executeOnExecutor(
                        THUMB_IMAGE_TASK_EXECUTOR, mPictrueExcuteActiontimeStamp);
            }
            //force stop taking picture lapse time .
            if (isStopLapsePic) {
                isStopLapsePic = false;
                isCapture = false;
                mTakPicIndex =0;
                mPictrueExcuteActiontimeStamp = "";
                return;
            }
            if(mBurstNum >1){
                mHandler.sendEmptyMessage(HipParamters.START_TAKE_PICTURE_LAPSE);
                mBurstNum--;
            } else {
                mSavedNum = mBurstNum = picture_burst_rate;
                mHandler.sendEmptyMessageDelayed(HipParamters.START_TAKE_PICTURE_LAPSE,picture_lapse_time);
            }
        }
    };

    private final class ShutterCallback implements CameraShutterCallback {
        public void onShutter(CameraProxy camera){

        };
    }

    private void initCamcorderProfile(int videoQuality) {
        int quality = videoQuality;
        if(quality == 0){
            quality = CamcorderProfile.QUALITY_6480x1080;
        }else if(quality == 1){
            quality = CamcorderProfile.QUALITY_3840x640;
        }else if(quality == 2){
            quality = CamcorderProfile.QUALITY_2880x480;
        }else if(quality == 3){
            quality = CamcorderProfile.QUALITY_1920x320;
        }else{
            quality = CamcorderProfile.QUALITY_6480x1080;
        }
        CameraLog.e(TAG, "#initCamcorderProfile Video Quality " + quality);
        mProfile = CamcorderProfile.get(quality);
        if (mProfile == null) {
            return;
        }
        CameraLog.d(TAG, "initCamcorderProfile setProfile" + mProfile.toString());
        CameraLog.d(TAG, "initCamcorderProfile audioBitRate:" + mProfile.audioBitRate);
        CameraLog.d(TAG, "initCamcorderProfile audioChannels:" + mProfile.audioChannels);
        CameraLog.d(TAG, "initCamcorderProfile audioCodec:" + mProfile.audioCodec);
        CameraLog.d(TAG, "initCamcorderProfile audioSampleRate:" + mProfile.audioSampleRate);
        CameraLog.d(TAG, "initCamcorderProfile duration:" + mProfile.duration);
        CameraLog.d(TAG, "initCamcorderProfile fileFormat:" + mProfile.fileFormat);
        CameraLog.d(TAG, "initCamcorderProfile quality:" + mProfile.quality);
        CameraLog.d(TAG, "initCamcorderProfile videoBitRate:" + mProfile.videoBitRate);
        CameraLog.d(TAG, "initCamcorderProfile videoCodec:" + mProfile.videoCodec);
        CameraLog.d(TAG, "initCamcorderProfile videoFrameHeight:" + mProfile.videoFrameHeight);
        CameraLog.d(TAG, "initCamcorderProfile videoFrameRate:" + mProfile.videoFrameRate);
        CameraLog.d(TAG, "initCamcorderProfile videoFrameWidth:" + mProfile.videoFrameWidth);
    }

    private String mRecordVideoFile = null;
    private int mVideoQuality = 0;
    private boolean initializeRecorder() {
        if (mCameraDevice == null) return false;

        File file = new File(mStorageVideoFile);
        if(!file.exists()){
            file.mkdirs();
        }
        mCameraLed.setStatus(HipParamters.EVENT_VIDEO_RECORD_START);
        setRcLedStatus(HipParamters.BT_STATUS_VIDEO_RECORD_START);
        if (mObservable != null) {
            mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_INIT);
        }

        mRecordVideoFile = mStorageVideoFile + File.separator + mVideoExcuteActiontimeStamp + ".mp4";
        CameraLog.d(TAG, "initializeRecorder->videoFile:" + mRecordVideoFile);
        mMediaRecorder = new MediaRecorder();
        mParameters.setZSLMode("off");
        mParameters.setRecordingHint(true);
        mParameters.setRotation(mOrientation);
        mCameraDevice.setParameters(mParameters);
        CameraLog.d(TAG, "initializeRecorder-> camera device unlock !");
        mCameraDevice.unlock();
        mMediaRecorder.setCamera(mCameraDevice.getCamera());

        CameraLog.d(TAG, "initializeRecorder-> camera device setAudioSource !");
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (mBluetoothHeadset != null && mBluetoothHeadset.getConnectedDevices().size() > 0
                && mBluetoothHeadset.isAudioOn()) {
            CameraLog.d(TAG, "initializeRecorder  BT is connected !");
            mProfile.audioSampleRate = 8000;
            mProfile.audioChannels = 1;
            mProfile.audioCodec = 1;
            mProfile.audioBitRate = 12200;
        } else {
            mProfile.audioSampleRate = 48000;
            mProfile.audioChannels = 2;
            mProfile.audioCodec = 3;
            mProfile.audioBitRate = 156000;
        }
        CameraLog.d(TAG, "initializeRecorder setProfile" + mProfile.toString());
        CameraLog.d(TAG, "initializeRecorder audioBitRate:" + mProfile.audioBitRate);
        CameraLog.d(TAG, "initializeRecorder audioChannels:" + mProfile.audioChannels);
        CameraLog.d(TAG, "initializeRecorder audioCodec:" + mProfile.audioCodec);
        CameraLog.d(TAG, "initializeRecorder audioSampleRate:" + mProfile.audioSampleRate);
        CameraLog.d(TAG, "initializeRecorder duration:" + mProfile.duration);
        CameraLog.d(TAG, "initializeRecorder fileFormat:" + mProfile.fileFormat);
        CameraLog.d(TAG, "initializeRecorder quality:" + mProfile.quality);
        CameraLog.d(TAG, "initializeRecorder videoBitRate:" + mProfile.videoBitRate);
        CameraLog.d(TAG, "initializeRecorder videoCodec:" + mProfile.videoCodec);
        CameraLog.d(TAG, "initializeRecorder videoFrameHeight:" + mProfile.videoFrameHeight);
        CameraLog.d(TAG, "initializeRecorder videoFrameRate:" + mProfile.videoFrameRate);
        CameraLog.d(TAG, "initializeRecorder videoFrameWidth:" + mProfile.videoFrameWidth);
        mMediaRecorder.setProfile(mProfile);
        setRecordLocation();
//        mMediaRecorder.setMaxDuration((int)getRemainingVideoTime());
        mMediaRecorder.setOutputFile(mRecordVideoFile);
        long mMaxVideoSize;
        long spaceSize = Storage.getSpaceStorageSzie() - HipParamters.MEDIARECORDER_SPACE_MAX_SIZE;
        mMaxVideoSize = spaceSize;
        if (!isDebug() && isExternalFat32()
                && (mMaxVideoSize - HipParamters.SPACE_MAX_VIDEO_SIZE_FAT32) >= HipParamters.SPACE_MAX_SIZE) {
            mMaxVideoSize = HipParamters.SPACE_MAX_VIDEO_SIZE_FAT32;
            mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
            CameraLog.d(TAG, "initializeRecorder spaceSize > 4G ," + "   max size = 4G"
                    + "   start recording , again ");
        } else {
            mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
        }
        mMediaRecorder.setMaxFileSize(mMaxVideoSize);
        CameraLog.d(TAG, "initializeRecorder spaceSize:" + spaceSize + "   mOrientation ="
                + mOrientation);
        mMediaRecorder.setOrientationHint(mOrientation);
        if (MODE_STATE == HipParamters.VID_TIME_LAPSE_MODE && video_lapse_enable) {
            double fps = 1000 / (double) video_lapse_time;
            CameraLog.e(TAG, "video lapse is enable ,set capture rate " + fps);
            mMediaRecorder.setCaptureRate(fps);
        }
        try {
            CameraLog.d(TAG, "initializeRecorder-> mMediaRecorder prepare !");
            mMediaRecorder.prepare();
        } catch (IOException e) {
            releaseMediaRecorder();
            throw new RuntimeException(e);
        }

        CameraLog.e(TAG, "mMediaRecorder.setOnInfoListener()");
        mMediaRecorder.setOnInfoListener(new OnInfoListener() {
            public void onInfo(MediaRecorder arg0, int what, int arg2) {
                CameraLog.d(TAG,"onInfo()");
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    CameraLog.d(TAG, "onInfo()->isRecording:" + isRecording + ",DURATION_REACHED");
                    if (isRecording) {
                        mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                        mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                    }
                } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    CameraLog.d(TAG, "onInfo()->isRecording:" + isRecording + ",FILESIZE_REACHED");
                    if (isRecording) {
                        if(mVideoContinuousRecord == VIDEO_RECORD_STATE.START){
                            mVideoContinuousRecord = VIDEO_RECORD_STATE.ONINFO_CONTINUATION;
                        }
                        //mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                        mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                    }
                }
            }
        });
        return true;
    }

    private void releaseMediaRecorder() {
    	CameraLog.e(TAG, "releaseMediaRecorder");
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mRecordingTimeView.setVisibility(View.GONE);
        mRecorTimeLapseTable.setVisibility(View.GONE);
        isRecording = false;
        camApp.setRecording(false);
        camApp.setVideoThumb(false);
    }

    private void startPreview() {
        if (mCameraDevice == null) {
            mCameraDevice = CameraUtil.openCamera(this, MY_CAMERA_ID, null, null);
            mParameters = mCameraDevice.getParameters();
        } else {
            CameraLog.d(TAG, "CAMERA_OUT_SLEEP_MODE waitDone:");
        }
        mCameraDevice.setErrorCallback(new HipCameraErrorCallback());
        if (isPreview) {
            stopPreview();
        }
        if (mCameraDevice != null) {
            mHandler.sendEmptyMessage(HipParamters.REFRSH_CAMERA_DEVICE);
//            refreshCamera();
            setPreviewDisplay(mSurfaceHolder);
            setDisplayOrientation();
            mCameraDevice.startPreview();
            isPreview = true;
            sendHipCameraCommand(HipParamters.START_PREVIEW, HipParamters.RESULT_OK);
            mCameraLed.setStatus(HipParamters.EVENT_ONREADY);
        }
    }

    private void stopPreview() {
        isPreview = false;
        CameraLog.d(TAG, "stopPreview() isPreview =" + isPreview);
        if (mCameraDevice != null) {
            CameraLog.d(TAG, "stopPreview() will stopPreview!");
            mCameraDevice.stopPreview();
        }
    }

    public void excuteTakePicture() {
        long spaceSize = Storage.getSpaceStorageSzie();
        CameraLog.d(TAG, "excuteTakePicture spaceSize:" + spaceSize + " , MODE_STATE =" + MODE_STATE);
        if(spaceSize <= HipParamters.SPACE_MAX_SIZE){
            CameraLog.d(TAG, "excuteTakePicture Space does not do!");
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE3);
            return;
        }
        if(camApp.isVideoThumb()){
            CameraLog.d(TAG, "take for  VideoThumb");
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE3);
            return;
        }
        mIsDebug  = isDebug();
        if(mIsDebug){
            CameraLog.e(TAG, "takePictureCalibration start!");
            mHandler.sendEmptyMessage(HipParamters.TAKE_PICTRUE_CALIBRATION);
            return;
        }
        switch (MODE_STATE) {
            case HipParamters.CAMERA_MODE:
            case HipParamters.VIDEO_MODE:
            case HipParamters.VID_TIME_LAPSE_MODE:
            case HipParamters.SURVEILLANCE_MODE:
                mStoragePictrueFile = Storage.EXTERALE_DCIM;
                mStorageThumbFile = Storage.EXTERALE_THUMB;
                mPictrueExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date(System.currentTimeMillis()));
                mHandler.sendEmptyMessage(HipParamters.TAKE_PICTURE_DALAY);
                return;
            case HipParamters.PIC_BURST_MODE:
                mSavedNum = mBurstNum = picture_burst_rate;
                mTakPicIndex = 0;
                mStoragePictrueFile = Storage.EXTERALE_DCIM_BUR;
                mStorageThumbFile = Storage.EXTERALE_THUMB_BUR;
                mPictrueExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date(System.currentTimeMillis()));
                mHandler.sendEmptyMessage(HipParamters.TAKE_PICTURE_DALAY_BURST);
                return;
            default:
                CameraLog
                        .e(TAG, "invild command :TAKE_PICTURE , MODE_STATE " + " = " + MODE_STATE);
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,
                        HipParamters.ERROR_CODE0);
                return;
        }
    }

    public void excuteRecord(boolean isStartRecording) {
        long spaceSize = Storage.getSpaceStorageSzie();
        if (mObservable == null) {
            mObservable = new HipCameraObservable();
        }
        camApp.sendVideoAction( "excuteRecord", true);
        mObservable.attach(mSurveillanceService.getHipCameraObserver());
        CameraLog.d(TAG, "excuteRecord spaceSize:" + spaceSize);
        if(spaceSize <= HipParamters.SPACE_MAX_SIZE){
            CameraLog.d(TAG, "excuteRecord Space does not do!");
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE3);
//            sentStopRecordIntent(HipParamters.ERROR_CODE3);
            camApp.sendVideoAction("excuteRecord", false);
            mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_FAIL);
            return;
        }
        MODE_STATE = HipSettingPreferences.getMode(this);
        CameraLog
                .e(TAG, "COM_KEY is" + " RECORD   , request record = " + isStartRecording
                        + "   state = " + mVideoContinuousRecord + "  MODE_STATE:" + MODE_STATE
                        + "  , isRecording = " + isRecording + "  , isStartRecording = "
                        + isStartRecording);
        mIsDebug  = isDebug();
        switch (MODE_STATE) {
            case HipParamters.VIDEO_MODE:
            case HipParamters.SURVEILLANCE_MODE:
                if (isRecording) {
                    if(isStartRecording){
                        CameraLog.e(TAG, "invild command :recording , "+ " RECORD =  true, " + " Error is ERROR_CODE4");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE4);
                        return;
                    }
                    if(mHandler.hasMessages(HipParamters.START_VIDEO_RCORDING)){
                        mHandler.removeMessages(HipParamters.START_VIDEO_RCORDING);
                    }
                    mHandler.sendEmptyMessageDelayed(HipParamters.VIDEO_CORDING, HipParamters.LAP_COUNT_DOWN_STEP);
                    mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                    mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                } else {
                    if(!isStartRecording){
                        camApp.sendVideoAction("excuteRecord", false);
                        CameraLog.e(TAG, "invild command :not record , "+ " RECORD =  false, " + " Error is ERROR_CODE5");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE5);
                        sentStopRecordIntent(HipParamters.ERROR_CODE5);
                        return;
                    }
                    mStorageVideoFile = Storage.EXTERALE_DCIM;
                    mStorageThumbFile = Storage.EXTERALE_THUMB;
                    if (mIsDebug) {
                        mStorageVideoFile = mStorageVideoFile.replace("sdcard1", "sdcard0");
                        mStorageThumbFile = mStorageThumbFile.replace("sdcard1", "sdcard0");
                    }
                    mVideoExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                    mHandler.sendEmptyMessage(HipParamters.START_VIDEO_RCORDING);
                }
                break;
            case HipParamters.VID_TIME_LAPSE_MODE:
                if (isRecording) {
                    if(isStartRecording){
                        CameraLog.e(TAG, "invild command :recording , "+ " RECORD =  true, " + " Error is ERROR_CODE4");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE4);
                        return;
                    }
                    if(mHandler.hasMessages(HipParamters.START_VIDEO_RCORDING)){
                        mHandler.removeMessages(HipParamters.START_VIDEO_RCORDING);
                    }
                    mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                    mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                } else {
                    if(!isStartRecording){
                        camApp.sendVideoAction( "excuteRecord", false);
                        CameraLog.e(TAG, "invild command :not record , "+ " RECORD =  false, " + " Error is ERROR_CODE5");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE5);
                        return;
                    }
                    mStorageVideoFile = Storage.EXTERALE_DCIM_VIDEO_TLP;
                    mStorageThumbFile = Storage.EXTERALE_THUMB_VIDEO_TLP;
                    if (mIsDebug) {
                        mStorageVideoFile = mStorageVideoFile.replace("sdcard1", "sdcard0");
                        mStorageThumbFile = mStorageThumbFile.replace("sdcard1", "sdcard0");
                    }
                    mVideoExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                    mHandler.sendEmptyMessage(HipParamters.START_VIDEO_RCORDING);
                }
                break;
            case HipParamters.PIC_TIME_LAPSE_MODE:
                if (isCapture) {
                    if(isStartRecording){
                        CameraLog.e(TAG, "invild command :taking picture , "+ " RECORD =  true, " + " Error is ERROR_CODE4");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE4);
                        return;
                    }
                    mStartRecordingTime =0;
                    mHandler.sendEmptyMessage(HipParamters.STOP_TAKE_PICTURE_LAPSE);
                } else {
                    if(!isStartRecording){
                        CameraLog.e(TAG, "invild command :don't take picture , "+ " RECORD =  false, " + " Error is ERROR_CODE5");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE5);
                        return;
                    }
                    mStartRecordingTime = SystemClock.uptimeMillis();
                    mStoragePictrueFile = Storage.EXTERALE_DCIM_TLP;
                    mStorageThumbFile = Storage.EXTERALE_THUMB_TLP;
                    mTakPicIndex = 0;
                    mPictrueExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                    mHandler.sendEmptyMessage(HipParamters.START_TAKE_PICTURE_LAPSE);
                }
                break;
            case HipParamters.PIC_BURST_TIME_LAPSE_MODE:
                CameraLog.e(TAG, " RECORD , mode = " + MODE_STATE + "   isCapture = " + isCapture + "   picture_burst_rate = " + picture_burst_rate);
                if (isCapture) {
                    if(isStartRecording){
                        CameraLog.e(TAG, "invild command :taking picture , "+ " RECORD =  true, " + " Error is ERROR_CODE4");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE4);
                        return;
                    }
                    mStartRecordingTime = 0;
                    mHandler.sendEmptyMessage(HipParamters.STOP_TAKE_PICTURE_LAPSE);
                } else {
                    if(!isStartRecording){
                        CameraLog.e(TAG, "invild command :don't take picture , "+ " RECORD =  false, " + " Error is ERROR_CODE5");
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE5);
                        return;
                    }
                    mStartRecordingTime = SystemClock.uptimeMillis();
                    mStoragePictrueFile = Storage.EXTERALE_DCIM_TWB;
                    mStorageThumbFile = Storage.EXTERALE_THUMB_TWB;
                    mTakPicIndex = 0;
                    mPictrueExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                    mSavedNum = mBurstNum = picture_burst_rate;
                    mHandler.sendEmptyMessage(HipParamters.START_TAKE_PICTURE_LAPSE);
                }
                break;
            default:
                CameraLog.e(TAG, "invild command :RECORD , mode" + " = " + MODE_STATE);
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL, HipParamters.ERROR_CODE0);
                break;
        }
        sendHipCameraCommand(HipParamters.RECORD, HipParamters.RESULT_OK);
    }
    private void takePicture() {
        CameraLog.e(TAG, "CameraApp takePicture,  MODE_STATE = " + MODE_STATE);
        if(mParameters == null){
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            CameraLog.e(TAG, "takePicture mParameters = " + mParameters);
            return;
        }
        CameraLog.e(TAG, "set jpeg mOrientation = " + mOrientation + ",isPreview =" + isPreview);
        if(!isPreview){
            startPreview();
            if (MODE_STATE == HipParamters.PIC_BURST_MODE) {
                mHandler.sendEmptyMessageDelayed(HipParamters.TAKE_PICTURE_DALAY_BURST, 400);
            } else {
                mHandler.sendEmptyMessageDelayed(HipParamters.TAKE_PICTURE_DALAY, 400);
            }
            CameraLog.e(TAG, "startPreview in take Picture");
            return;
        }
        if (isRecording) {
            takeASnapshot();
            return;
        }
        setTakePictureLocation(mParameters);
        mParameters.setRecordingHint(false);
        mParameters.setRotation(mOrientation);
        if (mCameraDevice != null) {
            CameraLog.e(TAG, "takePicture start");
            mParameters.setZSLMode("on");
            mParameters.setCameraMode(1);
            mCameraDevice.setParameters(mParameters);
            if (MODE_STATE == HipParamters.PIC_BURST_MODE) {
                mCameraDevice.takePicture(mHandler, new ShutterCallback(),
                        null, null, mBurstCameraPictureCallback);
            } else {
                mCameraDevice.takePicture(mHandler, new ShutterCallback(),
                        null, null, new PictureCallback());
            }
            if(mCameraLed != null){
                mCameraLed.setStatus(HipParamters.EVENT_TAKE_PICTURE_START);
            } else {
                CameraLog.e(TAG, "takePicture mCameraLed = " + mCameraLed);
            }
            setRcLedStatus(HipParamters.BT_STATUS_TAKE_PICTURE);
            if (mObservable != null) {
                mObservable.notifyObservers(HipCameraObserver.EVENT_TAKE_PICTURE_START);
            }
        }else {
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
        }
    }

    private void takeASnapshot(){
        if (mCameraDevice != null) {
            mCameraDevice.takePicture(mHandler, null, null, null, new PictureSnapshotCallback());
            mCameraLed.setStatus(HipParamters.EVENT_TAKE_PICTURE_START_ON_VIDEO_RECORD);
            setRcLedStatus(HipParamters.BT_STATUS_TAKE_PICTURE);
            if (mObservable != null) {
                mObservable
                        .notifyObservers(HipCameraObserver.EVENT_TAKE_PICTURE_START_ON_VIDEO_RECORD);
            }
        }else {
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
        }
    }

    private void takePictureForVideoThumb() {
        if (mCameraDevice != null) {
            CameraLog.e(TAG, "set jpeg mOrientation = " + mOrientation);
            //mParameters.setRotation(mOrientation);
            //mCameraDevice.setParameters(mParameters);
            mCameraDevice.takePicture(mHandler, null, null, null, new PictureCallback4VideoThumb());
        }
    }

    private void createVideoThumbnail(String videoPath) {
        CameraLog.e(TAG, "createVideoThumbnail vedio path = " + videoPath);
        Bitmap bitmap = BitmapUtils.createVideoThumbnail(videoPath);
        if(bitmap == null) {
            CameraLog.e(TAG, "createVideoThumbnail failed.");
            return;
        }
        bitmap = BitmapUtils.resizeDownByWidthAndHeight(bitmap, 480, 320);
//        String bmpPath = videoPath.replace("mp4", "bmp");
//        BitmapUtils.saveToStorage(bitmap, bmpPath);//for debug
        Bitmap newBitmap = BitmapUtils.unwrap(bitmap);

        byte[] value = BitmapUtils.compressToBytes(newBitmap);
        new SaveThumbImageTask(value, -1).execute(mVideoExcuteActiontimeStamp);
        CameraLog.d(TAG, "createVideoThumbnail end");
    }

    private void takePictureCalibration() {
        if (mCameraDevice == null) {
            mCameraDevice = CameraUtil.openCamera(
                    this, MY_CAMERA_ID, null,
                    null);
            mParameters = mCameraDevice.getParameters();
            refreshCamera();
        }
        if (!isPreview) {
            startPreview();
            mHandler.sendEmptyMessageDelayed(HipParamters.TAKE_PICTRUE_CALIBRATION, 400);
            CameraLog.e(TAG, "startPreview in takePictureCalibration");
            return;
        }

        CameraLog.e(TAG, "takePictureCalibration set jpeg mOrientation = " + mOrientation);
        mParameters.setZSLMode("on");
        mParameters.setRecordingHint(false);
        mCameraDevice.setParameters(mParameters);
        mCameraDevice.takePicture(mHandler, new ShutterCallback(), null, null,
                new PictureCallbackCalibration());
        mCameraLed.setStatus(HipParamters.EVENT_TAKE_PICTURE_START);
        setRcLedStatus(HipParamters.BT_STATUS_TAKE_PICTURE);
        if(mObservable != null)
        mObservable.notifyObservers(HipCameraObserver.EVENT_TAKE_PICTURE_START);
    }

    private void openCamera() {
        CameraLog.e(TAG, "openCamera start mCameraDevice = "+mCameraDevice);
        MODE_STATE = HipSettingPreferences.getMode(this);
        if (mCameraDevice == null) {
            mCameraDevice = CameraUtil.openCamera(
                    this, MY_CAMERA_ID, null,
                    null);
            CameraLog.e(TAG, "openCamera end mCameraDevice = "+mCameraDevice);
            if (mCameraDevice != null) {
                mParameters = mCameraDevice.getParameters();
                mHandler.sendEmptyMessage(HipParamters.REFRSH_CAMERA_DEVICE);
            }else {
                finish();
               return;
            }
        }else {
            mHandler.sendEmptyMessage(HipParamters.REFRSH_CAMERA_DEVICE);
        }
    }

    private void closeCamera() {
        CameraLog.e(TAG, "closeCamera isPreview =" + isPreview);
        if (isPreview) {
            stopPreview();
        }
        int donutjpg = android.os.SystemProperties.getInt("debug.camera.donutjpg", 0);
        if (donutjpg == 1) {
            releaseCamera();
        }
    }

    private void releaseCamera() {
        CameraLog.e(TAG, "releaseCamera ");
        if (mCameraDevice != null) {
            isCapture = isRecording = false;
            camApp.setCapture(isCapture);
            camApp.setRecording(isRecording);
            camApp.setLongRecTime(0);
            camApp.setVideoThumb(false);
            CameraHolder.instance().strongRelease();
            mCameraDevice = null;
            mMotionDetector.setDevices(null);
        }
    }

    private void startVideoRecording() {
        CameraLog.e(TAG, "startVideoRecording ");
        long spaceSize = Storage.getSpaceStorageSzie();
        CameraLog.d(TAG, "startVideoRecording spaceSize:" + spaceSize);
        if(spaceSize <= HipParamters.MEDIARECORDER_SPACE_MAX_SIZE){
            CameraLog.d(TAG, "spaceSize MAX_SIZE:" + spaceSize);
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE3);
//            sentStopRecordIntent(HipParamters.ERROR_CODE3);
            camApp.sendVideoAction("excuteRecord", false);
            if (mObservable != null) {
                mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_FAIL);
            }
            isRecording = false;
            camApp.setRecording(isRecording);
            return;
        }
        if (!isPreview) {
            startPreview();
        }
        camApp.sendVideoAction("excuteRecord", true);
        isRecording = true;
        camApp.setRecording(true);
        if (initializeRecorder()) {
            try {
                mHandler.sendEmptyMessageDelayed(HipParamters.VIDEO_CORDING, HipParamters.LAP_COUNT_DOWN_STEP);
                CameraLog.d(TAG, "startVideoRecording-> mMediaRecorder will start  !");
                if (mObservable != null) {
                    mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_START);
                }
                mMediaRecorder.start();
                CameraLog.d(TAG, "startVideoRecording -> mMediaRecorder started !");
                if (mObservable != null) {
                    mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_STARTING);
                }
                mMotionDetector.setDevices(mCameraDevice);
                mStartRecordingTime = SystemClock.uptimeMillis();
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK, "startRecorder",mVideoExcuteActiontimeStamp);

                mHandler.sendEmptyMessage(
                        HipParamters.UPDATE_RECORD_TIME);
//                mHandler.sendEmptyMessage(HipParamters.TAKE_PICTRUE_VIDEO_THUMB);
                if(mVideoQuality != 0)
                    mHandler.sendEmptyMessageDelayed(HipParamters.TAKE_PICTRUE_VIDEO_THUMB, 400);
                sendMediaAddIntent(HipParamters.MEDIA_TYPE_VIDEO, mStorageVideoFile
                        + File.separator + mVideoExcuteActiontimeStamp + ".mp4",
                        mVideoExcuteActiontimeStamp + ".mp4");
            } catch (RuntimeException e) {
                releaseMediaRecorder();
                File f = new File(mStorageVideoFile
                        + File.separator + mVideoExcuteActiontimeStamp + ".mp4",
                        mVideoExcuteActiontimeStamp + ".mp4");
                if(f.exists()){
                    f.delete();
                    sendMediaDelIntent(HipParamters.MEDIA_TYPE_VIDEO, mStorageVideoFile
                            + File.separator + mVideoExcuteActiontimeStamp + ".mp4",
                            mVideoExcuteActiontimeStamp + ".mp4");
                }
                mCameraDevice.lock();
//                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
                mHipCameraService.callBack(HipParamters.RECORD, false, HipParamters.ERROR_CODE5);
//                sentStopRecordIntent(HipParamters.ERROR_CODE5);
                camApp.sendVideoAction("excuteRecord", false);
                if (mObservable != null) {
                    mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_FAIL);
                }
                return;
            }
        }else {
            isRecording = false;
            camApp.setRecording(false);
            mCameraLed.setStatus(HipParamters.EVENT_VIDEO_RECORD_STOP);
            setRcLedStatus(HipParamters.BT_STATUS_VIDEO_RECORD_STOP);
            if (mObservable != null) {
                mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_FAIL);
            }
            releaseMediaRecorder();
            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
//            sentStopRecordIntent(HipParamters.ERROR_CODE7);
            camApp.sendVideoAction("excuteRecord", false);
        }
    }

   private void stopVideoRecording() {
        CameraLog.e(TAG, "stopVideoRecording ");
        // try to close wrap
        if (mMediaRecorder != null) {
            mCameraLed.setStatus(HipParamters.EVENT_VIDEO_RECORD_STOP);
            setRcLedStatus(HipParamters.BT_STATUS_VIDEO_RECORD_STOP);
            if(mHandler.hasMessages(HipParamters.UPDATE_RECORD_TIME)){
                mHandler.removeMessages(HipParamters.UPDATE_RECORD_TIME);
            }
            if(mHandler.hasMessages(HipParamters.VIDEO_CORDING)){
                mHandler.removeMessages(HipParamters.VIDEO_CORDING);
            }
            mVideo_lase_time = 0;
            CameraLog.d(TAG, "stopVideoRecording -> mMediaRecorder will stop !");
            mMediaRecorder.stop();
            CameraLog.e(TAG, "stopVideoRecording -> mMediaRecorder stoped !");
            releaseMediaRecorder();
            if(mVideoQuality == 0)
                createVideoThumbnail(mRecordVideoFile);
            CameraLog.d(TAG, "stopVideoRecording -> camera device lock !");
            mCameraDevice.lock();
            if (mVideoContinuousRecord == VIDEO_RECORD_STATE.NOT) {
                CameraLog.d(TAG, "stopVideoRecording -> video stop !");
                if (mObservable != null) {
                    mObservable.notifyObservers(HipCameraObserver.EVENT_VIDEO_RECORD_STOP);
                }
                sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK, "stopRecorder", null);
                camApp.sendVideoAction("excuteRecord", true);
                mRecordTime = 0;
                mHipCameraService.callBack(HipParamters.RECORD, true, HipParamters.NO_ERROR_CODE);
                camApp.setLongRecTime(mRecordTime);
                camApp.sendVideoAction("excuteRecord", false);
                Intent in = HipSettingPreferences.getMEMVIO(this);
                sendBroadcast(in);
            } else {
                mVideoExcuteActiontimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                mHandler.sendEmptyMessage(HipParamters.START_VIDEO_RCORDING);
            }
        }
    }

    private void intGPS() {
        if (mLocationManager != null) {
            CameraLog.d(TAG, " intGPS engps:"  + engps );
            mLocationManager.recordLocation(engps);
        }
    }

    private void initAutoFlip(boolean isAuto) {
        if (isAuto) {
            mAngleListener.enabel();
        } else {
            mAngleListener.disable();
        }
        if (mTiltAngle > 45 && mTiltAngle < 135) {
            if (!mHandler.hasMessages(HipParamters.TOP_TRIGGER)) {
                mHandler.sendEmptyMessage(HipParamters.TOP_TRIGGER);
            }
        } else if (mTiltAngle > -135 && mTiltAngle < -45) {
            if (isAuto_flip) {
                if (!mHandler.hasMessages(HipParamters.BOTTOM_TRIGGER)) {
                    mHandler.sendEmptyMessage(HipParamters.BOTTOM_TRIGGER);
                }
            } else {
                if (!mHandler.hasMessages(HipParamters.TOP_TRIGGER)) {
                    mHandler.sendEmptyMessage(HipParamters.TOP_TRIGGER);
                }
            }
        }
    }

    private void refreshCamera() {
        int swicth_mode = HipSettingPreferences.getMode(this);
        MODE_STATE = swicth_mode;
        if (mCameraDevice != null) {
            mParameters = HipSettingPreferences.refreshCameraSetting(this,
                    mParameters);
        } else {
            mCameraDevice = CameraUtil.openCamera(
                    this, MY_CAMERA_ID, null,
                    null);
            mParameters = mCameraDevice.getParameters();
            mParameters = HipSettingPreferences.refreshCameraSetting(this,
                    mParameters);
        }

        initCameraState(swicth_mode);

        CameraLog.e(TAG, "refreshCamera   swicth_mode  =" + ":" + swicth_mode);
        intGPS();

        isAuto_flip = HipSettingPreferences.get4Setting(this, HipParamters.AUTO_FLIP_STATE) == HipParamters.OPT_0;
        CameraLog.e(TAG, "refreshCamera   isAuto_flip  " + ": " + isAuto_flip);
        initAutoFlip(isAuto_flip);

        int video_size_opt = HipParamters.OPT_0;
        if (swicth_mode != HipParamters.SURVEILLANCE_MODE) {
            video_size_opt = HipSettingPreferences.get4SwitchMode(this, HipParamters.VIDEO_SIZE);
            if (mObservable != null) {
                mObservable.detachAll();
            }
            mObservable = null;
        } else {
            video_size_opt = HipParamters.OPT_3;
            CameraLog.e(TAG, "refreshCamera   mSurveillanceService  " + " : " + mSurveillanceService);
            if (mSurveillanceService != null) {
                if (mObservable == null) {
                    mObservable = new HipCameraObservable();
                }
                mObservable.attach(mSurveillanceService.getHipCameraObserver());
            }
        }

        if (mCameraDevice != null) {
            mCameraDevice.setParameters(mParameters);
        }

        isZSL = HipSettingPreferences.get4Setting(this, HipParamters.ZSL) == HipParamters.OPT_0;
        if (!isRecording) {
            initCamcorderProfile(video_size_opt);
        }
        mVideoQuality = video_size_opt;
    }

    private void initCameraState(int state) {
        MODE_STATE = state;
        int pic_time_opt, pic_burst_opt, vid_time_opt;
        CameraLog.e(TAG, "#initCameraState  MODE_STATE " + " = " + MODE_STATE);
        switch (MODE_STATE) {
            case HipParamters.CAMERA_MODE:
            case HipParamters.VIDEO_MODE:
                picture_lapse_burst_enable = false;
                picture_burst_enable = false;
                picture_lapse_enable = false;
                video_lapse_enable = false;
                mSavedNum = mBurstNum = picture_burst_rate = 1;
                break;
            case HipParamters.PIC_BURST_MODE:
                picture_lapse_burst_enable = false;
                picture_burst_enable = true;
                picture_lapse_enable = false;
                video_lapse_enable = false;
                pic_burst_opt = HipSettingPreferences.get4Setting(this,
                        HipParamters.PIC_BURST_RATE);
                picture_burst_rate = HipSettingPreferences.getPictrueBurst(pic_burst_opt);
                mSavedNum = mBurstNum = picture_burst_rate;
                break;
            case HipParamters.PIC_TIME_LAPSE_MODE:
                picture_lapse_burst_enable = false;
                picture_burst_enable = false;
                picture_lapse_enable = true;
                video_lapse_enable = false;
                pic_time_opt = HipSettingPreferences.get4Setting(this,
                        HipParamters.PIC_LAPSE_TIME);
                picture_lapse_time = HipSettingPreferences.getLapsetime(pic_time_opt);
                mSavedNum = mBurstNum = picture_burst_rate = 1;
                break;
            case HipParamters.PIC_BURST_TIME_LAPSE_MODE:
                picture_lapse_burst_enable = true;
                picture_burst_enable = false;
                picture_lapse_enable = false;
                video_lapse_enable = false;
                pic_burst_opt = HipSettingPreferences.get4Setting(this,
                        HipParamters.PIC_LAPSE_BURST_RATE);
                picture_burst_rate = HipSettingPreferences.getPictrueBurst(pic_burst_opt);
                pic_time_opt = HipSettingPreferences.get4Setting(this,
                        HipParamters.PIC_LAPSE_BURST_TIME);
                picture_lapse_time = HipSettingPreferences.getLapsetime(pic_time_opt);
                mSavedNum = mBurstNum = picture_burst_rate;
                break;
            case HipParamters.VID_TIME_LAPSE_MODE:
                picture_lapse_burst_enable = false;
                picture_burst_enable = false;
                picture_lapse_enable = false;
                video_lapse_enable = true;
                vid_time_opt = HipSettingPreferences.get4Setting(this,
                        HipParamters.VID_LAPSE_TIME);
                video_lapse_time = HipSettingPreferences.getLapsetime(vid_time_opt);
                break;
            case HipParamters.SURVEILLANCE_MODE:
                picture_lapse_burst_enable = false;
                picture_burst_enable = false;
                picture_lapse_enable = false;
                video_lapse_enable = false;
                mSavedNum = mBurstNum = picture_burst_rate = 1;
                break;
            default:
                picture_lapse_burst_enable = false;
                picture_burst_enable = false;
                picture_lapse_enable = false;
                video_lapse_enable = false;
                mSavedNum = mBurstNum = picture_burst_rate = 1;
                break;
        }
        CameraLog.d(TAG, "mSavedNum:" + mSavedNum + ",mBurstNum:" + mBurstNum
                + ",picture_burst_rate:" + picture_burst_rate);
    }

    private void sendHipCameraCommand(String key, String value) {
        Intent intent = new Intent(HipParamters.CAMERA_RESPONE_STATUS);
        intent.putExtra(key, value);
        sendBroadcast(intent);
    }

    private void sendIntent(String key, String value) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(key, value);
        sendBroadcast(intent);
    }

    private void sendIntent(String key, String value, int error) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(key, value);
        intent.putExtra(HipParamters.RESULT_ERROR, error);
        sendBroadcast(intent);
    }

    private void sendIntent(String key, String value, String action, String filename) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(key, value);
        intent.putExtra("action", action);
        intent.putExtra("timestamp",filename);
        sendBroadcast(intent);
    }

    private void sendVideoStatus(long laptime, String value) {
        Intent intent = new Intent(HipParamters.CAMERA_RESPONE_STATUS);
        intent.putExtra("mode", "video");
        intent.putExtra("action", value);
        intent.putExtra("time", laptime);
        CameraLog.d(TAG, "send CE record time:" + laptime);
        sendBroadcast(intent);
    }

    private void sendMediaAddIntent(int type, String path,String name) {
        Intent intent = new Intent(HipParamters.EVENT_CONTIF_CREATE);
        intent.putExtra("type", type);
        intent.putExtra("path", path);
        intent.putExtra("name", name);
        CameraLog.d(TAG, "#sendMediaAddIntent type:" + type + ",path:" + path + ",name:" + name);
        sendBroadcast(intent);
    }
    private void sendMediaDelIntent(int type, String path,String name) {
        Intent intent = new Intent(HipParamters.EVENT_CONTIF_DELETE);
        intent.putExtra("type", type);
        intent.putExtra("path", path);
        intent.putExtra("name", name);
        CameraLog.d(TAG, "#sendMediaDelIntent type:" + type + ",path:" + path + ",name:" + name);
        sendBroadcast(intent);
    }

    private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
        long seconds = milliSeconds / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        StringBuilder timeStringBuilder = new StringBuilder();

        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);

            timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');

        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);

        // Centi seconds
        if (displayCentiSeconds) {
            timeStringBuilder.append('.');
            long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
            if (remainderCentiSeconds < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(remainderCentiSeconds);
        }
        return timeStringBuilder.toString();
    }

    private void updateRecordingTime() {
        if (!isRecording) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        long delta = now - mStartRecordingTime;
        String timetext = null;
        long targetNextUpdateDelay;
        if (video_lapse_enable) {
            timetext = millisecondToTimeString(getTimeLapseVideoLength(delta), true);
            //timetext = millisecondToTimeString(delta, false);
            mRecorTimeLapseTable.setVisibility(View.VISIBLE);
            targetNextUpdateDelay = video_lapse_time;
            //targetNextUpdateDelay = 1000;
        }else {
            timetext = millisecondToTimeString(delta, false);
            targetNextUpdateDelay = 1000;
        }
        mRecordingTimeView.setText("");
        mRecordingTimeView.setVisibility(View.VISIBLE);
        CameraLog.e(TAG, "set time " + timetext);
        mRecordingTimeView.setText(timetext);
        long actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
        mHandler.sendEmptyMessageDelayed(
                HipParamters.UPDATE_RECORD_TIME, actualNextUpdateDelay);
        }

    private long getTimeLapseVideoLength(long deltaMs) {
        double numberOfFrames = (double) deltaMs / video_lapse_time;
        return (long) (numberOfFrames / mProfile.videoFrameRate * 1000);
    }

    private class CameraHandler extends Handler {
        public CameraHandler() {
            super(Looper.getMainLooper());
        }

        public boolean waitDone() {
            final Object waitDoneLock = new Object();
            final Runnable unlockRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (waitDoneLock) {
                        waitDoneLock.notifyAll();
                    }
                }
            };

            synchronized (waitDoneLock) {
                mHandler.post(unlockRunnable);
                try {
                    waitDoneLock.wait();
                } catch (InterruptedException ex) {
                    Log.v(TAG, "waitDone interrupted");
                    return false;
                }
            }
            return true;
        }
        private boolean isSleep = false;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HipParamters.UPDATE_RECORD_TIME: {
                    updateRecordingTime();
                    break;
                }
                case HipParamters.TAKE_PICTURE_DALAY: {
                    if(isRecording){
                        camApp.setLiveShot(true);
                    }else {
                        isCapture = true;
                    }
                    takePicture();
                    break;
                }
                case HipParamters.TAKE_PICTURE_DALAY_BURST: {
                    isCapture = true;
                    takePicture();
                    break;
                }
                case HipParamters.STOP_VIDEO_RCORDING: {
                    if(this.hasMessages(HipParamters.VIDEO_CORDING)){
                        this.removeMessages(HipParamters.VIDEO_CORDING);
                    }
                    mVideo_lase_time = 0;
                    stopVideoRecording();
                    break;
                }
                case HipParamters.STOP_PREVIEW_MSG: {
                    finish();
                    int opt = HipSettingPreferences.get4Setting(MyCamera.this, HipParamters.AUTO_START);
                    CameraLog.e(TAG, "command = STOP_PREVIEW_MSG , opt = " + opt);
                    if (HipSettingPreferences.get4Setting(MyCamera.this, HipParamters.AUTO_START) == HipParamters.OPT_0) {
                        releaseCamera();
                    } else {
                        
                    }
                    break;
                }
                case HipParamters.START_PREVIEW_MSG: {
                    startPreview();
                    break;
                }
                case HipParamters.START_VIDEO_RCORDING: {
                    isRecording = true;
                    camApp.setRecording(isRecording);
                    startVideoRecording();
                    break;
                }
                case HipParamters.VIDEO_CORDING: {
                    mVideo_lase_time+= HipParamters.LAP_COUNT_DOWN_STEP;
                    camApp.setLongRecTime(mVideo_lase_time);
                    camApp.sendVideoAction("excuteRecord", true);
                    this.sendEmptyMessageDelayed(HipParamters.VIDEO_CORDING,HipParamters.LAP_COUNT_DOWN_STEP);
                    break;
                }
                case HipParamters.TOP_TRIGGER: {
                    mOrientation = 0;
                    if (mCameraDevice != null) {
                        mCameraDevice.setDisplayOrientation(0);
                        CameraLog.d(TAG, "tiltAngle_z mCameraDevice.setDisplayOrientation:" + 0 + "  mOrientation="+mOrientation);
                    }
                    break;
                }
                case HipParamters.BOTTOM_TRIGGER: {
                    mOrientation = 180;
                    if (mCameraDevice != null) {
                        mCameraDevice.setDisplayOrientation(180);
                        CameraLog.d(TAG, "tiltAngle_z mCameraDevice.setDisplayOrientation:" + 180 + "   mOrientation ="+mOrientation);
                    }
                    break;
                }
                case HipParamters.START_TAKE_PICTURE_LAPSE: {
                    isStopLapsePic = false;
                    isCapture = true;
                    CameraLog.e(TAG, "command = START_TAKE_PICTURE_LAPSE ");
                    lapseTakePicture();
                }
                    break;
                case HipParamters.STOP_TAKE_PICTURE_LAPSE: {
                    CameraLog.e(TAG, "command = STOP_TAKE_PICTURE_LAPSE ");
                    isCapture = false;
                    if (this.hasMessages(HipParamters.START_TAKE_PICTURE_LAPSE)) {
                        this.removeMessages(HipParamters.START_TAKE_PICTURE_LAPSE);
                    }
                    mHipCameraService.callBack(HipParamters.RECORD, true, HipParamters.NO_ERROR_CODE);
                    isStopLapsePic = true;
                }
                    break;
                case HipParamters.ENABLE_DETECT_COMMAND:{
                    CameraLog.e(TAG, "ENABLE_DETECT_COMMAND");
                    mMotionDetector.passParameters(mParameters);
                    mMotionDetector.setDevices(mCameraDevice);
                    mMotionDetector.setIntent(((Intent) msg.obj));
                    sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK, HipParamters.NO_ERROR_CODE);
                }
                break;
                case HipParamters.DISABLE_DETECT_COMMAND:{
                    CameraLog.e(TAG, "DISABLE_DETECT_COMMAND");
                    mObservable = null;
                    mMotionDetector.setIntent(((Intent) msg.obj));
                    if (!isRecording && !isCapture) {
                        initCameraState(MODE_STATE);
                    }else if (isRecording){
                        mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                        mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                    }
                    sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_OK, HipParamters.NO_ERROR_CODE);
                }
                break;
                case HipParamters.TAKE_PICTRUE_VIDEO_THUMB : {
                    camApp.setVideoThumb(true);
                    takePictureForVideoThumb();
                }
                break;
                case HipParamters.REFRSH_CAMERA_DEVICE : {
                    CameraLog.d(TAG, "REFRSH_CAMERA_DEVICE starting, isRecording = " + isRecording);
                    if (!isRecording) {
                        refreshCamera();
                    }
                }
                break;
                case HipParamters.STOP_CAMERA_DEVICE :{
                    CameraLog.d(TAG, "STOP_CAMERA_DEVICE starting");
                    if(isRecording){
                        if(mHandler.hasMessages(HipParamters.START_VIDEO_RCORDING)){
                            mHandler.removeMessages(HipParamters.START_VIDEO_RCORDING);
                            mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                            mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                        }
                    } else {
                        releaseCamera();
                    }
                    MyCamera.this.finish();
                }
                break;
                case HipParamters.TAKE_PICTRUE_CALIBRATION: {
                    CameraLog.d(TAG, "TAKE_PICTRUE_CALIBRATION starting");
                    takePictureCalibration();
                }
                    break;
                default:
                    break;
            }
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
            mOrientation = CameraUtil.roundOrientation(orientation, mOrientation);
            CameraLog.d(TAG, "onOrientationChanged mOrientation:" + mOrientation);
        }
   }

    class AngleListener implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;
        private static final float RADIANS_TO_DEGREES = (float) (180 / Math.PI);
        private static final long NANOS_PER_MS = 1000000;
        private SensorManager mSensorManager;
        private boolean mEnabled = false;
        private final int RATE = 5000;
        private Sensor mSensor;
        private long mLastFilteredTimestampNanos;

        public void enabel() {
            mSensorManager = (SensorManager)MyCamera.this.getSystemService(Context.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mSensor != null) {
                // Create listener only if sensors do exist
                if (mEnabled == false) {
                    Log.d(TAG, "OrientationEventListener enabled");
                    mSensorManager.registerListener(this, mSensor, RATE);
                    mEnabled = true;
                    mLastFilteredTimestampNanos = Long.MIN_VALUE;
                }
            }
        }

        public void disable() {
            if ( mSensor != null ) {
                if (mEnabled == true) {
                    Log.d(TAG, "OrientationEventListener disabled");
                    mSensorManager.unregisterListener(this);
                    mEnabled = false;
                }
            }
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[_DATA_X];
            float y = event.values[_DATA_Y];
            float z = event.values[_DATA_Z];

            final long now = event.timestamp;
            final long then = mLastFilteredTimestampNanos;
            if (now < then + 500 * NANOS_PER_MS
                    || (x == 0 && y == 0 && z == 0)) {
                return;
            }
            mLastFilteredTimestampNanos = now;
            float magnitude = FloatMath.sqrt(x * x + y * y + z * z);
            final int tiltAngle = (int) Math.round(
                    Math.asin(z / magnitude) * RADIANS_TO_DEGREES);
            mTiltAngle = tiltAngle;
            //Log.d(TAG, "onSensorChanged tiltAngle_z=" + tiltAngle);
            if(!isRecording){
                if (tiltAngle > 45 && tiltAngle < 135) {
                    if (isTopTrigger) {
                        isTopTrigger = false;
                        mDirection = 0;
                        CameraLog.d(TAG, "tiltAngle_z top trigger,isAuto_flip:" + isAuto_flip);
                        if(!mHandler.hasMessages(HipParamters.TOP_TRIGGER)){
                            mHandler.sendEmptyMessage(HipParamters.TOP_TRIGGER);
                        }
                    }
                } else {
                    isTopTrigger = true;
                }
                if (tiltAngle > -135 && tiltAngle < -45) {
                    if (isBottomTrigger) {
                        isBottomTrigger = false;
                        mDirection = 1;
                        CameraLog.d(TAG, "tiltAngle_z bottom trigger,isAuto_flip:" + isAuto_flip);
                        if (isAuto_flip) {
                            if (!mHandler.hasMessages(HipParamters.BOTTOM_TRIGGER)) {
                                mHandler.sendEmptyMessage(HipParamters.BOTTOM_TRIGGER);
                            }
                        } else {
                            if (!mHandler.hasMessages(HipParamters.TOP_TRIGGER)) {
                                mHandler.sendEmptyMessage(HipParamters.TOP_TRIGGER);
                            }
                        }
                    }
                } else {
                    isBottomTrigger = true;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    }

    private BluetoothProfile.ServiceListener mBluetoothProfileServiceListener =
            new BluetoothProfile.ServiceListener() {

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    CameraLog.e(TAG, "mBluetoothProfileServiceListener  onServiceConnected isRecording="+isRecording);
                    mBluetoothHeadset = (BluetoothHeadset) proxy;
                    if(!isRecording){
                        mBluetoothHeadset.connectAudio();
                    }
                }

                @Override
                public void onServiceDisconnected(int profile) {
                    CameraLog.e(TAG, "mBluetoothProfileServiceListener  onServiceDisconnected  isRecording="+isRecording);
                    if(isRecording && mProfile.audioChannels == 1){
                        mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                        mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                    }
                    mBluetoothHeadset = null;
                }
    };

    private void initBT() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                adapter.getProfileProxy(this, mBluetoothProfileServiceListener,
                        BluetoothProfile.HEADSET);
            }
        } catch (Exception e) {
            CameraLog.e(TAG, "e ="+e.getMessage());
        }
    }

    public class DetectorBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            CameraLog.e(TAG, "DetectorBroadcastReceiver  =" + intent.getAction());
        }

    }

    private void writeThumbFile(String path,String fromPath) {
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        String thumbFile = path + File.separator + "thumb.jpg";
        CameraLog.d(TAG, "Thumb thumb.jpg:" + thumbFile);
        //Storage.writeFile(imagedate,thumbFile);
        String cp = "cp " + fromPath + " " + thumbFile;
        CameraLog.e(TAG,"cp=" + cp);
        execCommand(cp);
    }

    private void execCommand(String command){
        try {
            CameraLog.e(TAG,"execCommand getRuntime!");
            Runtime runtime = Runtime.getRuntime();
            CameraLog.e(TAG,"execCommand runtime.exec()!");
            Process proc = runtime.exec(command);
            CameraLog.e(TAG,"execCommand proc.waitFor()!");
            if (proc.waitFor() != 0) {
                CameraLog.e(TAG,"exit value = " + proc.exitValue());
            }
        } catch (Exception e) {
            CameraLog.e(TAG,"execCommand exception!");
        }
    }

    private void setPerview(int x,int y) {
        if (mCameraDevice == null) {
            return;
        }
//        Size size = mCameraDevice.getParameters().getPreviewSize();
//        CameraLog.e(TAG,"hal_w:" + size.width + ",hal_h:" + size.height);
        CameraLog.e(TAG, "mTargetHight =" + mTargetHight + ",mTargetWidth =" + mTargetWidth);

        int width = 1920;
        double wb = mTargetWidth / (double) (width);
        // int hight =(int)(wb * size.height);
        int hight = 320;
        CameraLog.e(TAG, "width:" + width + ",hight:" + hight + ",wb:" + wb);
        CameraLog.e(TAG, "x =" + x + ",y =" + y);

        int x1 = 0;
        int x2 = -(width - mTargetWidth);
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,hight,0,0);
        //int xx = mTextureView.getLeft() + x;
        int xx = x;
        if(xx > x1 || xx < x2){
            CameraLog.e(TAG,"Out of range, can not be moved:" + x1 + ">x>" + x2 + ",xx:" + xx);
        }else{
            params.x = xx;
            CameraLog.e(TAG,"move:" + x1 + ">x>" + x2 + ",xx:" + xx);
        }
        int cy = mTargetHight/2 - hight/2 + y;
        params.y = cy;

//        mTextureView.setLayoutParams(params);
        mSurfaceView.setLayoutParams(params);
        CameraLog.e(TAG, "params width:" + mSurfaceView.getWidth() + ",params hight:"
                + mSurfaceView.getHeight());
    }

    private ServiceConnection mHipCameraServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder b) {
            mHipCameraService = ((com.android.camera.hip.dragonfly.HipCameraService.LocalBinder) b).getService();
            CameraLog.d(TAG, "onServiceConnected->setListener()" + mHipCameraService);
            mHipCameraService.setListener(new HipCameraServiceListener());
            mHipCameraService.excutePendingAction();
            excuteIntent("'");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (mHipCameraService != null) {
                mHipCameraService.setListener(null);
                mHipCameraService = null;
            }
        }
    };
    private ServiceConnection mSurveillanceServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSurveillanceService = ((com.android.surveillance.SurveillanceService.LocalBinder) service)
                    .getService();
            CameraLog.d(TAG,
                    "#mSurveillanceServiceConnection onServiceConnected , mSurveillanceService = "
                            + mSurveillanceService);
            int swicth_mode = HipSettingPreferences.getMode(MyCamera.this);
            if (swicth_mode == HipParamters.SURVEILLANCE_MODE) {
                if(mObservable == null){
                    mObservable = new HipCameraObservable();
                }
                mObservable.attach(mSurveillanceService.getHipCameraObserver());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CameraLog.d(TAG, "#mSurveillanceServiceConnection onServiceConnected" );
            mSurveillanceService = null;
        }
    };

    private ServiceConnection mLedServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder b) {
            ledService = IRcLedService.Stub.asInterface(b);
            CameraLog.d(TAG, "mLedServiceConnection onServiceConnected ledService =" + ledService);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            ledService = null;
            CameraLog.d(TAG, "mLedServiceConnection onServiceDisconnected ledService =" + ledService);
        }
    };

    class HipCameraServiceListener implements HipCameraService.Listener {
        @Override
        public int queryCameraStatus() {
            CameraLog.d(TAG, "########## queryCameraStatus ###########  mCameraActivityStatus="
                    + mCameraActivityStatus);
            return mCameraActivityStatus;
        }

        @Override
        public boolean isPreview() {
            return isPreview;
        }

        @Override
        public void excuteAction(final Intent intent) {
            mHandler.post(new Runnable() {
                public void run() {
                    if(intent.hasExtra("catemp")){
                        int catemp = intent.getIntExtra("catemp", -1);
                        CameraLog.d(TAG, "catemp =" + catemp);
                        if(mMotionDetector != null){
                            if(catemp == 0){
                                mMotionDetector.setEable(false);
                            } else if(catemp == 1){
                                mMotionDetector.setEable(true);
                            }
                        } else {
                            CameraLog.d(TAG, "catemp mMotionDetector=" + mMotionDetector);
                        }
                        return;
                    }
                    CameraLog.d(TAG, "excuteAction : action = " + intent.getAction());
                    Message msg = mHandler.obtainMessage();
                    msg.obj = intent;
                    boolean isEnable = intent.getBooleanExtra("enable", true);
                    msg.what = isEnable ? HipParamters.ENABLE_DETECT_COMMAND
                            : HipParamters.DISABLE_DETECT_COMMAND;
                    mHandler.sendMessage(msg);
                }
            });
        }

        @Override
        public long getRecordingTime() {
            if (isRecording || isCapture) {
                return SystemClock.uptimeMillis() - mStartRecordingTime;
            } else {
                return 0;
            }
        }

        @Override
        public boolean isRecording() {
            CameraLog.e(TAG, "  isRecording = " + isRecording);
            return isRecording;
        }

        @Override
        public boolean isCaptrue() {
            CameraLog.e(TAG, "  isCapture = " + isCapture);
            return isCapture;
        }

        @Override
        public void excuteRecordCommand(final boolean f) {
            mHandler.post(new Runnable() {
                public void run() {
                    CameraLog.d(TAG, "########## excuteRecordCommand ###########  ");
                    excuteRecord(f);
                }
            });
        }

        @Override
        public void excuteTakePictueCommand() {
            mHandler.post(new Runnable() {
                public void run() {
                    CameraLog.d(TAG, "########## excuteTakePictueCommand ###########  ");
                    excuteTakePicture();
                }
            });
        }

        @Override
        public void refreshCamera() {
            CameraLog.d(TAG, "########## refreshCamera ###########  ");
            mHandler.sendEmptyMessage(HipParamters.REFRSH_CAMERA_DEVICE);
        }

        @Override
        public void stopCamera() {
            mHandler.sendEmptyMessage(HipParamters.STOP_CAMERA_DEVICE);
        }

        @Override
        public void pauseCamera() {
            CameraLog.d(TAG, "########## pauseCamera ###########  ");
            mHandler.sendEmptyMessage(HipParamters.STOP_PREVIEW_MSG);
        }

        @Override
        public void startPreivew() {
            mHandler.sendEmptyMessage(HipParamters.START_PREVIEW_MSG);
        }

        @Override
        public boolean surFaceViewReady() {
            return mSurfaceHolder != null;
        }

        @Override
        public void setInitialState() {
            setInitialActivityState();
        }

    }

    private void setInitialActivityState() {
        CameraLog.d(TAG, " setInitialActivityState start ..... ");

        if (mHandler.hasMessages(HipParamters.UPDATE_RECORD_TIME)) {
            mHandler.removeMessages(HipParamters.UPDATE_RECORD_TIME);
        }
        if (mHandler.hasMessages(HipParamters.TAKE_PICTURE_DALAY)) {
            mHandler.removeMessages(HipParamters.TAKE_PICTURE_DALAY);
        }
        if (mHandler.hasMessages(HipParamters.TAKE_PICTURE_DALAY_BURST)) {
            mHandler.removeMessages(HipParamters.TAKE_PICTURE_DALAY_BURST);
        }
        if (isRecording) {
            mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
        }

        isRecording = false;
        isCapture = false;
    }

    private void bindHipCameraService() {
        CameraLog.d(TAG, "########## bindHipCameraService ###########  ");
        Intent in = new Intent(this, HipCameraService.class);
        bindService(in, mHipCameraServiceConnection, Context.BIND_AUTO_CREATE);
        in = new Intent(this, SurveillanceService.class);
        bindService(in, mSurveillanceServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindHipCameraService() {
        CameraLog.d(TAG, "unBindHipCameraService() mHipCameraServiceConnection = " + mHipCameraServiceConnection);
        if (mHipCameraServiceConnection != null) {
            CameraLog.d(TAG, "unBindHipCameraService() mHipCameraService = " + mHipCameraService);
            if (mHipCameraService != null) {
                mHipCameraService.setListener(null);
                CameraLog.d(TAG, "setListener() Listener = null");
                mHipCameraService = null;
            }
            unbindService(mHipCameraServiceConnection);
        }
        if (mSurveillanceServiceConnection != null) {
            unbindService(mHipCameraServiceConnection);
        }
    }

    private void bindLedService() {
        CameraLog.d(TAG, "########## bindLedService ###########");
        Intent in = new Intent("com.gatt.remotecontrol.server.RcLedService");
        bindService(in, mLedServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindLedService() {
        CameraLog.d(TAG, "########## unBindLedService ###########");
        if (mLedServiceConnection != null) {
            unbindService(mLedServiceConnection);
        }
    }

    private BroadcastReceiver mHipActionBroadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            CameraLog.d(TAG, "mHipActionBroadcast : action = " + intent.getAction());
            if (HipParamters.CAMERA_ACTION.equals(intent.getAction())) {
                if (intent.hasExtra(HipParamters.COM_KEY)) {
                    if (HipParamters.STOP_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
                        CameraLog.d(TAG, "HipActionBroadcast isRecording:" + isRecording);
                        if (!isRecording) {
                            CameraLog.d(TAG, "HipActionBroadcast STOP_PREVIEW");
                            mHandler.sendEmptyMessage(HipParamters.STOP_PREVIEW_MSG);
                        } else {
                            sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE1);
                        }
                    } else if (HipParamters.START_PREVIEW.equals(intent.getStringExtra(HipParamters.COM_KEY))) {
                        CameraLog.d(TAG, "HipActionBroadcast START_PREVIEW");
                        mHandler.sendEmptyMessage(HipParamters.START_PREVIEW_MSG);
                    } else {
                        sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE0);
                    }
                } else {
                    sendIntent(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL,HipParamters.ERROR_CODE0);
                }
            }
        }

    };

    private final BroadcastReceiver broadcastRec = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CameraLog.d(TAG, "action:" + action);
            if (action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals("android.intent.action.ACTION_MEDIA_BAD_REMOVAL")
                    || action.equals(Intent.ACTION_MEDIA_SHARED)) {
                CameraLog.d(TAG, "sdcard not Mounting!");
                if(isRecording){
                    mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                    mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                }
                if(isCapture){
                    mHandler.sendEmptyMessage(HipParamters.STOP_TAKE_PICTURE_LAPSE);
                }
            }
        }
    };

    private BroadcastReceiver mBluetoothHandsetBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent in) {
            CameraLog.d(TAG, "#BluetoothHandset -> action = " + in.getAction());
           if(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(in.getAction())){
                int prevState = in.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE,
                        BluetoothProfile.STATE_DISCONNECTED);
                int newState = in.getIntExtra(BluetoothProfile.EXTRA_STATE,
                        BluetoothProfile.STATE_DISCONNECTED);
                BluetoothDevice btDevice = in.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if(mBluetoothHeadset == null){
                        initBT();
                    }
                    if (!isRecording) {
                        if (mBluetoothHeadset != null
                                && mBluetoothHeadset.getConnectedDevices().size() > 0) {
                            mBluetoothHeadset.connectAudio();
                        }
                    }
                } else if (prevState == BluetoothProfile.STATE_CONNECTED) {
                    if (isRecording && mProfile.audioChannels == 1) {
                        mVideoContinuousRecord = VIDEO_RECORD_STATE.NOT;
                        mHandler.sendEmptyMessage(HipParamters.STOP_VIDEO_RCORDING);
                    }
                }
                CameraLog.d(TAG, "action:" + "  prevState = " + prevState + "  newState ="
                        + +newState + "  device =" + btDevice + "  isRecording=" + isRecording);
           }
        }
    };

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            CameraLog.d(TAG, "surfaceCreated " );

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CameraLog.d(TAG, "surfaceChanged format=" + format + " width = " + width + "  height="
                    + height);
            mSurfaceHolder = holder;
            excuteIntent(HipParamters.START_PREVIEW);
//            startPreview();
//            mHipCameraService.excute4Activity(1);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCameraDevice != null) {
                mCameraDevice.stopPreview();
            }
            isCapture = false;
            isRecording = false;
            isPreview = false;
            camApp.setCapture(false);
            camApp.setLongRecTime(0);
            camApp.setRecording(false);
            CameraLog.d(TAG, " surfaceDestroyed");
            mSurfaceHolder = null;
        }

    };

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCameraDevice.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

    private static boolean isDebug(){
        boolean isDebug = android.os.SystemProperties.getBoolean("debug.factorytest.dir", false);
        CameraLog.d(TAG, "isDebug:" + isDebug);
        return isDebug;
    }

    private boolean isExternalFat32() {
        String type = android.os.SystemProperties.get(Storage.EXTERALE_PROPERTY_TYPE);
        CameraLog.d(TAG, "isExternalFat32  type : " + type);
        return Storage.EXTERALE_TYPE_FAT32.equals(type);
    }

    private void setRecordLocation() {
        boolean isOpenGps = android.os.SystemProperties.getBoolean("persist.sys.gps.enabled", false);
        CameraLog.d(TAG, "setRecordLocation isOpenGps:" + isOpenGps);
        Location loc = mLocationManager.getCurrentLocation();
        CameraLog.d(TAG, "setRecordLocation Location loc:" + loc);
        if (isOpenGps && loc != null) {
            mMediaRecorder.setLocation((float) loc.getLatitude(),
                    (float) loc.getLongitude());
        }
    }

    private void setTakePictureLocation(Parameters mParameters) {
        boolean isOpenGps = android.os.SystemProperties.getBoolean("persist.sys.gps.enabled", false);
        CameraLog.d(TAG, "setTakePictureLocation isOpenGps:" + isOpenGps);
        if (isOpenGps && mLocationManager != null) {
            Location loc = mLocationManager.getCurrentLocation();
            if (loc != null) {
                CameraLog.d(TAG,"setTakePictureLocation Latitude:" + loc.getLatitude() + ",Longitude:" + loc.getLongitude());
            } else {
                CameraLog.d(TAG, "setTakePictureLocation Location loc:" + loc);
                return;
            }
            CameraUtil.setGpsParameters(mParameters, loc);
        }else {
            mParameters.removeGpsData();
        }
    }

    private void sentStopRecordIntent(int error) {
        Intent intent = new Intent(HipParamters.VIDEO_STOP_RECORDING);
        intent.putExtra("recordTime", mRecordTime);
        intent.putExtra("error", error);
//        sendBroadcast(intent);
    }
    private void excuteIntent(String command){
        synchronized (waitDoneLock) {
            CameraLog.d(TAG, " this return server to excute action !" + command);
            if(mSurfaceHolder != null && mHipCameraService != null && mExcuteIntent != null){
                mHipCameraService.excute4Activity(mExcuteIntent);
            }
        }
    }
    private class HipCameraErrorCallback implements android.hardware.Camera.ErrorCallback {

        @Override
        public void onError(int error, Camera camera) {
            if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED
                    || error == android.hardware.Camera.CAMERA_ERROR_UNKNOWN) {
                CameraLog.e(TAG, "#HipCameraErrorCallback error : " + error);
                mCameraLed.setStatus(HipParamters.EVENT_VIDEO_RECORD_STOP);
                setRcLedStatus(HipParamters.BT_STATUS_VIDEO_RECORD_STOP);
                if(isRecording){
                    mRecordTime = 0;
                    camApp.setLongRecTime(mRecordTime);
                    camApp.sendVideoAction("excuteRecord", false);
                }
                camApp.setVideoThumb(false);
                finish();
                releaseCamera();
            }
        }
    }

    public boolean isSurveillanceMode(){
        int swicth_mode = HipSettingPreferences.getMode(MyCamera.this);
        CameraLog.e(TAG, "isSurveillanceMode swicth_mode=" + swicth_mode);
        if(swicth_mode == HipParamters.SURVEILLANCE_MODE){
            return true;
        }
        return false;
    }
    private void setRcLedStatus(int state) {
        if(ledService != null) {
            try {
                CameraLog.e(TAG, "setRcLedStatus will state=" + state);
                ledService.setRcLedStatus(state);
                CameraLog.e(TAG, "setRcLedStatus end state=" + state);
            } catch (Exception e) {
                CameraLog.e(TAG, "ledService.setRcLedStatus abnormal!");
            }
        } else {
            CameraLog.e(TAG, "setRcLedStatus ledService = " + ledService);
        }
    }

}
