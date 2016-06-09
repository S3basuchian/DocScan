LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= C:\cvl\dmrz\code\opencv_sdk\OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := opencv-jni.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := opencv-jni
LOCAL_CFLAGS += -std=c++11
LOCAL_LDFLAGS += -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
