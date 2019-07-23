#include <jni.h>
#include <string>
#include "lt602_sensor.h"
#include "Tlog.h"

lt602::Sensor sensor;

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
    TLOGV(" %d %d %d %d %d %d %d %d %d %d",lt602::RC_FAILED,lt602::RC_OK,lt602::RC_INVALID_ARGUMENT,lt602::RC_NOT_CONNECTED,
            lt602::RC_ALREADY_CONNECTED,lt602::RC_IN_PROGRESS, lt602::RC_COMMUNICATION_FAILED,
          lt602::RC_INVALID_RESPONSE,lt602::RC_CONNECTION_CLOSED,lt602::RC_ADDRESS_BIND_FAILED);

    return 1;
}
extern "C"
JNIEXPORT void JNICALL
Java_cn_com_startai_radarwall_MainActivity_Uninitialize(JNIEnv *env, jobject instance) {

    // TODO
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_Uninitialize ");
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
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_connect__Ljava_lang_String_2ILjava_lang_String_2I %s %d %s %d", ip, port,remoteIp,remotePort);
    // TODO
    lt602::ResultCode rc = sensor.connect(ip,port,remoteIp,remotePort);

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

    jintArray  array = env->NewIntArray(length);
    env->SetIntArrayRegion(array, 0, length, reinterpret_cast<const jint *>(data));
    return array;
}
extern "C"
JNIEXPORT jint JNICALL
Java_cn_com_startai_radarwall_MainActivity_acquirePositionDataArray(JNIEnv *env, jobject instance,
                                                                    jcharArray chars_,
                                                                    jint length) {
    jchar *data = env->GetCharArrayElements(chars_, NULL);
    TLOGD(" Java_cn_com_startai_radarwall_MainActivity_acquirePositionDataArray  %d", length);
    // TODO
    lt602::ResultCode rc;
    int frame = 0;
    rc = sensor.acquireDistanceData(data, &length);
    if (rc == lt602::RC_OK) {
        TLOGE("RC_OK %d: ", frame);
        for (int index = 0; index < length; index++) {
            TLOGD(" index %d = %d", index, data[index]);
        }
        TLOGD("\n");
    } else {
        TLOGE("%d: data acquired failed\n", frame);
    }

    env->ReleaseCharArrayElements(chars_, data, JNI_COMMIT);

    return rc;
}