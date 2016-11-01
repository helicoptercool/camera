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

package com.android.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.android.camera.data.LocalData;
import com.android.camera.exif.ExifInterface;
import com.android.camera.hip.dragonfly.HipParamters;
import com.android.camera.util.ApiHelper;

public class Storage {
    private static final String TAG = "CameraStorage";
    public static final String DCIM = "DCIM";
    public static final String THUMB = "Thumb";
    public static final String DIRECTORY = DCIM + "/Camera";
    public static final String RAW_DIRECTORY = DCIM + "/Camera/raw";
    public static final String JPEG_POSTFIX = ".jpg";

    public static final String EXTERALE_PROPERTY_TYPE = "vold.external_sd_fs_type";
    public static final String EXTERALE_TYPE_FAT32 = "vfat";
    public static final String EXTERALE_TYPE_EXFAT = "exfat";

    // Match the code in MediaProvider.computeBucketValues().
    public static final String BUCKET_ID =
            String.valueOf(DIRECTORY.toLowerCase().hashCode());

    public static final long UNAVAILABLE = -1L;
    public static final long PREPARING = -2L;
    public static final long UNKNOWN_SIZE = -3L;
    public static final long LOW_STORAGE_THRESHOLD_BYTES = 50000000;

    private static boolean sSaveSDCard = false;

    public static final String EXTERALE = "/storage/sdcard1";
    public static final String INTERALE = "/storage/sdcard0";

    public static final String EXTERALE_DCIM = EXTERALE + "/" + DCIM;
    public static final String INTERALE_DCIM = INTERALE + "/" + DCIM;

    public static final String EXTERALE_THUMB = EXTERALE + "/" + THUMB;
    public static final String INTERALE_THUMB = INTERALE + "/" + THUMB;

    public static final String PHOTO_TLP = "Photo_Timelapse";

    public static final String EXTERALE_DCIM_TLP = EXTERALE_DCIM + "/" + PHOTO_TLP;
    public static final String INTERALE_DCIM_TLP = INTERALE_DCIM + "/" + PHOTO_TLP;

    public static final String EXTERALE_THUMB_TLP = EXTERALE_THUMB + "/" + PHOTO_TLP;
    public static final String INTERALE_THUMB_TLP = INTERALE_THUMB + "/" + PHOTO_TLP;

    public static final String PHOTO_BUR = "Photo_Burst";

    public static final String EXTERALE_DCIM_BUR = EXTERALE_DCIM + "/" + PHOTO_BUR;
    public static final String INTERALE_DCIM_BUR = INTERALE_DCIM + "/" + PHOTO_BUR;

    public static final String EXTERALE_THUMB_BUR = EXTERALE_THUMB + "/" + PHOTO_BUR;
    public static final String INTERALE_THUMB_BUR = INTERALE_THUMB + "/" + PHOTO_BUR;

    public static final String PHOTO_TWB = "Photo_IntBurst";

    public static final String EXTERALE_DCIM_TWB = EXTERALE_DCIM + "/" + PHOTO_TWB;
    public static final String INTERALE_DCIM_TWB = INTERALE_DCIM + "/" + PHOTO_TWB;

    public static final String EXTERALE_THUMB_TWB = EXTERALE_THUMB + "/" + PHOTO_TWB;
    public static final String INTERALE_THUMB_TWB = INTERALE_THUMB + "/" + PHOTO_TWB;

    public static final String VIDEO_TLP = "Video_Timelapse";

    public static final String EXTERALE_DCIM_VIDEO_TLP = EXTERALE_DCIM + "/" + VIDEO_TLP;
    public static final String INTERALE_DCIM_VIDEO_TLP = INTERALE_DCIM + "/" + VIDEO_TLP;

    public static final String EXTERALE_THUMB_VIDEO_TLP = EXTERALE_THUMB + "/" + VIDEO_TLP;
    public static final String INTERALE_THUMB_VIDEO_TLP = INTERALE_THUMB + "/" + VIDEO_TLP;

    public static boolean isSaveSDCard() {
        return sSaveSDCard;
    }

