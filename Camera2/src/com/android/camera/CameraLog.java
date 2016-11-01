/**
 * @file CameraLog.java
 *
 * This file contains the CameraLog.
 *
 */
package com.android.camera;

import android.util.Log;

/**
 * Wrapper class for logging.
 */
public final class CameraLog {

    /**
     * Logging level.
     */
    private static final int LOCAL_LOG_LEVEL = Log.VERBOSE;

    public static final boolean DEBUG = true;

    /** Debug options. */
    public static final boolean DEBUG_LOG_WITH_TIME = false;
    public static final boolean DEBUG_LOG_ALL_I = false;


    /**
     * logging flag for performance measurement
     */
    public static final boolean isTimeDebug = false;

    /**
     * Copy of Logging levels.
     */
    public static final int LOG_ASSERT = Log.ASSERT;
    public static final int LOG_VERBOSE = Log.VERBOSE;
    public static final int LOG_DEBUG = Log.DEBUG;
    public static final int LOG_INFO = Log.INFO;
    public static final int LOG_WARN = Log.WARN;
    public static final int LOG_ERROR = Log.ERROR;

    /**
     * Camera application common tag string for logging.
     */
    private static final String TAG = "CameraApp";

    /**
     * Constructor.
     */
    private CameraLog() {
    }

    private static String time() {
        if (DEBUG_LOG_WITH_TIME) {
            long now = System.currentTimeMillis();
            return ((Long)now ).toString() + " ";
        } else {
            return "";
        }
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int v(String tag, String msg) {
        if (DEBUG) {
            if (LOCAL_LOG_LEVEL <= Log.VERBOSE) {
                return Log.v(TAG, time() + tag + ":" + msg);
            }
        }
        return 0;
    }

    /**
     * Send a {@link Log#VERBOSE} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            if (LOCAL_LOG_LEVEL <= Log.VERBOSE) {
                return Log.v(TAG, time() + tag + ":" + msg, tr);
            }
        }
        return 0;
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int d(String tag, String msg) {
        if (DEBUG) {
            if (LOCAL_LOG_LEVEL <= Log.DEBUG) {
                if (DEBUG_LOG_ALL_I) {
                    return Log.i(TAG, time() + tag + ":" + msg);
                } else {
                    return Log.d(TAG, time() + tag + ":" + msg);
                }
            }
        }
        return 0;
    }

    /**
     * Send a {@link Log#DEBUG} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            if (LOCAL_LOG_LEVEL <= Log.DEBUG) {
                return Log.d(TAG, time() + tag + ":" + msg, tr);
            }
        }
        return 0;
    }

    /**
     * Send an {@link Log#INFO} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int i(String tag, String msg) {
        if (DEBUG) {
            return Log.i(TAG, time() + tag + ":" + msg);
        }
        return 0;
    }

    /**
     * Send a {@link Log#INFO} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            return Log.i(TAG, time() + tag + ":" + msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int w(String tag, String msg) {
        if (DEBUG) {
            return Log.w(TAG, time() + tag + ":" + msg);
        }
        return 0;
    }

    /**
     * Send a {@link Log#WARN} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            return Log.w(TAG, time() + tag + ":" + msg, tr);
        }
        return 0;
    }



    /**
     * Send an {@link Log#ERROR} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int e(String tag, String msg) {
        return Log.e(TAG, time() + tag + ":" + msg);
    }

    /**
     * Send a {@link Log#ERROR} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(TAG, time() + tag + ":" + msg, tr);
    }

    public static void dumpStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        Log.d(TAG, "## dump stack trace ...");
        for(int i=1; i< trace.length; i++) {
            Log.d(TAG, "trace:"+trace[i].getClassName()+"#"+trace[i].getMethodName());
        }
    }
}
