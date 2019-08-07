#include <jni.h>
#include <string>
#include "lt602_sensor.h"
#include "Tlog.h"

lt602::Sensor sensor;
jobject globalObj = nullptr;
JavaVM *mVm = nullptr;
jmethodID callBackId = nullptr;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    TLOGE(" JNI_OnLoad ");

    mVm = vm;

    JNIEnv *evn;
    jint je = mVm->GetEnv((void **) &evn, JNI_VERSION_1_4);

    TLOGI(" GetEnv =  %d ", je);

    return JNI_VERSION_1_4;
}


void GetOneFrame(lt602::ResultCode rc, lt602::uint16 *data, int size, JNIEnv *evn) {

//	TLOGV( " getOneFrame size : %d ", size);

    jcharArray array = evn->NewCharArray(size);
    evn->SetCharArrayRegion(array, 0, size, data);
    evn->CallVoidMethod(globalObj, callBackId, array, size, rc);
    evn->DeleteLocalRef(array);

//    jbyteArray array = evn->NewByteArray( size);
//    evn->SetByteArrayRegion( array, 0, size, data);
//    evn->CallVoidMethod( globalObj, callBackId, array, size);
//    evn->DeleteLocalRef(evn, array);

}


extern "C" JNIEXPORT jstring JNICALL
Java_cn_com_startai_radarwall_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_Initialize(JNIEnv *env, jobject instance) {

    // TODO
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_Initialize ");
    TLOGV(" %d %d %d %d %d %d %d %d %d %d", lt602::RC_FAILED, lt602::RC_OK,
          lt602::RC_INVALID_ARGUMENT, lt602::RC_NOT_CONNECTED,
          lt602::RC_ALREADY_CONNECTED, lt602::RC_IN_PROGRESS, lt602::RC_COMMUNICATION_FAILED,
          lt602::RC_INVALID_RESPONSE, lt602::RC_CONNECTION_CLOSED, lt602::RC_ADDRESS_BIND_FAILED);

    if (nullptr == globalObj) {
        globalObj = env->NewGlobalRef(instance);
    }

    if (nullptr == callBackId) {
        jclass cls = env->GetObjectClass(instance);
        callBackId = env->GetMethodID(cls, "callBack", "([CII)V");
        TLOGD("GetMethodID success !");
    }

    return lt602::RC_OK;
}
extern "C"
JNIEXPORT void JNICALL
Java_cn_com_startai_radarwall_MainActivity_Uninitialize(JNIEnv *env, jobject instance) {

    // TODO
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_Uninitialize ");
    if (nullptr != globalObj) {
        env->DeleteGlobalRef(globalObj);
    }
    globalObj = nullptr;
}
extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_connect(JNIEnv *env, jobject instance, jstring ip_,
                                                   jint port) {
    const char *ip = env->GetStringUTFChars(ip_, nullptr);

    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_connect %s:%d", ip, port);

    lt602::ResultCode mResultCode = sensor.connect(ip, port);
    TLOGD(" sensor.connect %d", mResultCode);
    // TODO

    env->ReleaseStringUTFChars(ip_, ip);
    return mResultCode;
}extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_connect__Ljava_lang_String_2ILjava_lang_String_2I(
        JNIEnv *env, jobject instance, jstring ip_, jint port, jstring remoteIp_, jint remotePort) {
    const char *ip = env->GetStringUTFChars(ip_, 0);
    const char *remoteIp = env->GetStringUTFChars(remoteIp_, 0);
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_connect__Ljava_lang_String_2ILjava_lang_String_2I %s %d %s %d",
          ip, port, remoteIp, remotePort);
    // TODO
    lt602::ResultCode rc = sensor.connect(ip, port, remoteIp, remotePort);

    env->ReleaseStringUTFChars(ip_, ip);
    env->ReleaseStringUTFChars(remoteIp_, remoteIp);
    return rc;
}
extern "C"
JNIEXPORT void JNICALL
Java_cn_com_startai_radarwall_MainActivity_disconnect(JNIEnv *env, jobject instance) {
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_disconnect  ");
    // TODO
    sensor.disconnect();
}extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_setIntegrationTime(JNIEnv *env, jobject instance,
                                                              jint time) {

    // TODO
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_setIntegrationTime %d", time);
    return sensor.setIntegrationTime(time);

}

extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_setTwiceIntegrationTime(JNIEnv *env, jobject instance,
                                                                   jint time1, jint time2) {
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_setTwiceIntegrationTime %d ,%d", time1,
          time2);
    // TODO
    return sensor.setTwiceIntegrationTime(time1, time2);
}

extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_setLD(JNIEnv *env, jobject instance, jint gear) {
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_setLD  %d", gear);
    // TODO
    return sensor.setLD(gear);
}extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_com_startai_radarwall_MainActivity_isConnected(JNIEnv *env, jobject instance) {

    // TODO
    bool con = sensor.isConnected();
    return static_cast<jboolean>(con);
}extern "C"
JNIEXPORT jintArray JNICALL
Java_cn_com_startai_radarwall_MainActivity_acquirePositionData(JNIEnv *env, jobject instance) {

    // TODO
    int length = 320;
    lt602::uint16 data[length];
    lt602::ResultCode rc = sensor.acquireDistanceData(data, &length);
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_acquirePositionData  %d", rc);
    int frame = 0;
    if (rc == lt602::RC_OK) {
        TLOGE("RC_OK %d: ", frame);
        for (int index = 0; index < length; index++) {
            TLOGD(" index %d = %d", index, data[index]);
        }
        TLOGD("\n");
    } else {
        TLOGE("%d: data acquired failed\n", frame);
    }

    jintArray array = env->NewIntArray(length);
//    jint *buf = static_cast<jint *>(malloc(sizeof(jint) * 320));
    jboolean t = 1;
    jint *buf = env->GetIntArrayElements(array, &t);
    for (int i = 0; i < 320; i++) {
        buf[i] = data[i];
    }
//    env->SetIntArrayRegion(array, 0, length, buf);
//    free(buf);
    return array;
}
extern "C" JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_acquirePositionDataArray(JNIEnv *env, jobject instance,
                                                                    jcharArray chars_,
                                                                    jint length) {
    jchar *data = env->GetCharArrayElements(chars_, nullptr);
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_acquirePositionDataArray  %d", length);
    // TODO
    lt602::ResultCode rc;
    rc = sensor.acquireDistanceData(data, &length);

//    int frame = 0;
//    if (rc == lt602::RC_OK) {
//        TLOGE("RC_OK %d: ", frame);
//        for (int index = 0; index < length; index++) {
//            TLOGD(" index %d = %d", index, data[index]);
//        }
//        TLOGD("\n");
//    } else {
//        TLOGE("%d: data acquired failed\n", frame);
//    }

    env->ReleaseCharArrayElements(chars_, data, JNI_COMMIT);

    return rc;
}


int flag = 0;

void *always(void *arg) {

    static JNIEnv *evn;
    jint j = mVm->AttachCurrentThread(&evn, nullptr);
    if (j < 0) {
        TLOGE(" AttachCurrentThread error");
        return nullptr;
    }

    lt602::uint16 data[320];
    int length = 320;
    lt602::ResultCode rc;
    const int hz = 20;
    useconds_t sleep = 1000 / hz;

    int frame = 0;
    while (flag) {
        rc = sensor.acquireDistanceData(data, &length);
        frame++;
        GetOneFrame(rc, data, 320, evn);
        usleep(sleep);
        TLOGD(" data acquired result %d :%d", rc, frame);
    }
    TLOGD(" DetachCurrentThread ");
    mVm->DetachCurrentThread();

//    pthread_exit(nullptr);
    TLOGD(" ParsePkgThread exit ");

    return nullptr;
}

pthread_t mMeasureThread;

extern "C" JNIEXPORT void JNICALL
Java_cn_com_startai_radarwall_MainActivity_alwaysAcquirePositionData(JNIEnv *env,
                                                                     jobject instance) {
    // TODO

    if (flag == 1) {
        return;
    }

    flag = 1;
    int mMeasureThreadHandle = pthread_create(&mMeasureThread, nullptr, always, nullptr);
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_alwaysAcquirePositionData  %d",
          mMeasureThreadHandle);

}extern "C"
JNIEXPORT void JNICALL
Java_cn_com_startai_radarwall_MainActivity_stopAlwaysAcquirePositionData(JNIEnv *env,
                                                                         jobject instance) {

    // TODO
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_stopAlwaysAcquirePositionData ");
    flag = 0;
}