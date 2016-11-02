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

package com.crunchfish.helper.camera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.crunchfish.helper.exception.CameraFailureException;
import com.crunchfish.helper.utils.Utils;
import com.crunchfish.touchless_a3d.TouchlessA3D;

/**
 * This class is responsible for opening and initializing the Camera
 * and attach that to a SurfaceView.
 *
 * @note      This object needs to be attached to a View and visible within the UI
 *            in order to generate preview images.
 *
 * @adaptions Crunchfish AB This code is written for evaluation and educational
 *            purposes only. Crunchfish AB does not take any responsibilities to
 *            the functionality or usage of this code.
 */
public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = "CameraSurface";

    private Camera mCamera = null;
    private CameraInfo mCameraInfo;
    private final PreviewCallback mPreviewCallback;

    private final boolean mIsFrontFacing;
    private int mRotation = 0;

    private final int mWidth;
    private final int mHeight;

    private final boolean mWithBuffer;
    private byte[][] mBuffers;
    private int mCurrentBuffer;

    /**
     * PreviewCallback used when pre-allocated buffers are requested from the
     * the user of this class.
     *
     * @see Camera.setPreviewCallbackWithBuffer()
     */
    private final PreviewCallback mBufferedPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
        	Log.e("data legth", ""+data.length);
            if (mPreviewCallback != null) {
                mPreviewCallback.onPreviewFrame(data, camera);
            }

            camera.addCallbackBuffer(mBuffers[mCurrentBuffer]);
            mCurrentBuffer = 1 - mCurrentBuffer;
        }
    };

    /**
     * Construct a CameraSurface.
     *
     * @param context           The application context
     * @param width             The width of this surface
     * @param height            The height of this surface
     * @param isFrontFacing     True if using the front facing camera, false otherwise
     * @param withBuffer        True if pre-allocated Camera preview buffers should be enabled
     *                          (this is preferred, could enhance performance a bit), false otherwise.
     * @param previewCallback   The PreviewCallback in which to receive the Camera preview images.
     */
    public CameraSurface(Context context, int width, int height,
            boolean isFrontFacing, boolean withBuffer,
            PreviewCallback previewCallback) {
        super(context);
        mWidth = width;
        mHeight = height;
        mIsFrontFacing = isFrontFacing;
        mWithBuffer = withBuffer;
        mPreviewCallback = previewCallback;

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(LOG_TAG, "surfaceCreated()");

        try {
            openCamera();
        } catch (CameraFailureException expection) {
            Log.e(LOG_TAG, "Camera could not be created! - " + expection.getMessage());
            return;
        }

        try {
            prepareCamera(holder);
        } catch (CameraFailureException exception) {
            Log.e(LOG_TAG, "Could not prepare Camera! - " + exception.getMessage());
            mCamera.release();
            mCamera = null;
            return;
        }

        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(LOG_TAG, "surfaceDestroyed()");
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(LOG_TAG, "surfaceChanged() - " + width + "x" + height);

    }

    /**
     * Pause the Camera preview.
     */
    public void pause() {
        Log.i(LOG_TAG, "Camera paused preview");
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

  /*  *//**
     * Resume the Camera preview.
     *//*
    public void resume() {
        Log.i(LOG_TAG, "Camera resume preview");
        if (mCamera != null) {
            if (mWithBuffer) {
                mCurrentBuffer = 0;
                mCamera.addCallbackBuffer(mBuffers[0]);
                mCamera.addCallbackBuffer(mBuffers[1]);
                mCamera.setPreviewCallbackWithBuffer(mBufferedPreviewCallback);
            } else {
                mCamera.setPreviewCallback(mPreviewCallback);
            }
            mCamera.startPreview();
        }
    }*/

    /**
     * @return the width of the internal camera buffer
     */
    public int getPreviewWidth() {
        return mWidth;
    }

    /**
     * @return the height of the internal camera buffer
     */

    public int getPreviewHeight() {
        return mHeight;
    }

    /**
     * @return a TouchlessA3D.Rotate constant translated from the 
     *         mRotation property (which is in degrees)
     */
    public TouchlessA3D.Rotate getCameraRotation() {

        TouchlessA3D.Rotate rotate;

        if (mRotation == 0) {
            rotate = TouchlessA3D.Rotate.DO_NOT_ROTATE;
        } else if (mRotation == 90) {
            rotate = TouchlessA3D.Rotate.ROTATE_90;
        } else if (mRotation == 180) {
            rotate = TouchlessA3D.Rotate.ROTATE_180;
        } else {
            rotate = TouchlessA3D.Rotate.ROTATE_270;
        }

        return rotate;
    }


    /**
     * @return The android.hardware.Camera object associated with the surface
     */
    public Camera getCamera() {
        return mCamera;
    }

    public int getCameraDisplayOrientation() {
        if (mCameraInfo != null) {
            return mCameraInfo.orientation;
        }
        return 0;
    }

    private void openCamera() throws CameraFailureException  {
        int cameraId = 0;
        int secondaryCameraId = 0;
        if (Utils.isDeviceOldNexus7(getContext())) {
            cameraId = 0;
        } else {
            cameraId = CameraInfo.CAMERA_FACING_FRONT;
            secondaryCameraId = CameraInfo.CAMERA_FACING_BACK;
            try {
                mCamera = Camera.open(cameraId);
            } catch (RuntimeException rte) {
                Log.w(LOG_TAG,"Warning! We could not open camera with id "+cameraId+", will try "+secondaryCameraId);
                cameraId = secondaryCameraId;
                try {
                    mCamera = Camera.open(cameraId);
                }
                catch (RuntimeException rte2){
                    Log.e(LOG_TAG,"Could not open any cameras");
                    throw new CameraFailureException(rte2);
                }
            }
        }

        mCameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, mCameraInfo);
    }

    private void prepareCamera(SurfaceHolder holder) throws CameraFailureException {
        Camera.Size requestedPreviewSize = mCamera.new Size(mWidth, mHeight);
        if (!isPreviewSizeSupported(requestedPreviewSize)) {
            Log.e(LOG_TAG, "Could not prepare Camera! - unsupported preview size set!");
            return;
        }

        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(mWidth, mHeight);
        params.setExposureCompensation(0);

        try {
            mCamera.setParameters(params);
        } catch (RuntimeException rte) {
            throw new CameraFailureException(rte);
        }

        setCameraDisplayOrientation();

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            throw new CameraFailureException(exception);
        }

        if (mWithBuffer) {
            int bitsPerPixel = ImageFormat.getBitsPerPixel(mCamera.getParameters()
                    .getPreviewFormat());
            // Convert the bits to bytes and round it up to nearest integer.
            int bytesPerPixel = (int) Math.ceil(bitsPerPixel / 8.f);
            mBuffers = new byte[2][mWidth * mHeight * bytesPerPixel];
            mCurrentBuffer = 0;
            mCamera.addCallbackBuffer(mBuffers[0]);
            mCamera.addCallbackBuffer(mBuffers[1]);
            mCamera.setPreviewCallbackWithBuffer(mBufferedPreviewCallback);
        } else {
            mCamera.setPreviewCallback(mPreviewCallback);
        }
    }

    /**
     *  Updates the internal rotation state of the camera surface 
     *  to match that of the display.
     */
    public void setCameraDisplayOrientation() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (mCameraInfo.orientation - degrees + 360) % 360;
        }

        mRotation = result;

        if(mCamera != null) {
            mCamera.setDisplayOrientation(result);
        }
    }

    private boolean isPreviewSizeSupported(Camera.Size requestedPreviewSize) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> supportedPreviewSizes = params.getSupportedPreviewSizes();

        if (!supportedPreviewSizes.contains(requestedPreviewSize)) {
            Log.w(LOG_TAG, "Supported preview sizes are: ");
            for (int i = 0; i < supportedPreviewSizes.size(); i++) {
                Log.w(LOG_TAG, "(" + supportedPreviewSizes.get(i).width + ", "
                        + supportedPreviewSizes.get(i).height + ")");
            }

            return false;
        }

        return true;
    }
}
