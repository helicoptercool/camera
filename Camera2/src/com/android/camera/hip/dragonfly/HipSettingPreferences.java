/**
 * 
 */

package com.android.camera.hip.dragonfly;

import java.io.File;
import java.io.IOException;

import com.android.camera.CameraLog;
import com.android.camera.Storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.os.SystemClock;
import android.preference.PreferenceManager;

/**
 * @author thundersoft
 */
public class HipSettingPreferences {

    final private static String TAG = "HipSettingPreferences";
    private static int MODE_STATE = HipParamters.CAMERA_MODE;

    final private static String PATH_PROPERTIES = "/data/data/com.android.camera2/camera.properties";

    static void initParamterSettings(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        prefs.edit().putInt(HipParamters.MODE, HipParamters.INVILD_PREFENCE_VALUE);
        int MODE_STATE = prefs.getInt(HipParamters.MODE, HipParamters.INVLID_MODE);
        if (MODE_STATE == HipParamters.INVLID_MODE) {
            MODE_STATE = HipParamters.CAMERA_MODE;
            prefs.edit().putInt(HipParamters.MODE, MODE_STATE).commit();
        }

        initParameters(prefs, HipParamters.PIC_LAPSE_TIME, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.PIC_BURST_RATE, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.VID_LAPSE_TIME, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.PIC_LAPSE_BURST_RATE, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.PIC_LAPSE_BURST_TIME, HipParamters.OPT_0);

        for (int i = 0; i <= HipParamters.SURVEILLANCE_MODE; i++) {
            initParameters(prefs, HipParamters.PICTURE_SIZE + "_" + i, HipParamters.OPT_0);
            initParameters(prefs, HipParamters.VIDEO_SIZE + "_" + i, HipParamters.OPT_0);
            initParameters(prefs, HipParamters.COLOR_EFFECT + "_" + i, HipParamters.OPT_0);
            initParameters(prefs, HipParamters.WHITE_BALANCE + "_" + i, HipParamters.OPT_0);
            initParameters(prefs, HipParamters.EXPOSURE + "_" + i, HipParamters.OPT_2);
            initParameters(prefs, HipParamters.PREVIEW_SIZE + "_" + i, HipParamters.OPT_0);
        }
        prefs.edit()
                .putInt(HipParamters.PICTURE_SIZE + "_" + HipParamters.SURVEILLANCE_MODE,
                        HipParamters.OPT_4).commit();
        prefs.edit()
                .putInt(HipParamters.VIDEO_SIZE + "_" + +HipParamters.SURVEILLANCE_MODE,
                        HipParamters.OPT_3).commit();
        prefs.edit()
                .putInt(HipParamters.PREVIEW_SIZE + "_" + HipParamters.SURVEILLANCE_MODE,
                        HipParamters.OPT_2).commit();
        initParameters(prefs, HipParamters.GPS, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.AUTO_FLIP_STATE, HipParamters.OPT_0);
        prefs.edit().putInt(HipParamters.AUTO_START, HipParamters.OPT_0).commit();
        initParameters(prefs, HipParamters.AUTO_PREVIEW, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.ZSL, HipParamters.OPT_0);
        initParameters(prefs, HipParamters.UNWRAP, HipParamters.OPT_1); // OPT_1: open unwrap , OPT_0 : close
        int numPic = getPictureNumber(prefs.getInt(HipParamters.PICTURE_SIZE + "_" + MODE_STATE,
                HipParamters.OPT_4));
        int numVio = calculateVideoTime(prefs.getInt(HipParamters.VIDEO_SIZE + "_" + MODE_STATE,
                HipParamters.OPT_3));
        prefs.edit().putInt(HipParamters.KEY_MEMPIC, numPic).commit();
        prefs.edit().putInt(HipParamters.KEY_MEMVIO, numVio).commit();
        prefs.edit().putInt(HipParamters.MOTION_DETECTOR, HipParamters.OPT_0).commit();
    }

    private static void initParameters(SharedPreferences Pref, String key, int defaultValue) {
        int value = Pref.getInt(key, HipParamters.INVILD_PREFENCE_VALUE);
        if (value == HipParamters.INVILD_PREFENCE_VALUE) {
            Pref.edit().putInt(key, defaultValue).commit();
        }
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("thundersoft_hip", Context.MODE_PRIVATE);
    }

    private static void save4Setting(Context context, String key, int opt) {
        SharedPreferences prf = getSharedPreferences(context);
        prf.edit().putInt(key, opt).commit();
    }

    public static void saveMotionDetector(Context context, boolean f) {
        save4Setting(context, HipParamters.MOTION_DETECTOR, f ? HipParamters.OPT_1
                : HipParamters.OPT_0);
    }
    public static void saveCameraUnwrap(Context context, boolean f) {
        save4Setting(context, HipParamters.UNWRAP, f ? HipParamters.OPT_1
                : HipParamters.OPT_0);
    }

    public static boolean getMotionDetector(Context context) {
        return get4Setting(context, HipParamters.MOTION_DETECTOR) == HipParamters.OPT_1;
    }
    public static boolean isCloseCameraUnwrap(Context context) {
        return get4Setting(context, HipParamters.UNWRAP) == HipParamters.OPT_0;
    }
    public static boolean getAutoPreview(Context context) {
        return get4Setting(context, HipParamters.AUTO_PREVIEW) == HipParamters.OPT_0;
    }
    private static void save4SwitchMode(Context context, String key, int opt) {
        SharedPreferences prf = getSharedPreferences(context);
        prf.edit().putInt(key + "_" + MODE_STATE, opt).commit();
    }

    public static int get4SwitchMode(Context context, String key) {
        SharedPreferences prf = getSharedPreferences(context);
        return prf.getInt(key + "_" + MODE_STATE, HipParamters.OPT_0);
    }
    public static int get4Setting(Context context, String key) {
        SharedPreferences prf = getSharedPreferences(context);
        return prf.getInt(key, HipParamters.OPT_0);
    }

