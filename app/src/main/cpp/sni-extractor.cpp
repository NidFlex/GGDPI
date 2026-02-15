#include <jni.h>
#include <cstring>
#include <android/log.h>

#define LOG_TAG "GGDPI-Native"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ggdpi_app_dpibypass_native_NativeDpiUtils_extractSni(
    JNIEnv* env,
    jobject thiz,
    jbyteArray payload,
    jint offset,
    jint length
) {
    jbyte* data = env->GetByteArrayElements(payload, nullptr);
    
    if (length < 43 || data[offset] != 0x16) {
        env->ReleaseByteArrayElements(payload, data, JNI_ABORT);
        return nullptr;
    }
    
    int pos = offset + 9;
    pos += 34;
    
    int sessionIdLen = data[pos++] & 0xFF;
    pos += sessionIdLen;
    
    if (pos + 2 > offset + length) {
        env->ReleaseByteArrayElements(payload, data, JNI_ABORT);
        return nullptr;
    }
    int cipherSuitesLen = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
    pos += 2 + cipherSuitesLen;
    
    if (pos >= offset + length) {
        env->ReleaseByteArrayElements(payload, data, JNI_ABORT);
        return nullptr;
    }
    int compressionLen = data[pos++] & 0xFF;
    pos += compressionLen;
    
    if (pos + 2 > offset + length) {
        env->ReleaseByteArrayElements(payload, data, JNI_ABORT);
        return nullptr;
    }
    int extensionsLen = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
    pos += 2;
    
    int endPos = pos + extensionsLen;
    
    while (pos < endPos) {
        if (pos + 4 > offset + length) break;
        
        int extType = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
        int extLen = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
        
        if (extType == 0x0000) {
            int sniPos = pos + 4;
            sniPos += 2;
            sniPos += 1;
            
            int nameLen = ((data[sniPos] & 0xFF) << 8) | (data[sniPos + 1] & 0xFF);
            sniPos += 2;
            
            char* sni = new char[nameLen + 1];
            memcpy(sni, &data[sniPos], nameLen);
            sni[nameLen] = '\0';
            
            jstring result = env->NewStringUTF(sni);
            delete[] sni;
            
            env->ReleaseByteArrayElements(payload, data, JNI_ABORT);
            return result;
        }
        
        pos += 4 + extLen;
    }
    
    env->ReleaseByteArrayElements(payload, data, JNI_ABORT);
    return nullptr;
}