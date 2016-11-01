package com.android.camera.hip.dragonfly;

import android.os.Bundle;
import com.android.camera.hip.dragonfly.CameraSettings;
import com.android.camera.hip.dragonfly.CameraStatus;

interface ICameraControl {
    // local
    void open();
    void close();
    void startPreview();
    void stopPreview();
    boolean isSdcardFull();
    // get
    CameraSettings getSettings();
    CameraStatus getStatus();
    int getParameter(String key);
    // set
    void setCammod(int value);
    void setSwtmod(int value);
    void setCamflp(boolean value);
    void setParameter(String key, int value);
    void setPictwb(int interval, int rate);
    void setSvlmod(int value);
    void setAutdel(boolean value);
    void setAuddet(boolean trigger, int level, int mode, int reclen, int interval);
    void setMotdet(boolean trigger, int level, int mode, int reclen, int interval);
    void setShubut();
    void setRecord(boolean start);
    void switchRecord();
}