    public static Intent getSwitchMode(Context context) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        SharedPreferences prf = getSharedPreferences(context);
        int switch_mode = prf.getInt(HipParamters.MODE, HipParamters.CAMERA_MODE);
        MODE_STATE = switch_mode;
        in.putExtra(HipParamters.MODE, switch_mode);
        in.putExtra(HipParamters.SWITCH_MODE, switch_mode);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.GET_CAMERA_MODE);
        CameraLog.e(TAG, "get:" + HipParamters.SWITCH_MODE + " = " + switch_mode + "   MODE_STATE="
                + MODE_STATE);

        return in;
    }

    public static Intent setSwitchMode(final Context context, Intent intent) {

        Intent in = new Intent(HipParamters.RESULT_ACTION);

        SharedPreferences prf = getSharedPreferences(context);
        final int switch_mode = intent.getIntExtra(HipParamters.MODE, HipParamters.INVLID_MODE);
        if (switch_mode > HipParamters.SURVEILLANCE_MODE
                || switch_mode <= HipParamters.INVLID_MODE) {
            in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            in.putExtra(HipParamters.RESULT_ERROR, HipParamters.ERROR_CODE0);
        } else {
            MODE_STATE = switch_mode;
            prf.edit().putInt(HipParamters.MODE, switch_mode).commit();
            in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        }

        CameraLog.e(TAG, HipParamters.SWITCH_MODE + " = " + switch_mode + "   MODE_STATE = "
                + MODE_STATE);
        in.putExtra("command", HipParamters.SWITCH_MODE);

        new Thread() {
            public void run() {
                updateMediaMemory(context);
                Intent intent = new Intent(HipParamters.HIP_CAMRRA_SWITCHMOD_EVENT);
                intent.putExtra(HipParamters.SWTMOD, switch_mode);
                intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
                intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
                context.sendBroadcast(intent);
            }
        }.start();
        return in;
    }

    public static Intent autoSwitchMode(final Context context, int switch_mode) {

        Intent in = new Intent(HipParamters.HIP_CAMRRA_SWITCHMOD_EVENT);

        SharedPreferences prf = getSharedPreferences(context);
        if (switch_mode > HipParamters.SURVEILLANCE_MODE
                || switch_mode <= HipParamters.INVLID_MODE) {
            in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            in.putExtra(HipParamters.RESULT_ERROR, HipParamters.ERROR_CODE0);
        } else {
            MODE_STATE = switch_mode;
            prf.edit().putInt(HipParamters.MODE, switch_mode).commit();
            in.putExtra(HipParamters.SWTMOD, switch_mode);
            in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
            in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        }

        CameraLog.e(TAG, "#autoSwitchMode " + " = " + switch_mode + "   MODE_STATE = "
                + MODE_STATE);
        in.putExtra("command", HipParamters.SWITCH_MODE);

        new Thread() {
            public void run() {
                updateMediaMemory(context);
            }
        }.start();
        return in;
    }

    public static Intent setParameters(final Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int opt = 0;

        boolean isUpdateMediaMemory = false;
        // set picture size
        if (intent.hasExtra(HipParamters.PICTURE_SIZE)) {
            opt = intent.getIntExtra(HipParamters.PICTURE_SIZE, HipParamters.OPT_0);
            if (opt > 4) {
                opt = HipParamters.OPT_0;
            }
            save4SwitchMode(context, HipParamters.PICTURE_SIZE, opt);
            int preview_opt = 0;
            switch (opt) {
                case HipParamters.OPT_0:
                    preview_opt = HipParamters.OPT_0;
                    break;
                case HipParamters.OPT_1:
                    preview_opt = HipParamters.OPT_0;
                    break;
                case HipParamters.OPT_2:
                    preview_opt = HipParamters.OPT_0;
                    break;
                case HipParamters.OPT_3:
                    preview_opt = HipParamters.OPT_1;
                    break;
                case HipParamters.OPT_4:
                    preview_opt = HipParamters.OPT_2;
                    break;
            }
            save4SwitchMode(context, HipParamters.PREVIEW_SIZE, preview_opt);
            CameraLog.e(TAG, HipParamters.PICTURE_SIZE + " = " + opt + ",preview_opt=" + preview_opt + ",MODE_STATE="+ MODE_STATE);
            isUpdateMediaMemory = true;
        }
        if (intent.hasExtra(HipParamters.VIDEO_SIZE)) {
            // set video size
            opt = intent.getIntExtra(HipParamters.VIDEO_SIZE, HipParamters.OPT_0);
            if (opt > 3) {
                opt = HipParamters.OPT_0;
            }
            save4SwitchMode(context, HipParamters.VIDEO_SIZE, opt);
            int preview_opt=0, pic_opt = 0;
            switch (opt) {
                case HipParamters.OPT_0:
                    pic_opt = preview_opt = HipParamters.OPT_0;
                    break;
                case HipParamters.OPT_1:
                    preview_opt = HipParamters.OPT_0;
                    pic_opt = HipParamters.OPT_2;
                    break;
                case HipParamters.OPT_2:
                    preview_opt = HipParamters.OPT_1;
                    pic_opt = HipParamters.OPT_3;
                    break;
                case HipParamters.OPT_3:
                    preview_opt = HipParamters.OPT_2;
                    pic_opt = HipParamters.OPT_4;
                    break;
            }
            save4SwitchMode(context, HipParamters.PICTURE_SIZE, pic_opt);
            save4SwitchMode(context, HipParamters.PREVIEW_SIZE, preview_opt);
            CameraLog.e(TAG, HipParamters.PICTURE_SIZE + " = " + pic_opt + " , "
                    + HipParamters.VIDEO_SIZE + " = " + opt + ",preview_opt=" + preview_opt
                    + ",MODE_STATE=" + MODE_STATE);
            isUpdateMediaMemory = true;
        }

        if (intent.hasExtra(HipParamters.WHITE_BALANCE)) {
            // set white balance
            opt = intent.getIntExtra(HipParamters.WHITE_BALANCE, HipParamters.OPT_0);
            if (opt > 4) {
                opt = HipParamters.OPT_0;
            }
            save4SwitchMode(context, HipParamters.WHITE_BALANCE, opt);
            CameraLog.e(TAG, HipParamters.WHITE_BALANCE + " = " + opt + "   MODE_STATE="
                    + MODE_STATE);
        }

        if (intent.hasExtra(HipParamters.COLOR_EFFECT)) {
            // set color effect
            opt = intent.getIntExtra(HipParamters.COLOR_EFFECT, HipParamters.OPT_0);
            if (opt > 4) {
                opt = HipParamters.OPT_0;
            }
            save4SwitchMode(context, HipParamters.COLOR_EFFECT, opt);
            CameraLog.e(TAG, HipParamters.COLOR_EFFECT + " = " + opt + "   MODE_STATE="
                    + MODE_STATE);
        }

        if (intent.hasExtra(HipParamters.GPS)) {
            // set gps
            boolean isGPS = true;
            if (intent.hasExtra(HipParamters.GPS)) {
                isGPS = intent.getBooleanExtra(HipParamters.GPS, true);
            }
            save4Setting(context, HipParamters.GPS, isGPS ? HipParamters.OPT_0 : HipParamters.OPT_1);
            CameraLog.e(TAG, HipParamters.GPS + " = " + isGPS + "   MODE_STATE=" + MODE_STATE);
        }

        if (intent.hasExtra(HipParamters.EXPOSURE)) {
            // set exposure
            opt = intent.getIntExtra(HipParamters.EXPOSURE, HipParamters.OPT_0);
            if (opt > 4) {
                opt = HipParamters.OPT_0;
            }
            save4SwitchMode(context, HipParamters.EXPOSURE, opt);
            CameraLog.e(TAG, HipParamters.EXPOSURE + " = " + opt + "   MODE_STATE = " + MODE_STATE);
        }
        

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.SET_PARAMETERS);
        if (isUpdateMediaMemory) {
            new Thread() {
                public void run() {
                    updateMediaMemory(context);
                }
            }.start();
        }
        return in;
    }

    public static int getMode(Context context){
        SharedPreferences prf = getSharedPreferences(context);
        MODE_STATE = prf.getInt(HipParamters.MODE, HipParamters.CAMERA_MODE);
        return MODE_STATE;
    }

    public static Intent getParameters(Context context, Intent intent) {

        SharedPreferences prf = getSharedPreferences(context);
        int switch_mode = prf.getInt(HipParamters.MODE, MODE_STATE);
        MODE_STATE = switch_mode;
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        int type = -1;
        if (intent.hasExtra(HipParamters.PICTURE_SIZE)) {
            type = get4SwitchMode(context, HipParamters.PICTURE_SIZE);
            CameraLog.e(TAG, "SharedPreferences->picture_size type:" + type);
            if (type != HipParamters.OPT_INV) {
                in.putExtra(HipParamters.PICTURE_SIZE, type);
            }else {
                in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            }
        }
        if (intent.hasExtra(HipParamters.VIDEO_SIZE)) {
            type = get4SwitchMode(context, HipParamters.VIDEO_SIZE);
            CameraLog.e(TAG, "SharedPreferences->video_size type:" + type);
            if (type != HipParamters.OPT_INV) {
                in.putExtra(HipParamters.VIDEO_SIZE, type);
            }else {
                in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            }
        }
        if (intent.hasExtra(HipParamters.WHITE_BALANCE)) {
            type = get4SwitchMode(context, HipParamters.WHITE_BALANCE);
            CameraLog.e(TAG, "SharedPreferences->white_balance type:" + type);
            if (type != HipParamters.OPT_INV) {
                in.putExtra(HipParamters.WHITE_BALANCE, type);
            } else {
                in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            }
        }
        if (intent.hasExtra(HipParamters.COLOR_EFFECT)) {
            type = get4SwitchMode(context, HipParamters.COLOR_EFFECT);
            CameraLog.e(TAG, "SharedPreferences->white_balance type:" + type);
            if (type != HipParamters.OPT_INV) {
                in.putExtra(HipParamters.COLOR_EFFECT, type);
            } else {
                in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            }
        }

        if (intent.hasExtra(HipParamters.EXPOSURE)) {
            type = get4SwitchMode(context, HipParamters.EXPOSURE);
            CameraLog.e(TAG, "SharedPreferences->EXPOSURE type:" + type);
            if (type != HipParamters.OPT_INV) {
                in.putExtra(HipParamters.EXPOSURE, type);
            } else {
                in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_FAIL);
            }
        }
        return in;
    }

    public static String getPictureSize(int sizeopt) {
        switch (sizeopt) {
            case HipParamters.OPT_0:
                return HipParamters.PIC_SIZE_OPT_0;
            case HipParamters.OPT_1:
                return HipParamters.PIC_SIZE_OPT_1;
            case HipParamters.OPT_2:
                return HipParamters.PIC_SIZE_OPT_2;
            case HipParamters.OPT_3:
                return HipParamters.PIC_SIZE_OPT_3;
            case HipParamters.OPT_4:
                return HipParamters.PIC_SIZE_OPT_4;
            default:
                break;
        }
        return HipParamters.PIC_SIZE_OPT_0;
    }

    private static String getPreivewSize(int sizeopt) {
        switch (sizeopt) {
            case HipParamters.OPT_0:
                return HipParamters.PRE_SIZE_OPT_0;
            case HipParamters.OPT_1:
                return HipParamters.PRE_SIZE_OPT_1;
            case HipParamters.OPT_2:
                return HipParamters.PRE_SIZE_OPT_2;
            default:
                break;
        }
        return HipParamters.PRE_SIZE_OPT_0;
    }

    public static String getVideoSize(int sizeopt) {
        switch (sizeopt) {
            case HipParamters.OPT_0:
                return HipParamters.VIDEO_SIZE_OPT_0;
            case HipParamters.OPT_1:
                return HipParamters.VIDEO_SIZE_OPT_1;
            case HipParamters.OPT_2:
                return HipParamters.VIDEO_SIZE_OPT_2;
            case HipParamters.OPT_3:
                return HipParamters.VIDEO_SIZE_OPT_3;
            default:
                break;
        }
        return HipParamters.VIDEO_SIZE_OPT_0;
    }

    private static String getWhiteBalance(int sizeopt) {
        switch (sizeopt) {
            case HipParamters.OPT_0:
                return HipParamters.WB_OPT_0;
            case HipParamters.OPT_1:
                return HipParamters.WB_OPT_1;
            case HipParamters.OPT_2:
                return HipParamters.WB_OPT_2;
            case HipParamters.OPT_3:
                return HipParamters.WB_OPT_3;
            case HipParamters.OPT_4:
                return HipParamters.WB_OPT_4;
            default:
                break;
        }
        return HipParamters.WB_OPT_0;
    }

    private static String getColorEffect(int effectopt) {
        switch (effectopt) {
            case HipParamters.OPT_0:
                return HipParamters.EFFECT_OPT_0;
            case HipParamters.OPT_1:
                return HipParamters.EFFECT_OPT_1;
            case HipParamters.OPT_2:
                return HipParamters.EFFECT_OPT_2;
            default:
                break;
        }
        return HipParamters.EFFECT_OPT_0;
    }

    public static long getLapsetime(int timeopt) {
        switch (timeopt) {
            case HipParamters.OPT_0:
                return HipParamters.LAP_OPT_0;
            case HipParamters.OPT_1:
                return HipParamters.LAP_OPT_1;
            case HipParamters.OPT_2:
                return HipParamters.LAP_OPT_2;
            case HipParamters.OPT_3:
                return HipParamters.LAP_OPT_3;
            case HipParamters.OPT_4:
                return HipParamters.LAP_OPT_4;
            default:
                break;
        }
        return HipParamters.LAP_OPT_0;
    }

    public static int getExposure(int exopt) {
        switch (exopt) {
            case HipParamters.OPT_0:
                return HipParamters.EX_OPT_4;
            case HipParamters.OPT_1:
                return HipParamters.EX_OPT_3;
            case HipParamters.OPT_2:
                return HipParamters.EX_OPT_2;
            case HipParamters.OPT_3:
                return HipParamters.EX_OPT_1;
            case HipParamters.OPT_4:
                return HipParamters.EX_OPT_0;
            default:
                break;
        }
        return HipParamters.EX_OPT_2;
    }

    public static int getPictrueBurst(int exopt) {
        switch (exopt) {
            case HipParamters.OPT_0:
                return 5;
            case HipParamters.OPT_1:
                return 15;
            case HipParamters.OPT_2:
                return 30;
            default:
                break;
        }
        return 5;
    }

    public static Intent setZSL(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);
        boolean isZSL = intent.getIntExtra(HipParamters.ZSL, HipParamters.OPT_0) == HipParamters.OPT_0;
        save4Setting(context, HipParamters.PIC_LAPSE_TIME, isZSL ? HipParamters.OPT_0
                : HipParamters.OPT_1);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.ZSL);
        return in;
    }

    public static Intent setPictureLapseTime(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);
        int time_opt = intent.getIntExtra(HipParamters.PICTURE_LAPSE_TIME, HipParamters.OPT_0);
        save4Setting(context, HipParamters.PIC_LAPSE_TIME, time_opt);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.PIC_LAPSE_TIME);
        CameraLog.e(TAG, "set:" + HipParamters.PIC_LAPSE_TIME + " = " + time_opt + "   MODE_STATE="
                + MODE_STATE);
        return in;
    }

    public static Intent getPictureLapseTime(Context context, Intent intent) {
        int switch_mode = getMode(context);
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int time_opt = get4Setting(context, HipParamters.PIC_LAPSE_TIME);
        in.putExtra(HipParamters.PICTURE_LAPSE_TIME, time_opt);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        in.putExtra("command", HipParamters.GET_PICTURE_LAPSE);
        CameraLog.e(TAG, "command : " + HipParamters.PICTURE_LAPSE_TIME + " ,time_opt = "
                + time_opt + "   MODE_STATE=" + MODE_STATE);

        return in;
    }

    public static Intent setPictureBurst(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int burst_opt = intent.getIntExtra(HipParamters.PICTURE_BURST_RATE, HipParamters.OPT_0);
        save4Setting(context, HipParamters.PIC_BURST_RATE, burst_opt);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.SET_PICTURE_LAPSE);

        CameraLog.e(TAG, "  burst_opt = " + burst_opt + " ,MODE_STATE = " + MODE_STATE
                + "  , command = " + HipParamters.SET_PICTURE_LAPSE);
        return in;
    }

    public static Intent getPictureBurst(Context context, Intent intent) {
        int switch_mode = getMode(context);
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int burst_opt = get4Setting(context, HipParamters.PIC_BURST_RATE);
        in.putExtra(HipParamters.PICTURE_BURST_RATE, burst_opt);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.PICTURE_BURST, switch_mode == HipParamters.PIC_BURST_MODE);
        in.putExtra(HipParamters.PICTURE_LAPSE_BURST,
                switch_mode == HipParamters.PIC_BURST_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.VIDEO_LAPSE, switch_mode == HipParamters.VID_TIME_LAPSE_MODE);
        in.putExtra("command", HipParamters.GET_PICTURE_BURST);
        CameraLog.e(TAG, "  burst_opt = " + burst_opt + " ,MODE_STATE = " + MODE_STATE
                + "  , command = " + HipParamters.GET_PICTURE_BURST);

        return in;
    }

    public static Intent setPictureLapseBurst(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);
        int time_opt = intent.getIntExtra(HipParamters.PICTURE_LAPSE_TIME, HipParamters.OPT_0);
        int burst_opt = intent.getIntExtra(HipParamters.PICTURE_BURST_RATE, HipParamters.OPT_0);

        save4Setting(context, HipParamters.PIC_LAPSE_BURST_TIME, time_opt);
        save4Setting(context, HipParamters.PIC_LAPSE_BURST_RATE, burst_opt);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.SET_PICTURE_LAPSE);
        CameraLog.e(TAG, "  time_opt = " + time_opt + " , burst_opt = " + burst_opt
                + " , MODE_STATE = " + MODE_STATE + "  , command = "
                + HipParamters.SET_PICTURE_LAPSE);

        return in;

    }

    public static Intent getPictureLapseBurst(Context context, Intent intent) {
        int switch_mode = getMode(context);
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int burst_opt = get4Setting(context, HipParamters.PIC_LAPSE_BURST_RATE);
        int time_opt = get4Setting(context, HipParamters.PIC_LAPSE_BURST_TIME);
        in.putExtra(HipParamters.PIC_LAPSE_TIME, time_opt);
        in.putExtra(HipParamters.PICTURE_BURST_RATE, burst_opt);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.GET_PICTURE_BURST);
        in.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.PICTURE_BURST, switch_mode == HipParamters.PIC_BURST_MODE);
        in.putExtra(HipParamters.PICTURE_LAPSE_BURST,
                switch_mode == HipParamters.PIC_BURST_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.VIDEO_LAPSE, switch_mode == HipParamters.VID_TIME_LAPSE_MODE);

        CameraLog.e(TAG, "  time_opt = " + time_opt + " , burst_opt = " + burst_opt
                + " , MODE_STATE = " + MODE_STATE + "  , command = "
                + HipParamters.GET_PICTURE_BURST);
        return in;
    }

    public static Intent setVideoLapsetime(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int time_opt = intent.getIntExtra(HipParamters.VIDEO_LAPSE_TIME, HipParamters.OPT_0);
        save4Setting(context, HipParamters.VID_LAPSE_TIME,time_opt);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.SET_VIDEO_LAPSE);

        CameraLog.e(TAG, "  time_opt = " + time_opt + " , MODE_STATE = " + MODE_STATE
                + "  , command = " + HipParamters.SET_VIDEO_LAPSE);
        return in;
    }

    public static Intent getVideoLapsetime(Context context, Intent intent) {
        int switch_mode = getMode(context);
        Intent in = new Intent(HipParamters.RESULT_ACTION);
        int time_opt = get4Setting(context, HipParamters.VID_LAPSE_TIME);
        in.putExtra(HipParamters.VIDEO_LAPSE_TIME, time_opt);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);

        in.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.PICTURE_BURST, switch_mode == HipParamters.PIC_BURST_MODE);
        in.putExtra(HipParamters.PICTURE_LAPSE_BURST,
                switch_mode == HipParamters.PIC_BURST_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.VIDEO_LAPSE, switch_mode == HipParamters.VID_TIME_LAPSE_MODE);
        in.putExtra("command", HipParamters.GET_VIDEO_LAPSE);

        CameraLog.e(TAG, "  time_opt = " + time_opt + " , MODE_STATE = " + MODE_STATE
                + "  , command = " + HipParamters.GET_VIDEO_LAPSE);
        return in;
    }

    public static Intent getGeneralIntent(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        SharedPreferences prf = getSharedPreferences(context);
        boolean isAutoFlip = prf.getInt(HipParamters.AUTO_FLIP_STATE, HipParamters.OPT_0) == 0;
        in.putExtra(HipParamters.AUTO_FLIP, isAutoFlip);
        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.GENERAL);

        CameraLog.e(TAG, "  isAutoFlip = " + isAutoFlip + " , MODE_STATE = " + MODE_STATE
                + "  , command = " + HipParamters.GENERAL);
        return in;
    }

    public static Intent getStillIntent(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int switch_mode = getMode(context);
        in.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.PICTURE_BURST, switch_mode == HipParamters.PIC_BURST_MODE);
        in.putExtra(HipParamters.PICTURE_LAPSE_BURST,
                switch_mode == HipParamters.PIC_BURST_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.VIDEO_LAPSE, switch_mode == HipParamters.VID_TIME_LAPSE_MODE);

        int pic_time_opt = get4Setting(context, HipParamters.PIC_LAPSE_TIME);
        in.putExtra(HipParamters.PICTURE_LAPSE_TIME, pic_time_opt);

        int burst_opt = get4Setting(context, HipParamters.PIC_BURST_RATE);
        in.putExtra(HipParamters.PICTURE_BURST_RATE, burst_opt);

        int vid_time_opt = get4Setting(context, HipParamters.VID_LAPSE_TIME);
        in.putExtra(HipParamters.VIDEO_LAPSE_TIME, vid_time_opt);

        int pic_size_opt = get4SwitchMode(context, HipParamters.PICTURE_SIZE);
        in.putExtra(HipParamters.PICTURE_SIZE, pic_size_opt);

        int vid_size_opt = get4SwitchMode(context, HipParamters.VIDEO_SIZE);
        in.putExtra(HipParamters.VIDEO_SIZE, vid_size_opt);

        int bur_time_opt = get4Setting(context, HipParamters.PIC_LAPSE_BURST_TIME);
        in.putExtra(HipParamters.PIC_LAPSE_BURST_TIME, bur_time_opt);

        int bur_rate_opt = get4Setting(context, HipParamters.PIC_LAPSE_BURST_RATE);
        in.putExtra(HipParamters.PIC_LAPSE_BURST_RATE, bur_rate_opt);

        int wbl_opt = get4SwitchMode(context, HipParamters.WHITE_BALANCE);
        in.putExtra(HipParamters.WHITE_BALANCE, wbl_opt);
        int ex_opt = get4SwitchMode(context, HipParamters.EXPOSURE);
        in.putExtra(HipParamters.EXPOSURE, ex_opt);
        int ce_opt = get4SwitchMode(context, HipParamters.COLOR_EFFECT);
        in.putExtra(HipParamters.COLOR_EFFECT, ce_opt);
        CameraLog.e(TAG, " pic_time_opt = " + pic_time_opt + ",  burst_opt = " + burst_opt
                + ",  vid_time_opt = " + vid_time_opt + " ,pic_size_opt = " + pic_size_opt
                + "  ,vid_size_opt = " + vid_size_opt + " ,bur_time_opt = " + bur_time_opt
                + " ,bur_rate_opt = " + bur_rate_opt + " ,wbl_opt = " + wbl_opt + "  ,ex_opt = "
                + ex_opt + "  ,ce_opt = " + ce_opt + " ,MODE_STATE = " + MODE_STATE
                + "  , command = " + HipParamters.STILL);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.STILL);
        return in;
    }

    public static Intent getSettingsIntent(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        int switch_mode = getMode(context);
        boolean isGPS = get4Setting(context, HipParamters.GPS) == HipParamters.OPT_0;
        in.putExtra(HipParamters.GPS, isGPS);

        boolean isAuto_flip = get4Setting(context, HipParamters.AUTO_FLIP_STATE) == HipParamters.OPT_0;
        in.putExtra(HipParamters.AUTO_FLIP, isAuto_flip);

        int picture_size = get4Setting(context, HipParamters.PICTURE_SIZE);
        int video_size = get4Setting(context, HipParamters.VIDEO_SIZE);
        int white_balance = get4SwitchMode(context, HipParamters.WHITE_BALANCE);
        int color_effect = get4SwitchMode(context, HipParamters.COLOR_EFFECT);
        int exposure = get4SwitchMode(context, HipParamters.EXPOSURE);

        in.putExtra(HipParamters.SWITCH_MODE, switch_mode);
        in.putExtra(HipParamters.MODE, switch_mode);
        in.putExtra(HipParamters.PICTURE_SIZE, picture_size);
        in.putExtra(HipParamters.VIDEO_SIZE, video_size);
        in.putExtra(HipParamters.WHITE_BALANCE, white_balance);
        in.putExtra(HipParamters.COLOR_EFFECT, color_effect);
        in.putExtra(HipParamters.EXPOSURE, exposure);

        int pic_time_opt = get4Setting(context, HipParamters.PIC_LAPSE_TIME);
        in.putExtra(HipParamters.PICTURE_LAPSE_TIME, pic_time_opt);
        int burst_opt =  get4Setting(context, HipParamters.PIC_BURST_RATE);
        in.putExtra(HipParamters.PICTURE_BURST_RATE, burst_opt);

        int vid_time_opt =  get4Setting(context, HipParamters.VID_LAPSE_TIME);
        in.putExtra(HipParamters.VIDEO_LAPSE_TIME, vid_time_opt);

        in.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.PICTURE_BURST, switch_mode == HipParamters.PIC_BURST_MODE);
        in.putExtra(HipParamters.PICTURE_LAPSE_BURST,
                switch_mode == HipParamters.PIC_BURST_TIME_LAPSE_MODE);
        in.putExtra(HipParamters.VIDEO_LAPSE, switch_mode == HipParamters.VID_TIME_LAPSE_MODE);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.SETTING);

        CameraLog.e(TAG, " pic_time_opt = " + pic_time_opt + " , picture_size = " + picture_size
                + ",  burst_opt = " + burst_opt
                + ",  vid_time_opt = " + vid_time_opt + " ,MODE_STATE=" + MODE_STATE
                + "  , command = " + HipParamters.SETTING);

        return in;
    }

    public static CameraSettings getCameraSettings(Context context) {
        CameraSettings setting = new CameraSettings();

        int switch_mode = getMode(context);
        setting.cammod = switch_mode;
        boolean isGPS = get4Setting(context, HipParamters.GPS) == HipParamters.OPT_0;

        boolean isAuto_flip = get4Setting(context, HipParamters.AUTO_FLIP_STATE) == HipParamters.OPT_0;
        setting.camflp = isAuto_flip;

        int picture_size = get4SwitchMode(context, HipParamters.PICTURE_SIZE);
        setting.picres = picture_size;
        int video_size = get4SwitchMode(context, HipParamters.VIDEO_SIZE);
        setting.vdores = video_size;
        int white_balance = get4SwitchMode(context, HipParamters.WHITE_BALANCE);
        setting.picwbl = setting.vdowbl = white_balance;
        int color_effect = get4SwitchMode(context, HipParamters.COLOR_EFFECT);
        setting.piccef = setting.vdocef = color_effect;
        int exposure = get4SwitchMode(context, HipParamters.EXPOSURE);
        setting.picexp = exposure;

        int pic_time_opt = get4Setting(context, HipParamters.PIC_LAPSE_TIME);
        setting.pictlpInterval = pic_time_opt;
        int burst_opt = get4Setting(context, HipParamters.PIC_BURST_RATE);
        setting.picburRate = burst_opt;

        int vid_time_opt = get4Setting(context, HipParamters.VID_LAPSE_TIME);
        setting.vdotlpInterval = vid_time_opt;

        int pic_bur_time_opt = get4Setting(context, HipParamters.PIC_LAPSE_BURST_TIME);
        setting.pictwbInterval = pic_bur_time_opt;
        int pic_bur_time_rate = get4Setting(context, HipParamters.PIC_LAPSE_BURST_RATE);
        setting.pictwbRate = pic_bur_time_rate;

        CameraLog.e(TAG, " pic_time_opt = " + pic_time_opt + "  , picture_size = " + picture_size
                + " , video_size = " + video_size + ",  burst_opt = " + burst_opt
                + ",  vid_time_opt = " + vid_time_opt + " ,MODE_STATE = " + switch_mode
                + "  , command = " + HipParamters.SETTING);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        setting.svlmod = pref.getInt(HipParamters.PREF_KEY_MODE, 0);
        setting.auddetTrigger = pref.getBoolean(HipParamters.PREF_KEY_AUDIO_TRIGGER, false);
        setting.auddetLevel = pref.getInt(HipParamters.PREF_KEY_AUDIO_LEVEL, 1);
        setting.auddetMode = pref.getInt(HipParamters.PREF_KEY_AUDIO_ACTION, 0);
        setting.auddetReclen = pref.getInt(HipParamters.PREF_KEY_AUDIO_RECLEN, 5000);
        setting.auddetInterval = pref.getInt(HipParamters.PREF_KEY_AUDIO_INTERVAL, 1000);
        setting.motdetTrigger = pref.getBoolean(HipParamters.PREF_KEY_MOTION_TRIGGER, false);
        setting.motdetLevel = pref.getInt(HipParamters.PREF_KEY_MOTION_LEVEL, 1);
        setting.motdetReclen = pref.getInt(HipParamters.PREF_KEY_MOTION_RECLEN, 5000);
        setting.motdetMode = pref.getInt(HipParamters.PREF_KEY_MOTION_ACTION, 0);
        setting.motdetInterval = pref.getInt(HipParamters.PREF_KEY_MOTION_INTERVAL, 1000);
        setting.motdetReclen = pref.getInt(HipParamters.PREF_KEY_MOTION_RECLEN, 5000);
        setting.autdel = pref.getBoolean(HipParamters.PREF_KEY_AUTDEL, false);
        return setting;
    }

    public static CameraStatus getCameraStatus(Context context) {
        int switch_mode = getMode(context);

        CameraStatus status = new CameraStatus();
        status.swtmod = switch_mode;
        status.mempic = getPictureNumber(context);
        status.memvid = calculateVideoTime(context);
        return status;
    }

    public static Intent getBasstaIntent(Context context, Intent in, HipCameraService.Listener l) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);

        int time = 0;
        if (l != null) {
            boolean isRecording = l.isRecording();
            CameraLog.e(TAG, "getBasstaIntent() isRecording =" + isRecording);
            intent.putExtra(HipParamters.RECORD_STATE,isRecording);
            intent.putExtra(HipParamters.RECORDING_TIME,
                    SystemClock.uptimeMillis() - l.getRecordingTime());
        } else {
            intent.putExtra(HipParamters.RECORD_STATE, false);
        }

        int number = getPictureNumber(context);
        intent.putExtra(HipParamters.REMAINING_PICTURE, number);

        time = calculateVideoTime(context);
        intent.putExtra(HipParamters.REMAINING_VIDEO, time);
        intent.putExtra("command", HipParamters.SETTING);

        CameraLog.e(TAG, " time = " + time + ",MODE_STATE = " + MODE_STATE + "  , command = "
                + HipParamters.BASSTA);

        return intent;
    }

    public static Intent setAutoFlip(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        final int opt = intent.getBooleanExtra(HipParamters.AUTO_FLIP, true) ? HipParamters.OPT_0
                : HipParamters.OPT_1;
        save4Setting(context, HipParamters.AUTO_FLIP_STATE, opt);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.SET_AUTO_FLIP);
        new Thread() {
            public void run() {
                CameraLog.e(TAG, " opt = " + opt + " ,run .... ");
                File f = new File(PATH_PROPERTIES);
                if (opt == HipParamters.OPT_0) {
                    if (f.exists()) {
                        CameraLog.e(TAG, " opt = " + opt + " ,delete .... ");
                        f.delete();
                    }
                } else {
                    if (!f.exists()) {
                        try {
                            CameraLog.e(TAG, " opt = " + opt + " ,create .... ");
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }.start();
        CameraLog.e(TAG, " opt = "+opt + " ,command = " +HipParamters.SET_AUTO_FLIP);
        return in;
    }

    public static Intent getAutoFlip(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        boolean isAuto_flip = get4Setting(context, HipParamters.AUTO_FLIP_STATE) == HipParamters.OPT_0;
        in.putExtra(HipParamters.AUTO_FLIP, isAuto_flip);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.GET_AUTO_FLIP);
        CameraLog.e(TAG, " isAuto_flip ="+isAuto_flip + " ,command = " +HipParamters.GET_AUTO_FLIP);
        return in;
    }


    public static Intent getGPS(Context context, Intent intent) {
        Intent in = new Intent(HipParamters.RESULT_ACTION);

        boolean isGPS = get4Setting(context, HipParamters.AUTO_FLIP_STATE) == HipParamters.OPT_0;
        in.putExtra(HipParamters.GPS, isGPS);

        in.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        in.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        in.putExtra("command", HipParamters.GET_GPS);
        CameraLog.e(TAG, " isGPS ="+isGPS + " ,command = " +HipParamters.GET_GPS);

        return in;
    }

    public static Intent getVideoSetting(Context context, Intent in) {
        int switch_mode = getMode(context);
        Intent intent = new Intent(HipParamters.RESULT_ACTION);

        int time_opt = get4Setting(context, HipParamters.VID_LAPSE_TIME);
        intent.putExtra(HipParamters.VIDEO_LAPSE_TIME, time_opt);

        int video_opt = get4Setting(context, HipParamters.VIDEO_SIZE);
        intent.putExtra(HipParamters.VIDEO_SIZE, video_opt);

        int wb_opt = get4SwitchMode(context, HipParamters.WHITE_BALANCE);
        intent.putExtra(HipParamters.WHITE_BALANCE, wb_opt);

        int ef_opt = get4SwitchMode(context, HipParamters.COLOR_EFFECT);
        intent.putExtra(HipParamters.COLOR_EFFECT, ef_opt);

        intent.putExtra(HipParamters.PICTURE_LAPSE, switch_mode == HipParamters.PIC_TIME_LAPSE_MODE);
        intent.putExtra(HipParamters.PICTURE_BURST, switch_mode == HipParamters.PIC_BURST_MODE);
        intent.putExtra(HipParamters.PICTURE_LAPSE_BURST,
                switch_mode == HipParamters.PIC_BURST_TIME_LAPSE_MODE);
        intent.putExtra(HipParamters.VIDEO_LAPSE, switch_mode == HipParamters.VID_TIME_LAPSE_MODE);

        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.VIDEOSETING);
        CameraLog.e(TAG, " time_opt =" + time_opt + " ,video_opt = " + video_opt + " ,ef_opt = "
                + ef_opt + "  ,wb_opt = " + wb_opt + " ,command = " + HipParamters.VIDEOSETING);
        return intent;
    }

    public static Intent getMEMPIC(Context context) {

        Intent intent = new Intent(HipParamters.HIP_DRAGONFLY_CAMERA_ACTION_MEMPIC);

        int number = getPictureNumber(context);
        intent.putExtra(HipParamters.KEY_MEMPIC, number);

        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.VIDEOSETING);
        CameraLog.e(TAG, " getMEMPIC    number = " + number + " ,command = "
                + HipParamters.KEY_MEMPIC);
        return intent;
    }

    public static Intent getMEMVIO(Context context) {

        Intent intent = new Intent(HipParamters.HIP_DRAGONFLY_CAMERA_ACTION_MEMVIO);

        int number = calculateVideoTime(context);
        intent.putExtra(HipParamters.KEY_MEMVIO, number);

        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.VIDEOSETING);
        CameraLog.e(TAG, " getMEMVIO    number = " + number + " ,command = "
                + HipParamters.KEY_MEMVIO);
        return intent;
    }

    public static Parameters refreshCameraSetting(Context context, Parameters p) {
        int switch_mode = getMode(context);
        int ex_opt, wb_opt, c_ef_opt, pic_size_opt, vid_size_opt;
        CameraLog.e(TAG, "#initCameraState  switch_mode " + " = " + switch_mode);
        c_ef_opt = get4SwitchMode(context, HipParamters.COLOR_EFFECT);
        p.setColorEffect(getColorEffect(c_ef_opt));

        wb_opt = get4SwitchMode(context, HipParamters.WHITE_BALANCE);
        p.setWhiteBalance(getWhiteBalance(wb_opt));

        ex_opt = get4SwitchMode(context, HipParamters.EXPOSURE);
        p.setExposureCompensation(getExposure(ex_opt));

        pic_size_opt = get4SwitchMode(context, HipParamters.PICTURE_SIZE);
        String picture_size = getPictureSize(pic_size_opt);
        int[] pSize = splitsSize(picture_size);
        p.setPictureSize(pSize[0], pSize[1]);

        vid_size_opt = get4SwitchMode(context, HipParamters.VIDEO_SIZE);
        String video_size = getVideoSize(vid_size_opt);
        p.set("video-size", video_size);

        int pre_size_opt = get4SwitchMode(context, HipParamters.PREVIEW_SIZE);
        String preview_size = getPreivewSize(pre_size_opt);
        int[] preSize = splitsSize(preview_size);
        p.setPreviewSize(preSize[0], preSize[1]);

        CameraLog.e(TAG, " wb_opt = " + wb_opt + " ,c_ef_opt = " + c_ef_opt + " ,ex_opt = "
                + ex_opt + " ,pic_size_opt = " + pic_size_opt + " ,vid_size_opt = "
                + vid_size_opt + " ,pre_size_opt = " + pre_size_opt + "  switch_mode = "
                 +switch_mode +  " , refreshCameraSetting");

        return p;
    }

    public static int[] splitsSize(String strSize){
        int index = strSize.indexOf('x');
        int width = Integer.parseInt(strSize.substring(0, index));
        int height = Integer.parseInt(strSize.substring(index + 1));
        int[] size = {
                width, height
        };
        return size;
    }

    public static Intent getRemaingPictrue(Context context) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        int number  = getPictureNumber(context);
        intent.putExtra(HipParamters.REMAINING_PICTURE, number);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.REMAINING_PICTURE);
        CameraLog.e(TAG, " number =" + number + " ,command = " + HipParamters.REMAINING_PICTURE);
        return intent;
    }

    public static Intent getRemaingSpace(Context context) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        int number = getPictureNumber(context);
        intent.putExtra(HipParamters.REMAINING_PICTURE, number);
        int time = calculateVideoTime(context);
        intent.putExtra(HipParamters.REMAINING_VIDEO, time);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.REMAINING_PICTURE);
        CameraLog.e(TAG, " number =" + number + " ,time = " + time);
        return intent;
    }

    public static int getPictureNumber(Context context) {
        int number = 0;
        int size_opt = get4SwitchMode(context, HipParamters.PICTURE_SIZE);
        number = getPictureNumber(size_opt);
        return number;
    }

    private static int getPictureNumber(int opt) {
        long sizeSpace = Storage.getSpaceStorageSzie();
        return (int) (sizeSpace / getPicturePx(opt));
    }

    private static long getPicturePx(int opt) {
        long size = 0;
        switch (opt) {
            case HipParamters.OPT_0:
                size = 1249622;
                break;
            case HipParamters.OPT_1:
                size = 727354;
                break;
            case HipParamters.OPT_2:
                size = 625831;
                break;
            case HipParamters.OPT_3:
                size = 430028;
                break;
            case HipParamters.OPT_4:
                size = 322475;
                break;
            default:
                size = 1249622;
                break;
        }
        return size;
    }

    public static Intent getRemaingVideo(Context context) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);

        int time = calculateVideoTime(context);
        intent.putExtra(HipParamters.REMAINING_VIDEO, time);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.REMAINING_VIDEO);
        CameraLog.e(TAG, " time = " + time + " ,command = " + HipParamters.REMAINING_VIDEO);
        return intent;
    }

    public static Intent setOpenCamera(Context context) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.CAMERA_LOGIN);

        save4Setting(context, HipParamters.AUTO_START, HipParamters.OPT_1);
        CameraLog.e(TAG, " command = " + HipParamters.CAMERA_LOGIN);

        return intent;
    }

    public static Intent setAutoPreview(Context context, boolean flag) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.START_PREVIEW);

        save4Setting(context, HipParamters.AUTO_PREVIEW, flag ? HipParamters.OPT_1
                : HipParamters.OPT_0);
        CameraLog.e(TAG, " command = " + HipParamters.START_PREVIEW + " = " + flag);

        return intent;
    }

    public static Intent setCloseCamera(Context context) {
        Intent intent = new Intent(HipParamters.RESULT_ACTION);
        intent.putExtra(HipParamters.RESULT_KEY, HipParamters.RESULT_OK);
        intent.putExtra(HipParamters.RESULT_ERROR, HipParamters.NO_ERROR_CODE);
        intent.putExtra("command", HipParamters.CAMERA_LOGOUT);

        save4Setting(context, HipParamters.AUTO_START, HipParamters.OPT_0);
        CameraLog.e(TAG, " command = " + HipParamters.CAMERA_LOGOUT);

        return intent;
    }

    private static void updateMediaMemory(Context context) {

        int time = calculateVideoTime(context);
        save4SwitchMode(context, HipParamters.KEY_MEMVIO, time);
        int number  = getPictureNumber(context);
        save4SwitchMode(context, HipParamters.KEY_MEMPIC, number);
        CameraLog.e(TAG, "#updateMediaMemory  time =" + time + " ,number = " + number);
    }

    private static int calculateVideoTime(int opt) {
        int quality = CamcorderProfile.QUALITY_6480x1080;
        switch (opt) {
            case HipParamters.OPT_0:
                quality = CamcorderProfile.QUALITY_6480x1080;
                break;
            case HipParamters.OPT_1:
                quality = CamcorderProfile.QUALITY_3840x640;
                break;
            case HipParamters.OPT_2:
                quality = CamcorderProfile.QUALITY_2880x480;
                break;
            case HipParamters.OPT_3:
                quality = CamcorderProfile.QUALITY_1920x320;
                break;
            default:
                quality = CamcorderProfile.QUALITY_6480x1080;
                break;
        }
        CamcorderProfile mProfile = CamcorderProfile.get(quality);
        if (mProfile == null) {
            return 0;
        }
        long size = (long) (((160 * 1024) + mProfile.videoBitRate) * 1.1 / 8);
        long sizeTotalSpace = Storage.getSpaceStorageSzie()
                - HipParamters.MEDIARECORDER_SPACE_MAX_SIZE;

        CameraLog.e(TAG, "size_opt = " + opt + " ,quality = " + quality
                + " , mProfile.videoBitRate = "
                + mProfile.videoBitRate + " , size = " + size);
        if (sizeTotalSpace <=0) {
            return 0;
        }
        return  (int) (sizeTotalSpace / size) +1;
    }

    public static int calculateVideoTime(Context context) {
        int size_opt = get4SwitchMode(context, HipParamters.VIDEO_SIZE);
        return calculateVideoTime(size_opt);
    }

    public static Intent reducePictureNumberAction(Context context) {
        Intent intent = new Intent(HipParamters.HIP_DRAGONFLY_CAMERA_ACTION_MEMPIC);
        int number = reducePictureNumber(context);
        intent.putExtra(HipParamters.KEY_MEMPIC, number);
        intent.putExtra("command", HipParamters.KEY_MEMPIC);
        CameraLog.e(TAG, "#reducePictureNumberAction  " + " , number = " + number);
        return intent;
    }

    private static int reducePictureNumber(Context context) {
        int number = get4SwitchMode(context, HipParamters.KEY_MEMPIC);
        save4SwitchMode(context, HipParamters.KEY_MEMPIC, number > 0 ? number - 1 : 1);
        number = number > 0 ? number - 1 : 0;
        return number;
    }
}
