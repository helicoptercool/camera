#ifndef SAK_APP_MOTION_DETECTION_H__
#define SAK_APP_MOTION_DETECTION_H__

#include "tcomdef.h"
#include "tsoffscreen.h"

#ifndef SAK_STATIC  // Dynamic Library
#   if (defined(_WIN32) || defined(_WIN64)) && !defined(__GNUC__)
#       ifdef SAK_DLL
#           define SAK_EXPORTS __declspec(dllexport)
#       else
#           define SAK_EXPORTS __declspec(dllimport)
#       endif
#   elif (defined(linux) || defined(__linux) || defined(__ANDROID__)) && defined(__GNUC__)
#      define SAK_EXPORTS __attribute__((visibility("default")))
#   else
#      define SAK_EXPORTS 
#   endif
#else  // Static Library
#   define SAK_EXPORTS 
#endif
    
#ifndef __cplusplus
#   define SAK_DEFAULT(val)
#else
#   define SAK_DEFAULT(val) = val
extern "C" {
#endif

/**
 * The module's status.
 */
typedef enum {
    TS_MD_STATUS_UPDATING,      /**< The module is initializing the internal scene model
                                 * and cann't be used. */
    TS_MD_STATUS_PROCESSING     /**< The module is working.  */
} TS_MD_STATUS;

/** 
 * Create the module handle.
 * 
 * @return Zero if failed.
 */
SAK_EXPORTS  THandle tsMotionDetection_create();

/** 
 * Release the module.
 * 
 * @param h The module handle.
 */
SAK_EXPORTS  void tsMotionDetection_release(THandle h);

/** 
 * Detect the motion objects.
 * 
 * @param h      The module handle.    
 * @param frame  The frame image.
 * @param alarmLevel The alarm level belonging to [0, 1].
 * 
 * @return If TS_MD_STATUS_UPDATING returned, the value of _alarmLevel_ is invalid.
 */
SAK_EXPORTS  TS_MD_STATUS tsMotionDetection_detect(THandle h, const TSOFFSCREEN* frame, float* alarmLevel);

/** 
 * Set the module's properties, which are given by the form of Key-Value.
 * NOTE: The KEY's name is case-sensitive.
 *
 * |-------------+------------+-------------|
 * | Key         | Value Type | Value Range |
 * |-------------+------------+-------------|
 * | Sensitivity | float      | [0,1]       |
 * |-------------+------------+-------------|
 * 
 * @param h     The module handle.
 * @param key   Key.
 * @param value Value.
 */
SAK_EXPORTS  void tsMotionDetection_setProperty(THandle h, const char* key, const void* value);

/** 
 * Get the module's properties.
 * 
 * @param h     The module handle.
 * @param key 
 * @param value OUT
 */
SAK_EXPORTS  void tsMotionDetection_getProperty(THandle h, const char* key, void* value);

#ifdef __cplusplus
}
#endif

#endif//SAK_APP_MOTION_DETECTION_H__
