package com.hip.dragonfly.camera.tests;

import com.android.camera.CameraLog;
import com.android.camera.hip.dragonfly.HipParamters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author hip
 */
public class CameraTestBroadcast extends BroadcastReceiver {

    private String TAG = "CameraTestBroadcast";

    private static final String HIP_DRAGONFLY_CAMERA_ACTION_MEMPIC = "com.hip.dragonfly.camera.action.EVENT_MEMPIC";
    private static final String HIP_DRAGONFLY_CAMERA_ACTION_MEMVIO = "com.hip.dragonfly.camera.action.EVENT_MEMVID";
    private static final String RESULT_ACTION = "com.android.mycamera.command.result";

    private static final String KEY_MEMPIC = "mempic";
    private static final String KEY_MEMVIO = "memvio";
    private static final String KEY_STATUS = "result";

    public void onReceive(Context context, Intent intent) {
        CameraLog.e(TAG, "intent=" + intent.getAction());
        if (HIP_DRAGONFLY_CAMERA_ACTION_MEMPIC.equals(intent.getAction())) {
            //CameraLog.e(TAG, KEY_MEMPIC + "  =  " + intent.getIntExtra(KEY_MEMPIC, 0));
        }
        if (HIP_DRAGONFLY_CAMERA_ACTION_MEMVIO.equals(intent.getAction())) {
            //CameraLog.e(TAG, KEY_MEMVIO + "  =  " + intent.getIntExtra(KEY_MEMVIO, 0));
        }
        if (RESULT_ACTION.equals(intent.getAction())) {
            int switch_mode = -1;
            if(intent.hasExtra("switch_mode")){
                switch_mode = intent.getIntExtra("switch_mode",-1);
            }
            CameraLog.e(TAG, "switch_mode:" + " = " + switch_mode);
        }
	    if (HipParamters.VIDEO_STOP_RECORDING.equals(intent.getAction())) {
           if (intent.hasExtra(HipParamters.RESULT_KEY)) {
                CameraLog.e(TAG, HipParamters.VIDEO_STOP_RECORDING + " # " + HipParamters.RESULT_KEY + " = " + intent.getStringExtra(HipParamters.RESULT_KEY));
           }
        }
	    if (HipParamters.CAMERA_RESPONE_STATUS.equals(intent.getAction())) {
	        String mode = intent.getStringExtra("mode");
	        String action = intent.getStringExtra("action");
	        long laptime = intent.getLongExtra("time", 0);
	        CameraLog.d(TAG, "HipParamters mode:" + mode);
	        CameraLog.d(TAG, "HipParamters action:" + action);
	        CameraLog.d(TAG, "HipParamters laptime:" + laptime);
	    }
	    if("com.hip.dragonfly.camera.action.EVENT_MEMPIC".equals(intent.getAction())){
	        int mempic = intent.getIntExtra("mempic", -1);
	        CameraLog.d(TAG, "mempic:" + mempic);
	    }
	    if("com.hip.dragonfly.camera.action.EVENT_MEMVID".equals(intent.getAction())){
            int memvio = intent.getIntExtra("memvio", -1);
            CameraLog.d(TAG, "memvio:" + memvio);
        }
    }

}
