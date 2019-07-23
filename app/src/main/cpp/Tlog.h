//
// Created by Administrator on 2019/7/17.
//

#ifndef LT602DEMO_TLOG_H
#define LT602DEMO_TLOG_H

#endif //LT602DEMO_TLOG_H

#include <jni.h>
#include <android/log.h>
#define TAG "radar"

#define DEBUG 1

#if DEBUG

#define TLOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__);
#define TLOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__);
#define TLOGW(...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__);
#define TLOGI(...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__);
#define TLOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__);

#else

#define TLOGV(...)		((void *)0)
#define TLOGD(...)		((void *)0)
#define TLOGW(...)		((void *)0)
#define TLOGI(...)		((void *)0)
#define TLOGE(...)		((void *)0)

#endif
