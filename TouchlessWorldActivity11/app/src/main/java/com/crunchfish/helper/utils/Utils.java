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

package com.crunchfish.helper.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

/**
 * @adaptions Crunchfish AB This code is written for evaluation and educational
 *            purposes only. Crunchfish AB does not take any responsibilities to
 *            the functionality or usage of this code.
 */
public class Utils {

    /**
     *  Apparently on old Nexus 7 tablets the CameraInfo.CAMERA_FACING_FRONT
     *  will not work even though the device has a front-facing camera.
     *  We need to pass 0 when creating the Camera on that device.
     *
     *  To distinguish between and old and a new Nexus 7 tablet we need to
     *  also check the screen resolution. Mainly the Build.MODEL or Build.ID
     *  is not enough.
     */
    public static boolean isDeviceOldNexus7(final Context ctx) {
        if(Build.MODEL.equals("Nexus 7")) {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            if (size.x == 1280 && size.y == 736) {
                return true;
            }
        }
        return false;
    }
}
