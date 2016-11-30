#include <jni.h>
#include <string>
extern "C"
{
    const std::string iab_lkey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnOUPBXzV9oCWKyyRV+YxdUlqhZnwJintBrl9hWjTILnwlD0ZGbHShavGBxLNmnS2iwGxgR7Zj1CPGhbVD5YNK4m6ExZbxGmB/yFIkY0TkeRVvUUccX+oQ8XThou2wBLEdIUJm6YLnpVrKjWQCosKGwMgLR0GuutXCtibwOA7uzem939ogulfT7z0Y9Epfsf9jhuAxZO4HHalRVRD4Cx8y3WJNmPAgqYHYe9iZiSkkhPAjKibJ3XkMBPSFgSGlMIKxhNc6dYojs2IQFj8SYZXOU1Z5ia1kEMiRhTBDmyoxuAtgIr7g8wcwgErsk61E/iYQnhBjBG9K/f33d+Vqk6WwwIDAQAB";

    jstring Java_rs_pedjaapps_eventlogger_utility_Utility_getIABLKey(JNIEnv * env, jobject thiz)
    {
        return env->NewStringUTF((const char* )iab_lkey.c_str());
    }
}


