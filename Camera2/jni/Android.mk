LOCAL_PATH:= $(call my-dir)
####################### surveillance #######################
include $(CLEAR_VARS)

LOCAL_SHARED_LIBRARIES += libsurveillance
LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := libjni_detect

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        DetectJniNative.cpp

LOCAL_C_INCLUDES := \
        $(JNI_H_INCLUDE) \
        $(LOCAL_PATH)/include

include $(BUILD_SHARED_LIBRARY)
####################### surveillance #######################