    public static void setSaveSDCard(boolean saveSDCard) {
        sSaveSDCard = saveSDCard;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setImageSize(ContentValues values, int width, int height) {
        // The two fields are available since ICS but got published in JB
        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
            values.put(MediaColumns.WIDTH, width);
            values.put(MediaColumns.HEIGHT, height);
        }
    }

    public static void writeFile(String path, byte[] jpeg, ExifInterface exif,
            String mimeType) {
        if (exif != null && (mimeType == null ||
            mimeType.equalsIgnoreCase("jpeg"))) {
            try {
                exif.writeExif(jpeg, path);
            } catch (Exception e) {
                Log.e(TAG, "Failed to write data", e);
            }
        } else if (jpeg != null) {
            if (!(mimeType.equalsIgnoreCase("jpeg") || mimeType == null)) {
                 File dir = new File(RAW_DIRECTORY);
                 dir.mkdirs();
            }
            writeFile(path, jpeg);
        }
    }

    public static void writeFile(String path, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close file after write", e);
            }
        }
    }

    public static void writeFile(byte[] data, String path) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        float sw = (float) 960 / bitmap.getWidth();
        float sh = (float) 160 / bitmap.getHeight();
        matrix.postScale(sw, sh);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
        CameraLog.d("myCamera", "Thumb bitmap:" + bitmap.getWidth() + ":" + bitmap.getHeight());
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeThumbFile(String path, byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        float sw = (float) 960/bitmap.getWidth();
        float sh = (float) 160/bitmap.getHeight();
        matrix.postScale(sw, sh);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        CameraLog.d("myCamera",  "Thumb bitmap:" + bitmap.getWidth() + ":" +bitmap.getHeight());
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save the image with a given mimeType and add it the MediaStore.
    public static Uri addImage(ContentResolver resolver, String title, long date,
            Location location, int orientation, ExifInterface exif, byte[] jpeg, int width,
            int height, String mimeType) {

        String path = generateFilepath(title, mimeType);
        writeFile(path, jpeg, exif, mimeType);
        return addImage(resolver, title, date, location, orientation,
                jpeg.length, path, width, height, mimeType);
    }

    // Get a ContentValues object for the given photo data
    public static ContentValues getContentValuesForData(String title,
            long date, Location location, int orientation, int jpegLength,
            String path, int width, int height, String mimeType) {
        // Insert into MediaStore.
        ContentValues values = new ContentValues(9);
        values.put(ImageColumns.TITLE, title);
        if (mimeType.equalsIgnoreCase("jpeg") || mimeType == null) {
            values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
        } else {
            values.put(ImageColumns.DISPLAY_NAME, title + ".raw");
        }
        values.put(ImageColumns.DATE_TAKEN, date);
        values.put(ImageColumns.MIME_TYPE, "image/jpeg");
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(ImageColumns.ORIENTATION, orientation);
        values.put(ImageColumns.DATA, path);
        values.put(ImageColumns.SIZE, jpegLength);

        setImageSize(values, width, height);

        if (location != null) {
            values.put(ImageColumns.LATITUDE, location.getLatitude());
            values.put(ImageColumns.LONGITUDE, location.getLongitude());
        }
        return values;
    }

    // Add the image to media store.
    public static Uri addImage(ContentResolver resolver, String title,
            long date, Location location, int orientation, int jpegLength,
            String path, int width, int height, String mimeType) {
        // Insert into MediaStore.
        ContentValues values =
                getContentValuesForData(title, date, location, orientation, jpegLength, path,
                        width, height, mimeType);

         return insertImage(resolver, values);
    }

    // Overwrites the file and updates the MediaStore, or inserts the image if
    // one does not already exist.
    public static void updateImage(Uri imageUri, ContentResolver resolver, String title, long date,
            Location location, int orientation, ExifInterface exif, byte[] jpeg, int width,
            int height, String mimeType) {
        String path = generateFilepath(title, mimeType);
        writeFile(path, jpeg, exif, mimeType);
        updateImage(imageUri, resolver, title, date, location, orientation, jpeg.length, path,
                width, height, mimeType);
    }

    // Updates the image values in MediaStore, or inserts the image if one does
    // not already exist.
    public static void updateImage(Uri imageUri, ContentResolver resolver, String title,
            long date, Location location, int orientation, int jpegLength,
            String path, int width, int height, String mimeType) {

        ContentValues values =
                getContentValuesForData(title, date, location, orientation, jpegLength, path,
                        width, height, mimeType);

        // Update the MediaStore
        int rowsModified = resolver.update(imageUri, values, null, null);

        if (rowsModified == 0) {
            // If no prior row existed, insert a new one.
            Log.w(TAG, "updateImage called with no prior image at uri: " + imageUri);
            insertImage(resolver, values);
        } else if (rowsModified != 1) {
            // This should never happen
            throw new IllegalStateException("Bad number of rows (" + rowsModified
                    + ") updated for uri: " + imageUri);
        }
    }

    public static void deleteImage(ContentResolver resolver, Uri uri) {
        try {
            resolver.delete(uri, null, null);
        } catch (Throwable th) {
            Log.e(TAG, "Failed to delete image: " + uri);
        }
    }

    public static String generateFilepath(String title, String pictureFormat) {
        if (pictureFormat == null || pictureFormat.equalsIgnoreCase("jpeg")) {
            if (isSaveSDCard() && SDCard.instance().isWriteable()) {
                return SDCard.instance().getDirectory() + '/' + title + ".jpg";
            } else {
                return DIRECTORY + '/' + title + ".jpg";
            }
        } else {
            return RAW_DIRECTORY + '/' + title + ".raw";
        }
    }

    public static long getSpaceStorageSzie(){
        long spaceSize = 0;
        try {
            StatFs stat = null;
            if (!isDebug()) {
                stat = new StatFs("/storage/sdcard1");
            } else {
                stat = new StatFs("/storage/sdcard0");
            }
            spaceSize = stat.getAvailableBytes();
            spaceSize = spaceSize - HipParamters.SPACE_RESERVED_SIZE;
            if(spaceSize<=0){
                spaceSize = 0;
            }
            CameraLog.d("myCamera", "getSpaceStorageSzie:" + spaceSize);
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage", e);
            return UNKNOWN_SIZE;
        }
        return spaceSize;
    }

    private static boolean isDebug() {
        boolean isDebug = android.os.SystemProperties.getBoolean("debug.factorytest.dir", false);
        return isDebug;
    }
    public static long getAvailableSpace() {
        if (isSaveSDCard() && SDCard.instance().isWriteable()) {
            File dir = new File(SDCard.instance().getDirectory());
            dir.mkdirs();
            try {
                StatFs stat = new StatFs(SDCard.instance().getDirectory());
                long ret = stat.getAvailableBytes();
                return ret;
            } catch (Exception e) {
            }
            return UNKNOWN_SIZE;
        } else if (isSaveSDCard() && !SDCard.instance().isWriteable()) {
            return UNKNOWN_SIZE;
        } else {
            String state = Environment.getExternalStorageState();
            Log.d(TAG, "External storage state=" + state);
            if (Environment.MEDIA_CHECKING.equals(state)) {
                return PREPARING;
            }
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                return UNAVAILABLE;
            }

            File dir = new File(DIRECTORY);
            dir.mkdirs();
            if (!dir.isDirectory() || !dir.canWrite()) {
                return UNAVAILABLE;
            }

            try {
                StatFs stat = new StatFs(DIRECTORY);
                return stat.getAvailableBlocks() * (long) stat.getBlockSize();
            } catch (Exception e) {
                Log.i(TAG, "Fail to access external storage", e);
            }
            return UNKNOWN_SIZE;
        }
    }

    /**
     * OSX requires plugged-in USB storage to have path /DCIM/NNNAAAAA to be
     * imported. This is a temporary fix for bug#1655552.
     */
    public static void ensureOSXCompatible() {
        File nnnAAAAA = new File(DCIM, "100ANDRO");
        if (!(nnnAAAAA.exists() || nnnAAAAA.mkdirs())) {
            Log.e(TAG, "Failed to create " + nnnAAAAA.getPath());
        }
    }

    private static Uri insertImage(ContentResolver resolver, ContentValues values) {
        Uri uri = null;
        try {
            uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }
}
