package com.android.camera.hip.dragonfly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.android.camera.CameraLog;

/**
 * @author duzc
 */
public class HipCameraBroadcast extends BroadcastReceiver {

    private static String TAG = "HipCameraBroadcast";

    private static String CAMERA_PACKAGE_NAME = "com.android.camera2";
    //private static String CAMERA_HIP_ACTIVITY = "com.android.camera.hip.dragonfly.HipCameraActivity";
    private static String CAMERA_HIP_ACTIVITY = "com.android.mycamera.MyCamera";

    public void onReceive(Context context, Intent intent) {
        CameraLog.d(TAG, "action = " + intent.getAction());
        if (HipParamters.CAMERA_START_ACTION.equals(intent.getAction())) {
            Intent in = new Intent();
            in.setClassName(CAMERA_PACKAGE_NAME, CAMERA_HIP_ACTIVITY);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(in);
            CameraLog.d(TAG, "HipCameraBroadcast Camera Activity Start");
        } else {
            Intent in = intent;
            in.setClass(context, HipCameraService.class);
            context.startService(in);
            CameraLog.d(TAG, "HipCameraBroadcast Camera HipCameraService Start");
        }
    }

}
