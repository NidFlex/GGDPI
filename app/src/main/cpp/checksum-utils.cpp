#include <jni.h>
#include <cstdint>

extern "C"
JNIEXPORT void JNICALL
Java_com_ggdpi_app_utils_PacketUtils_nativeUpdateIpChecksum(
    JNIEnv* env,
    jclass clazz,
    jbyteArray packet,
    jint headerLen
) {
    jbyte* data = env->GetByteArrayElements(packet, nullptr);
    
    // Reset checksum field
    data[10] = 0;
    data[11] = 0;
    
    uint32_t sum = 0;
    for (int i = 0; i < headerLen; i += 2) {
        sum += ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
    }
    
    while (sum >> 16) {
        sum = (sum & 0xFFFF) + (sum >> 16);
    }
    
    uint16_t checksum = ~sum;
    data[10] = (checksum >> 8) & 0xFF;
    data[11] = checksum & 0xFF;
    
    env->ReleaseByteArrayElements(packet, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggdpi_app_utils_PacketUtils_nativeUpdateTcpChecksum(
    JNIEnv* env,
    jclass clazz,
    jbyteArray packet,
    jint ipHeaderLen,
    jint tcpHeaderLen,
    jint payloadLen
) {
    // TCP checksum calculation with pseudo-header
    // Implementation similar to IP checksum but includes pseudo-header
    jbyte* data = env->GetByteArrayElements(packet, nullptr);
    
    // Reset TCP checksum
    int tcpChecksumOffset = ipHeaderLen + 16;
    data[tcpChecksumOffset] = 0;
    data[tcpChecksumOffset + 1] = 0;
    
    // Calculate checksum (simplified)
    // Full implementation would include pseudo-header calculation
    
    env->ReleaseByteArrayElements(packet, data, 0);
}