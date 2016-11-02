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

package com.crunchfish.helper.integration;

import android.R.color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;

import com.crunchfish.helper.camera.CameraSurface;

import com.crunchfish.sample.hellotouchlessworld.R;
import com.crunchfish.touchless_a3d.TouchlessA3D;
import com.crunchfish.touchless_a3d.TouchlessA3D.Rotate;
import com.crunchfish.touchless_a3d.exception.LicenseNotValidException;
import com.crunchfish.touchless_a3d.exception.LicenseServerUnavailableException;
import com.crunchfish.touchless_a3d.gesture.Gesture;

/**
 * This is an example on a simple setup towards the TouchlessA3D Engine for
 * Android. It sets up the TouchlessA3D Engine. It uses the CameraSurface object
 * to set up and run the Camera and sending the preview images to the engine.
 *
 * @adaptions Crunchfish AB This code is written for evaluation and educational
 *            purposes only. Crunchfish AB does not take any responsibilities to
 *            the functionality or usage of this code.
 * @see CameraSurface
 */
public class TouchlessA3DHelper  implements SurfaceHolder.Callback{

    private static final String LOG_TAG = "A3DSetup";

    private final Context mContext;

    private TouchlessA3D mTouchlessA3D;
    private final CameraSurface mCameraSurface;
    private StartA3DEngineTask mStartEngineTask;

    //private final ImageRotationHandler mImageRotationHandler;
    private final EngineReadyCallback mEngineReadyCallback;

    private final int mImageWidth;
    private final int mImageHeight;

    /**
     * The PreviewCallback in which the preview images are received from the
     * CameraSurface. They are feed into the engine.
     */
    private final PreviewCallback mPreviewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mTouchlessA3D != null) {
                mTouchlessA3D.handleImage(System.currentTimeMillis(), data,
                		 Rotate.ROTATE_270
                        );
            }
        }
    };

    /**
     * Interface the defines an EngineReadyCallback function that is called when
     * the Engine is initialized and ready for use.
     */
    public interface EngineReadyCallback {

        /**
         * Called when the Engine is initialized and ready for use. This may
         * happen more than once as the engine is stopped when the application
         * is paused. All listeners will need to be re-registred.
         */
        void onEngineReady();
    }

    /**
     * Construct a BasicA3DSetup.
     *
     * @param context The context of the application
     * @param imageWidth The width of the images the A3DEngine will work on
     * @param imageHeight The height of the images the A3DEngine will work on
     * @param useFrontFacingCamera True if the front-facing Camera should be
     *            used, false if the back-facing.
     */
    public TouchlessA3DHelper(Context context, int imageWidth, int imageHeight,
            boolean useFrontFacingCamera, EngineReadyCallback engineReadyCallback) {
        mContext = context;
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mEngineReadyCallback = engineReadyCallback;

        mCameraSurface = new CameraSurface(context, imageWidth, imageHeight,
                useFrontFacingCamera, true, mPreviewCallback);

        mCameraSurface.getHolder().addCallback(this);
    }

    /**
     * Get the CameraSurface instance.
     *
     * @note This should be attached to a View and should be visible.
     */
    public final CameraSurface getCameraSurface() {
        return mCameraSurface;
    }

    /**
     * @see TouchlessA3D#registerGesture(Gesture)
     */
    public void registerGesture(final Gesture gesture) {
        if (mTouchlessA3D != null) {
            mTouchlessA3D.registerGesture(gesture);
        }
    }

    /**
     * @see TouchlessA3D#unregisterGesture(Gesture)
     */
    public void unregisterGesture(final Gesture gesture) {
        if (mTouchlessA3D != null) {
            mTouchlessA3D.unregisterGesture(gesture);
        }
    }

    /**
     * @deprecated Use {@link #registerGesture(Gesture)} instead.
     * @see TouchlessA3D::registerGestureListener()
     */
    public void registerGestureListener(int gestureType,
            com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener gestureListener) {
        if (mTouchlessA3D != null) {
            Log.i(LOG_TAG, "RegisterListener type: " + gestureType);
            mTouchlessA3D.registerGestureListener(gestureType, gestureListener);
        }
    }

    /**
     * @deprecated Use {@link #unregisterGesture(Gesture)} instead.
     * @see TouchlessA3D::unregisterGestureListener()
     */
    public void unregisterGestureListener(int gestureType,
            com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener gestureListener) {
        if (mTouchlessA3D != null) {
            Log.i(LOG_TAG, "UnregisterListener type: " + gestureType);
            mTouchlessA3D.unregisterGestureListener(gestureType, gestureListener);
        }
    }

    /**
     * @see TouchlessA3D::setParameter()
     */
    public void setExtendedRange(boolean enabled) {
        if (mTouchlessA3D != null) {
            mTouchlessA3D.setParameter(TouchlessA3D.Parameters.EXTENDED_RANGE, enabled ? 1 : 0);
        }
    }

    /**
     * @see TouchlessA3D::getParameter()
     */
    public boolean isExtendedRange() {
        if (mTouchlessA3D != null) {
            return mTouchlessA3D.getParameter(TouchlessA3D.Parameters.EXTENDED_RANGE) != 0;
        }
        return false;
    }

    /**
     * AsyncTask that will create the Engine in a separate thread since the
     * setup could take some time. Mostly due to the license check while
     * enabled.
     */
    private class StartA3DEngineTask extends AsyncTask<Void, Void, TouchlessA3D> {
        private int errorResId;

        @Override
        protected TouchlessA3D doInBackground(Void... v) {
            TouchlessA3D engine = null;
            try {
                engine = new TouchlessA3D(mImageWidth, mImageHeight, mContext);
            } catch (LicenseNotValidException lnve) {
                Log.e(LOG_TAG, "License is not valid.");
                errorResId = R.string.license_denied_dialog_message;
            } catch (LicenseServerUnavailableException lsue) {
                Log.e(LOG_TAG, "License server not available.");
                errorResId = R.string.license_connection_dialog_message;
            }
            return engine;
        }

        @Override
        protected void onCancelled(final TouchlessA3D engine) {
            if (null != engine) {
                engine.close();
            }
        }

        @Override
        protected void onPostExecute(final TouchlessA3D engine) {
            if (engine != null) {
                engineCreated(engine);
            } else {
                showLicenceErrorDialog(errorResId);
            }
        }
    }

    private void engineCreated(TouchlessA3D engine) {
        mTouchlessA3D = engine;
        if (mEngineReadyCallback != null) {
            mEngineReadyCallback.onEngineReady();
        }
    }

    private void showLicenceErrorDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Start and initialized the engine in a separate thread.
        mStartEngineTask = new StartA3DEngineTask();
        mStartEngineTask.execute();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mStartEngineTask.cancel(true);
        if (null != mTouchlessA3D) {
            mTouchlessA3D.close();
            mTouchlessA3D = null;
        }
    }
}
