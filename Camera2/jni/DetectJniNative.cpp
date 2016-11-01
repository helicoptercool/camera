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

#include <stdlib.h>
#include <jni.h>
#include "include/motion-detection.h"

#ifdef __cplusplus
extern "C" {
#endif

static THandle sMD = NULL;

/*
 * Class:     com_hip_dragonfly_detector_MotionDetector
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_surveillance_detector_MotionDetector_init(JNIEnv* env, jobject thiz)
{
    if ( sMD != NULL ) {
        tsMotionDetection_release(sMD);
        sMD = NULL;
    }
    sMD = tsMotionDetection_create();
}

/*
 * Class:     com_hip_dragonfly_detector_MotionDetector
 * Method:    release
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_surveillance_detector_MotionDetector_release(JNIEnv* env, jobject thiz)
{
    tsMotionDetection_release(sMD);
}

/*
 * Class:     com_hip_dragonfly_detector_MotionDetector
 * Method:    setSensitivity
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_com_android_surveillance_detector_MotionDetector_setSensitivity(JNIEnv* env, jobject thiz, jdouble sensitivity)
{
    tsMotionDetection_setProperty(sMD, "Sensitivity", &sensitivity);
}

/*
 * Class:     com_hip_dragonfly_detector_MotionDetector
 * Method:    detect
 * Signature: (II[B)F
 */
JNIEXPORT jfloat JNICALL Java_com_android_surveillance_detector_MotionDetector_detect(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray photo_data)
{
    float alarmLevel = 0.0;
    jbyte *pixels = env->GetByteArrayElements(photo_data, 0);

    TSOFFSCREEN frame;

    frame.u32PixelArrayFormat = TS_PAF_GRAY;//TS_PAF_NV21;
    frame.i32Width = width;
    frame.i32Height = height;
    frame.ppu8Plane[0] = (TUInt8*)pixels;
    frame.pi32Pitch[0] = width;

    int status = tsMotionDetection_detect(sMD, &frame, &alarmLevel);

    env->ReleaseByteArrayElements(photo_data, pixels, 0 );

    if ( status == TS_MD_STATUS_UPDATING ) {
        alarmLevel = 0.0;
    }
    return alarmLevel;
}

#ifdef __cplusplus
}
#endif
