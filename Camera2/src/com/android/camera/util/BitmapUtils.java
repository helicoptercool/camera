/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.camera.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    private static final int DEFAULT_JPEG_QUALITY = 90;
    public static final int UNCONSTRAINED = -1;

    private BitmapUtils(){}

    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(int width, int height,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(
                width, height, minSideLength, maxNumOfPixels);

        return initialSize <= 8
                ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private static int computeInitialSampleSize(int w, int h,
            int minSideLength, int maxNumOfPixels) {
        if (maxNumOfPixels == UNCONSTRAINED
                && minSideLength == UNCONSTRAINED) return 1;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) FloatMath.ceil(FloatMath.sqrt((float) (w * h) / maxNumOfPixels));

        if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            int sampleSize = Math.min(w / minSideLength, h / minSideLength);
            return Math.max(sampleSize, lowerBound);
        }
    }

    // This computes a sample size which makes the longer side at least
    // minSideLength long. If that's not possible, return 1.
    public static int computeSampleSizeLarger(int w, int h,
            int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the min x that 1 / x >= scale
    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) FloatMath.floor(1f / scale);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the max x that 1 / x <= scale.
    public static int computeSampleSize(float scale) {
        Utils.assertTrue(scale > 0);
        int initialSize = Math.max(1, (int) FloatMath.ceil(1 / scale));
        return initialSize <= 8
                ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        Log.d(TAG, "getConfig config = " + config);
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeDownBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeDownByWidthAndHeight(
            Bitmap bitmap, int newWidth, int newHeight) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / srcWidth;
        float scaleHeight = ((float) newHeight) / srcHeight;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, srcWidth, srcHeight, matrix, true);
        return newBitmap;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w,  h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    public static void recycleSilently(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            bitmap.recycle();
        } catch (Throwable t) {
            Log.w(TAG, "unable recycle bitmap", t);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, int rotation, boolean recycle) {
        if (rotation == 0) return source;
        int w = source.getWidth();
        int h = source.getHeight();
        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
        if (recycle) source.recycle();
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath) {
        // MediaMetadataRetriever is available on API Level 8
        // but is hidden until API Level 10
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();

            Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, filePath);

            // The method name changes between API Level 9 and 10.
            if (Build.VERSION.SDK_INT <= 9) {
                return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) return bitmap;
                }
                return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
            }
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } catch (InstantiationException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static byte[] compressToBytes(Bitmap bitmap) {
        return compressToBytes(bitmap, DEFAULT_JPEG_QUALITY);
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    public static boolean isSupportedByRegionDecoder(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.startsWith("image/") &&
                (!mimeType.equals("image/gif") && !mimeType.endsWith("bmp"));
    }

    public static boolean isRotationSupported(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.equals("image/jpeg");
    }

    static public void saveToStorage(Bitmap bitmap, String path) {
    	int w = bitmap.getWidth(), h = bitmap.getHeight();
    	int[] pixels = new int[w * h];
    	bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
    	saveToStorage(pixels, w, h, path);
    }

    static public void saveToStorage(int[] buffer, int width, int heigth, String path) {
    	byte[] rgb = addBMP_RGB_888(buffer, width, heigth);
        byte[] header = addBMPImageHeader(rgb.length);
        byte[] infos = addBMPImageInfosHeader(width	, heigth);

        byte[] byteBuffer = new byte[54 + rgb.length];
        System.arraycopy(header, 0, byteBuffer, 0, header.length);
        System.arraycopy(infos, 0, byteBuffer, 14, infos.length);
        System.arraycopy(rgb, 0, byteBuffer, 54, rgb.length);
        try {
	        FileOutputStream fos = new FileOutputStream(path);
	        fos.write(byteBuffer);
        } catch (FileNotFoundException e) {
	        e.printStackTrace();
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
    }

    public static Bitmap unwrap(Bitmap bitmap) {
        Log.d(TAG, "unwrap =====>>>>>");
    	int w = bitmap.getWidth(), h = bitmap.getHeight();
    	int[] pixels = new int[w * h];
    	bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

    	int[] upperBuffer = new int[(w * h + 1) / 2];
    	int[] lowerBuffer = new int[(w * h + 1) / 2];
        System.arraycopy(pixels, 0, upperBuffer, 0, upperBuffer.length);
        System.arraycopy(pixels, upperBuffer.length, lowerBuffer, 0, lowerBuffer.length);

        int offset = 0;
        for (int i = 0; i <= (w * h + 1) / 2 - 1; i += w) {
        	for(int j = 0; j <= w - 1; j++) {
        		pixels[j + offset] = lowerBuffer[i + j];
        		pixels[j + w + offset] = upperBuffer[i + j];
        	}
        	offset = i * 2;
        }

        //for debug
//        saveToStorage(upperBuffer, w, h / 2, "/storage/sdcard1/test_up.bmp");
//        saveToStorage(lowerBuffer, w, h / 2, "/storage/sdcard1/test_lower.bmp");
//        saveToStorage(pixels, w * 2, h / 2, "/storage/sdcard1/test.bmp");

        Bitmap target = Bitmap.createBitmap(pixels, w * 2, h / 2, getConfig(bitmap));

        Log.d(TAG, "unwrap <<<<<=====");
        return target;
    }

	/**
	 * add BMP image header
	 * */
    static private byte[] addBMPImageHeader(int size) {
	    byte[] buffer = new byte[14];
	    buffer[0] = 0x42;
	    buffer[1] = 0x4D;
	    buffer[2] = (byte) (size >> 0);
	    buffer[3] = (byte) (size >> 8);
	    buffer[4] = (byte) (size >> 16);
	    buffer[5] = (byte) (size >> 24);
	    buffer[6] = 0x00;
	    buffer[7] = 0x00;
	    buffer[8] = 0x00;
	    buffer[9] = 0x00;
	    buffer[10] = 0x36;
	    buffer[11] = 0x00;
	    buffer[12] = 0x00;
	    buffer[13] = 0x00;
	    return buffer;
    }

	/**
	 * add BMP image header
	 * */
    static private byte[] addBMPImageInfosHeader(int w, int h) {
	    byte[] buffer = new byte[40];
	    buffer[0] = 0x28;
	    buffer[1] = 0x00;
	    buffer[2] = 0x00;
	    buffer[3] = 0x00;
	    buffer[4] = (byte) (w >> 0);
	    buffer[5] = (byte) (w >> 8);
	    buffer[6] = (byte) (w >> 16);
	    buffer[7] = (byte) (w >> 24);
	    buffer[8] = (byte) (h >> 0);
	    buffer[9] = (byte) (h >> 8);
	    buffer[10] = (byte) (h >> 16);
	    buffer[11] = (byte) (h >> 24);
	    buffer[12] = 0x01;
	    buffer[13] = 0x00;
	    buffer[14] = 0x18;
	    buffer[15] = 0x00;
	    buffer[16] = 0x00;
	    buffer[17] = 0x00;
	    buffer[18] = 0x00;
	    buffer[19] = 0x00;
	    buffer[20] = 0x00;
	    buffer[21] = 0x00;
	    buffer[22] = 0x00;
	    buffer[23] = 0x00;
	    buffer[24] = (byte) 0xE0;
	    buffer[25] = 0x01;
	    buffer[26] = 0x00;
	    buffer[27] = 0x00;
	    buffer[28] = 0x02;
	    buffer[29] = 0x03;
	    buffer[30] = 0x00;
	    buffer[31] = 0x00;
	    buffer[32] = 0x00;
	    buffer[33] = 0x00;
	    buffer[34] = 0x00;
	    buffer[35] = 0x00;
	    buffer[36] = 0x00;
	    buffer[37] = 0x00;
	    buffer[38] = 0x00;
	    buffer[39] = 0x00;
	    return buffer;
    }

    /**
     * convert pixel data to RGB
     * */
    static private byte[] addBMP_RGB_888(int[] pixels, int w, int h) {
	    int len = pixels.length;
	    byte[] buffer = new byte[w * h * 3];
	    int offset = 0;
	    for (int i = len - 1; i >= w; i -= w) {
		    int end = i,start = i - w + 1;
		    for(int j = start; j <= end; j++) {
			    buffer[offset] = (byte)(pixels[j] >> 0);
			    buffer[offset + 1] = (byte)(pixels[j] >> 8);
			    buffer[offset + 2] = (byte)(pixels[j] >> 16);
			    offset += 3;
		    }
	    }
	    return buffer;
    }
}
