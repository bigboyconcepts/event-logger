LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := el-jni
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := iab_lkey.cpp
LOCAL_LDLIBS    := -llog 

include $(BUILD_SHARED_LIBRARY)
