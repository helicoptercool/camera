package com.android.camera.hip.dragonfly;

import com.android.camera.CameraLog;
import com.android.camera.Storage;
import com.android.camera.app.CameraApp;
import com.android.surveillance.SurveillanceService;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class CameraControlService extends Service {

    private static final String TAG = "HIP_CameraControlService";
    private CameraControlBinder mBinder;
    private CameraApp camApp;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mBinder = new CameraControlBinder(this);
        super.onCreate();
        camApp = (CameraApp) this.getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mBinder != null) {
            mBinder = null;
        }
        super.onDestroy();
    }

    private void openCamera() {
        CameraLog.e(TAG, "#openCamera  : command try to open camera  !  ");
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.CAMERA_LOGIN);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void closeCamera() {
        CameraLog.e(TAG, "#closeCamera  : command try to close camera  !  ");
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.CAMERA_LOGOUT);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void startPreview() {
        CameraLog.e(TAG, "#startPreview  : command try to start preview  !  ");
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.START_PREVIEW);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void stopPreview() {
        CameraLog.e(TAG, "#stopPreview  : command try to stop preview !  ");
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.STOP_PREVIEW);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private CameraSettings getSettings() {
        CameraSettings setting = HipSettingPreferences.getCameraSettings(this);
        return setting;
    }

    private CameraStatus getStatus() {
        CameraStatus status = HipSettingPreferences.getCameraStatus(this);
        status.recstaRec = camApp.isRecording();
        status.recstaTime = (int)camApp.getLongRecTime();
        return status;
    }

    private void setCammod(int mode) {
        CameraLog.e(TAG, "#setCammod  : command try to set mode  ! mode =  " + mode);
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.SWITCH_MODE);
        in.putExtra(HipParamters.MODE, mode);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void setSwtmod(int mode) {
        CameraLog.e(TAG, "#setSwtmod  is calling  !");
        setCammod(mode);
    }

    private void setCamflp(boolean isFlp) {
        CameraLog.e(TAG, "#setCamflp  is calling !  isFlp = " + isFlp);
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.SET_AUTO_FLIP);
        in.putExtra(HipParamters.AUTO_FLIP, isFlp);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void setParameter(String key, int value) {
        CameraLog.e(TAG, "#setParameter  is calling !  key = " + key + "  ,value = " + value);
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        if ("picres".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.PICTURE_SIZE, value);
        } else if ("picexp".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.EXPOSURE, value);
        } else if ("picwbl".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.WHITE_BALANCE, value);
        } else if ("picbur".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PICTURE_BURST);
            in.putExtra(HipParamters.PICTURE_BURST_RATE, value);
        } else if ("pictlp".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PICTURE_LAPSE);
            in.putExtra(HipParamters.PICTURE_LAPSE_TIME, value);
        } else if ("pictwb".equalsIgnoreCase(key)) {
            return;
        } else if ("piccef".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.COLOR_EFFECT, value);
        } else if ("vdores".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.VIDEO_SIZE, value);
        } else if ("vdowbl".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.WHITE_BALANCE, value);
        } else if ("vdotlp".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_VIDEO_LAPSE);
            in.putExtra(HipParamters.VIDEO_LAPSE_TIME, value);
        } else if ("vdocef".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.COLOR_EFFECT, value);
        } else if ("camgps".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PARAMETERS);
            in.putExtra(HipParamters.GPS, value);
        } else if ("camflp".equalsIgnoreCase(key)) {
            in.putExtra(HipParamters.COM_KEY, HipParamters.SET_AUTO_FLIP);
            in.putExtra(HipParamters.AUTO_FLIP, value);
        } else {
            return;
        }
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void setPictwb(int interval, int rate) {
        CameraLog.e(TAG, "#setPictwb  is calling !  " + "  , rate = " + rate
                + "  , interval = " + interval);
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.SET_PICTURE_LAPSE_BURST);
        in.putExtra(HipParamters.PICTURE_LAPSE_TIME, interval);
        in.putExtra(HipParamters.PICTURE_BURST_RATE, rate);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private boolean isSdcardFull() {
        long spaceSize = Storage.getSpaceStorageSzie();
        CameraLog.e(TAG, "isSdcardFull spaceSize : " + spaceSize);
        return spaceSize <= HipParamters.SPACE_MAX_SIZE;
    }

    private void shubut() {
        CameraLog.e(TAG, "#shubut  : command try to take pictrue !");
        boolean isRecord = camApp.isRecording();
        if (isRecord) {
            if (camApp.isLiveShot())
                return;
        }
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.TAKE_PICTURE);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void record(boolean start) {
        boolean isRecord = camApp.isRecording();
        CameraLog.e(TAG, "#record  : command try record video  !  start = " + start
                + " , isRecord = " + isRecord);
        if(isRecord == start){
            if (!isRecord) {
                camApp.sendVideoAction("excuteRecord", false);
            }
            return;
        }
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.RECORD);
        in.putExtra(HipParamters.RECORD, start);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void switchRecord() {
        Intent in = new Intent(HipParamters.REQUEST_ACTION);
        in.putExtra(HipParamters.COM_KEY, HipParamters.RECORD);
        boolean isRecord = camApp.isRecording();
        CameraLog.e(TAG, "#switchRecord  is calling ! " + "  , isRecord = " + isRecord);
        if (!isRecord) {
            if (!camApp.isEnableExcuteRCRecord()) {
                CameraLog
                        .d(TAG, "#switchRecord  is calling ! " + "  , don't record  = " + isRecord);
                return;
            }
        }
        in.putExtra(HipParamters.RECORD, !isRecord);
        in.setClass(getApplication(), HipCameraService.class);
        this.startService(in);
    }

    private void setSvlmod(int value) {
        CameraLog.e(TAG, "#setSvlmod  is calling ! " + "  , value = " + value);
        Intent in = new Intent(SurveillanceService.RESULT_ACTION);
        in.putExtra(HipParamters.COM_KEY, SurveillanceService.KEYS_SET_DETECT_MODE);
        in.putExtra(SurveillanceService.KEYS_DETECT_MODE, value);
        in.setClass(getApplication(), SurveillanceService.class);
        this.startService(in);
    }

    private int getParameter (String key) {
        CameraLog.e(TAG, "#getParameter  is calling ! " + "  , key = " + key);
        if ("picres".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.PICTURE_SIZE);
        } else if ("picexp".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.EXPOSURE);
        } else if ("picwbl".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.WHITE_BALANCE);
        } else if ("picbur".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4Setting(this, HipParamters.PIC_BURST_RATE);
        } else if ("pictlp".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4Setting(this, HipParamters.PIC_LAPSE_TIME);
        } else if ("piccef".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.COLOR_EFFECT);
        } else if ("vdores".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.VIDEO_SIZE);
        } else if ("vdowbl".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.WHITE_BALANCE);
        } else if ("vdotlp".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4Setting(this, HipParamters.VIDEO_LAPSE_TIME);
        } else if ("vdocef".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4SwitchMode(this, HipParamters.COLOR_EFFECT);
        } else if ("camgps".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4Setting(this, HipParamters.GPS);
        } else if ("camflp".equalsIgnoreCase(key)) {
            return HipSettingPreferences.get4Setting(this, HipParamters.AUTO_FLIP);
        } else {
            return 0;
        }
    }

    private void setAutdel(boolean value) {
        CameraLog.e(TAG, "#setAutdel  is calling ! " + "  , value = " + value);
        Intent retintent = new Intent(SurveillanceService.RESULT_ACTION);
        retintent.putExtra(HipParamters.COM_KEY,SurveillanceService.KEYS_SET_AUTO_DEL );
        retintent.getBooleanExtra(SurveillanceService.KEYS_AUTODEL, false);
        retintent.setClass(getApplication(), SurveillanceService.class);
        this.startService(retintent);
    }

    private void setAuddet(boolean trigger, int level, int mode, int reclen, int interval) {
        CameraLog.e(TAG, "#setAuddet  is calling ! " + "  , trigger = " + trigger + " , level = "
                + level + " , reclen = " + reclen + " , interval = " + interval);
        Intent retintent = new Intent(SurveillanceService.RESULT_ACTION);
        retintent.putExtra(HipParamters.COM_KEY,SurveillanceService.KEYS_SET_AUDIO_DETECT );
        Bundle action = new Bundle();
        action.putInt(SurveillanceService.KEYS_MODE, mode);
        action.putInt(SurveillanceService.KEYS_RECLEN, reclen);
        action.putInt(SurveillanceService.KEYS_INTERVAL, interval);
        Bundle auddet = new Bundle();
        auddet.putBoolean(SurveillanceService.KEYS_ENABLE, trigger);
        auddet.putInt(SurveillanceService.KEYS_LEVEL, level);
        auddet.putBundle(SurveillanceService.KEYS_ACTION, action);
        retintent.putExtra(SurveillanceService.KEYS_AUDDET,auddet);
        retintent.setClass(getApplication(), SurveillanceService.class);
        this.startService(retintent);
    }

    private void setMotdet(boolean trigger, int level, int mode, int reclen, int interval) {
        CameraLog.e(TAG, "#setMotdet  is calling ! " + "  , trigger = " + trigger + " , level = "
                + level + " , reclen = " + reclen + " , interval = " + interval);
        Intent retintent = new Intent(SurveillanceService.RESULT_ACTION);
        retintent.putExtra(HipParamters.COM_KEY,SurveillanceService.KEYS_SET_MOTION_DETECT );
        Bundle action = new Bundle();
        action.putInt(SurveillanceService.KEYS_MODE, mode);
        action.putInt(SurveillanceService.KEYS_RECLEN, reclen);
        action.putInt(SurveillanceService.KEYS_INTERVAL, interval);
        Bundle motdet = new Bundle();
        motdet.putBoolean(SurveillanceService.KEYS_ENABLE, trigger);
        motdet.putInt(SurveillanceService.KEYS_LEVEL, level);
        motdet.putBundle(SurveillanceService.KEYS_ACTION, action);
        retintent.putExtra(SurveillanceService.KEYS_MOTDET,motdet);
        retintent.setClass(getApplication(), SurveillanceService.class);
        this.startService(retintent);
    }

    public class CameraControlBinder extends ICameraControl.Stub {

        CameraControlService mService;

        public CameraControlBinder(CameraControlService service) {
            mService = service;
        }

        public void open() {
            if (mService == null)
                return;
            mService.openCamera();
        }

        public void close() {
            if (mService == null)
                return;
            mService.closeCamera();
        }

        public void startPreview() {
            if (mService == null)
                return;
            mService.startPreview();
        }

        public void stopPreview() {
            if (mService == null)
                return;
            mService.stopPreview();
        }

        public boolean isSdcardFull() {
            if (mService == null)
                return true;
            return mService.isSdcardFull();
        }

        public void setCammod(int value) {
            if (mService == null)
                return;
            mService.setCammod(value);
        }

        public void setSwtmod(int value) {
            if (mService == null)
                return;
            mService.setSwtmod(value);
        }

        public void setParameter(String key, int value) {
            if (mService == null)
                return;
            mService.setParameter(key, value);
        }

        public void setCamflp(boolean value) {
            if (mService == null)
                return;
            mService.setCamflp(value);
        }

        public void setPictwb(int interval, int rate) {
            if (mService == null)
                return;
            mService.setPictwb(interval, rate);
        }

        public void setSvlmod(int value) {
            if (mService == null)
                return;
            mService.setSvlmod(value);
        }

        public void setAutdel(boolean value) {
            if (mService == null)
                return;
            mService.setAutdel(value);
        }

        public void setAuddet(boolean trigger, int level, int mode, int reclen, int interval) {
            if (mService == null)
                return;
            mService.setAuddet(trigger, level, mode, reclen, interval);
        }

        public void setMotdet(boolean trigger, int level, int mode, int reclen, int interval) {
            if (mService == null)
                return;
            mService.setMotdet(trigger, level, mode, reclen, interval);
        }

        public void setShubut() {
            if (mService == null)
                return;
            mService.shubut();
        }

        public void setRecord(boolean start) {
            if (mService == null)
                return;
            mService.record(start);
        }

        public void switchRecord() {
            if (mService == null)
                return;
            mService.switchRecord();
        }

        public CameraSettings getSettings() {
            if (mService == null)
                return null;
            return mService.getSettings();
        }

        public CameraStatus getStatus() {
            if (mService == null)
                return null;
            return mService.getStatus();
        }

        public int getParameter(String key) {
            if (mService == null)
                return 0;
            return mService.getParameter(key);
        }
    }

}
