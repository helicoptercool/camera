/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein is
 * or may be trade secrets of Crunchfish AB.
 *
 * This code is written for evaluation and educational purposes only.
 * The code is not an integral part of the licensed software libraries for Touchless A3D SDK
 * and is provided "AS IS" and "AS AVAILABLE" with all faults and without warranty of any kind.
 * Crunchfish is only providing limited documentation and no support related to this code.
 * The code might change at any time.
 */

package com.crunchfish.sample.hellotouchlessworld;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.crunchfish.helper.camera.CameraSurface;
import com.crunchfish.helper.integration.TouchlessA3DHelper;
import com.crunchfish.helper.integration.TouchlessA3DHelper.EngineReadyCallback;
import com.crunchfish.touchless_a3d.deprecated_gestures.ClosedHandPresence;
import com.crunchfish.touchless_a3d.deprecated_gestures.Gesture;
import com.crunchfish.touchless_a3d.deprecated_gestures.ObjectPresence;
import com.crunchfish.touchless_a3d.deprecated_gestures.OpenHandPresence;
import com.crunchfish.touchless_a3d.deprecated_gestures.ThumbsUpPresence;
import com.crunchfish.touchless_a3d.deprecated_gestures.VSignPresence;

/**
 * This is a very simple sample application which basically only displays an
 * open hand (if a hand is presented to the front-facing camera) or a closed
 * hand (if such is presented to the camera).
 * 
 * @adaptions Crunchfish AB This code is written for evaluation and educational
 *            purposes only. Crunchfish AB does not take any responsibilities to
 *            the functionality or usage of this code.
 *
 */
@SuppressWarnings("deprecation")
//EngineReadyCallback 注册手势
//Gesture.Listener 监听手势
public class TouchlessWorldActivity extends Activity implements
		Gesture.Listener, EngineReadyCallback {

	private final static String LOG_TAG = "HelloTouchlessWorld";

	/**
	 * This is the class which setups the engine and starts the detection of
	 * Hands and ClosedHands.
	 */
	private TouchlessA3DHelper mTouchlessA3DHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create and set the basic layout defined in
		// activity_hand_or_closed_hand.xml.
		setContentView(R.layout.activity_hello_touchless_world);

		// We set the background to white.
		//RelativeLayout rootView = (RelativeLayout) findViewById(R.id.rootview);
	//	rootView.setBackgroundColor(Color.WHITE);
		// Create the Engine through the Setup helper class BasicA3DSetup.
		mTouchlessA3DHelper = new TouchlessA3DHelper(this, 320, 240, true, this);
		// Retrieve the CameraSurface and set this to an already defined
		// layout.
		CameraSurface cameraSurface = mTouchlessA3DHelper.getCameraSurface();

		LinearLayout cameraPreviewLayout = (LinearLayout) findViewById(R.id.camera_preview_layout);
		cameraPreviewLayout.addView(cameraSurface);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTouchlessA3DHelper.unregisterGestureListener(OpenHandPresence.TYPE,
				this);
		mTouchlessA3DHelper.unregisterGestureListener(ClosedHandPresence.TYPE,
				this);
		mTouchlessA3DHelper.unregisterGestureListener(VSignPresence.TYPE, this);
		mTouchlessA3DHelper.unregisterGestureListener(ThumbsUpPresence.TYPE,
				this);
	}

	@Override
	public void onEngineReady() {
		Log.i(LOG_TAG, "A3DEngine is ready");

		// Register for open and closed hand gestures.
		mTouchlessA3DHelper
				.registerGestureListener(OpenHandPresence.TYPE, this);
		mTouchlessA3DHelper.registerGestureListener(ClosedHandPresence.TYPE,
				this);
		mTouchlessA3DHelper.registerGestureListener(VSignPresence.TYPE, this);
		mTouchlessA3DHelper
				.registerGestureListener(ThumbsUpPresence.TYPE, this);
	}

	@Override
	public void onGesture(Gesture gesture) {

		// Here in this callback function we will receive information about
		// detected open and closed hands and their corresponding state.
		ObjectPresence objectPresence = (ObjectPresence) gesture;

		// Let's update the view depending on the ObjectPresence
		// we got from the callback.
		String gestureType = "";
		switch (objectPresence.getType()) {
		case 0:
			gestureType = "OpenHandPresence";
			break;
		case 1:
			gestureType ="ClosedHandPresence";
			break;
		case 2:
			gestureType ="FacePresence";
			
			break;
		case 3:
			gestureType ="ThumbsUpPresence";
			
			break;
		case 4:
			gestureType ="VSignPresence";
			
			break;
		case 5:
			gestureType ="PinchSignPresence";
			
			break;
		case 6:
			gestureType ="Swipe";
			break;
			
		default:
			break;
		}
		System.out.println("==objectPresence.getType is:"
				+ gestureType + ";x:"
				+ objectPresence.getCenterX() + "y:"
				+ objectPresence.getCenterY());
	}
}
