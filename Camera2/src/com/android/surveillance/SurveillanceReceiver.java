package com.android.surveillance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SurveillanceReceiver extends BroadcastReceiver{
	private static final String TAG = "SurveillanceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(TAG, "onReceive:"+intent);
    	intent.setClass(context, SurveillanceService.class);
        context.startService(intent);
    }
}
