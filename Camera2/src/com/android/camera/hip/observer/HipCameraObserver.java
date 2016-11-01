/**
 * 
 */
package com.android.camera.hip.observer;

/**
 * @author thundersoft
 *
 */
public interface HipCameraObserver {
    public static final int EVENT_ONREADY = 1; // Camera 准备状态
    public static final int EVENT_TAKE_PICTURE_START = 2; // 触发快门按键
    public static final int EVENT_VIDEO_RECORD_START = 4; // 开始录像
    public static final int EVENT_VIDEO_RECORD_STARTING = 3; // 正在录像
    public static final int EVENT_VIDEO_RECORD_STOP = 5; // 停止录像
    public static final int EVENT_VIDEO_RECORD_INIT = 6; // 初始化
    public static final int EVENT_VIDEO_RECORD_FAIL= 7; // 初始化
    public static final int EVENT_TAKE_PICTURE_START_ON_VIDEO_RECORD = 8;

    void notifyCameraStatus(int status);
}
