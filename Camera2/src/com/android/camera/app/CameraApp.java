/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.app;

import android.app.Application;
import android.content.Intent;

import com.android.camera.CameraLog;
import com.android.camera.hip.dragonfly.HipParamters;
import com.android.camera.hip.dragonfly.HipSettingPreferences;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.UsageStatistics;

public class CameraApp extends Application {

    static private boolean isRecording = false;
    private boolean isCapture = false;
    private boolean isVideoThumb = false;
    private boolean isLiveShot = false;
    public boolean isLiveShot() {
        CameraLog.e(TAG, "isLiveShot isLiveShot = " + isLiveShot);
        return isLiveShot;
    }

    public void setLiveShot(boolean isLiveShot) {
        CameraLog.e(TAG, "setLiveShot isLiveShot = " + isLiveShot);
        this.isLiveShot = isLiveShot;
    }

    private int mActivityStatus = HipParamters.CAMERA_ACTIVITY_STATUS_DESTROY;
    public boolean isVideoThumb() {
        CameraLog.e(TAG, "isVideoThumb isVideoThumb = " + isVideoThumb);
        return isVideoThumb;
    }

    public void setVideoThumb(boolean isVideoThumb) {
        CameraLog.e(TAG, "setVideoThumb isVideoThumb = " + isVideoThumb);
        this.isVideoThumb = isVideoThumb;
    }

    private long longRecTime = 0;

    private static final String TAG = "HipCameraApp";

    @Override
    public void onCreate() {
        super.onCreate();
        UsageStatistics.initialize(this);
        CameraUtil.initialize(this);
    }

    public boolean isRecording() {
        CameraLog.e(TAG, "isRecording isLiveShot = " + isLiveShot + "  , isRecording ="
                + isRecording);
        return isRecording;
    }

    public void setRecording(boolean isRecording) {
        CameraLog.e(TAG, "setRecording isRecording = " + isRecording);
        if(!isRecording){
            isLiveShot = false;
        }
        this.isRecording = isRecording;
    }

    public boolean isCapture() {
        CameraLog.d(TAG, "isCapture isCapture = " + isCapture);
        return isCapture;
    }

    public void setCapture(boolean isCapture) {
        CameraLog.d(TAG, "setCapture isCapture = " + isCapture);
        this.isCapture = isCapture;
    }

    public long getLongRecTime() {
        CameraLog.d(TAG, "setCapture longRecTime = " + longRecTime);
        return longRecTime;
    }

    public void setLongRecTime(long longRecTime) {
        CameraLog.d(TAG, "setLongRecTime longRecTime = " + longRecTime);
        this.longRecTime = longRecTime;
    }

    public void sendVideoAction( String action, boolean isRec) {
        Intent intent = new Intent(HipParamters.HIP_DRAGONFLY_CAMERA_ACTION_RECSTA);
        CameraLog.d(TAG, "send CE record time : " + (int) longRecTime + " , isRec = " + isRec);
        if (isRec) {
            if (longRecTime <= 0) {
                return;
            }
            intent.putExtra("time", (int) longRecTime);
        } else {
            isLiveShot = false;
        }
        isRecording = isRec;
        intent.putExtra("rec", isRec);
        sendBroadcast(intent);
    }

    public void setActivityStatus(int status) {
        CameraLog.d(TAG, "setActivityStatus status = " + status);
        this.mActivityStatus = status;
    }

    public boolean isEnableExcuteRCRecord() {
        CameraLog.d(TAG, "isEnableExcuteRCRecord  :  isCapture = " + isCapture
                + " , isRecording = " + isRecording + "  , mActivityStatus = " + mActivityStatus);
        if (isCapture) {
            return false;
        }
        if (mActivityStatus == HipParamters.CAMERA_ACTIVITY_STATUS_PAUSE) {
            return false;
        }
        if (mActivityStatus == HipParamters.CAMERA_ACTIVITY_STATUS_ACTIVE) {
            boolean isAutoPreivew = HipSettingPreferences.getAutoPreview(this);
            return isAutoPreivew ? false : true;
        }
        return true;
    }
}

