// app/src/main/cpp/checksum-utils.cpp (исправить)
#include <jni.h>
#include <cstdint>
#include <arpa/inet.h>

// Вспомогательная функция для расчёта checksum
static uint16_t calculate_checksum(const uint8_t* data, size_t len) {
    uint32_t sum = 0;
    for (size_t i = 0; i < len; i += 2) {
        uint16_t word;
        if (i + 1 < len) {
            word = (data[i] << 8) | data[i + 1];
        } else {
            word = data[i] << 8;
        }
        sum += word;
    }
    while (sum >> 16) {
        sum = (sum & 0xFFFF) + (sum >> 16);
    }
    return ~sum;
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
    jbyte* data = env->GetByteArrayElements(packet, nullptr);
    if (!data) return;
    
    // Псевдо-заголовок для TCP checksum (RFC 793)
    // Source IP (4 bytes) + Dest IP (4 bytes) + Zero (1) + Protocol (1) + TCP Length (2)
    
    const uint8_t* ipHeader = reinterpret_cast<uint8_t*>(data);
    const uint8_t* tcpHeader = ipHeader + ipHeaderLen;
    
    // Извлекаем IP-адреса из IPv4 заголовка
    uint32_t srcIp = (ipHeader[12] << 24) | (ipHeader[13] << 16) | 
                     (ipHeader[14] << 8) | ipHeader[15];
    uint32_t dstIp = (ipHeader[16] << 24) | (ipHeader[17] << 16) | 
                     (ipHeader[18] << 8) | ipHeader[19];
    
    uint16_t tcpLength = static_cast<uint16_t>(tcpHeaderLen + payloadLen);
    
    // Сбрасываем поле checksum перед пересчётом
    int tcpChecksumOffset = ipHeaderLen + 16;
    data[tcpChecksumOffset] = 0;
    data[tcpChecksumOffset + 1] = 0;
    
    // Считаем checksum: pseudo-header + TCP header + payload
    uint32_t sum = 0;
    
    // Pseudo-header part 1: source IP
    sum += (srcIp >> 16) & 0xFFFF;
    sum += srcIp & 0xFFFF;
    
    // Pseudo-header part 2: dest IP  
    sum += (dstIp >> 16) & 0xFFFF;
    sum += dstIp & 0xFFFF;
    
    // Pseudo-header part 3: zero + protocol + length
    sum += 0x0006; // protocol = 6 (TCP)
    sum += tcpLength;
    
    // TCP header + payload
    const uint8_t* tcpSegment = tcpHeader;
    size_t tcpSegmentLen = tcpHeaderLen + payloadLen;
    
    for (size_t i = 0; i < tcpSegmentLen; i += 2) {
        uint16_t word;
        if (i + 1 < tcpSegmentLen) {
            word = (tcpSegment[i] << 8) | (tcpSegment[i + 1] & 0xFF);
        } else {
            word = tcpSegment[i] << 8;
        }
        sum += word;
    }
    
    // Fold 32-bit sum to 16 bits
    while (sum >> 16) {
        sum = (sum & 0xFFFF) + (sum >> 16);
    }
    
    uint16_t checksum = ~static_cast<uint16_t>(sum);
    
    // Записываем результат
    data[tcpChecksumOffset] = (checksum >> 8) & 0xFF;
    data[tcpChecksumOffset + 1] = checksum & 0xFF;
    
    env->ReleaseByteArrayElements(packet, data, 0);
}