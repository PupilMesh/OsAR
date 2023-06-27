#include"log.hpp"

void LOG_D(const char* tag, const char* message, ...) {
    va_list args;
    va_start(args, message);
    char formatted_message[512];
    vsprintf(formatted_message, message, args);
    va_end(args);
    __android_log_write(ANDROID_LOG_DEBUG, tag, formatted_message);
}

void LOG_I(const char* tag, const char* message) {
    __android_log_write(ANDROID_LOG_INFO, tag, message);
}
void LOG_W(const char* tag, const char* message) {
    __android_log_write(ANDROID_LOG_WARN, tag, message);
}
void LOG_E(const char* tag, const char* message) {
    __android_log_write(ANDROID_LOG_ERROR, tag, message);
}
void LOG_F(const char* tag, const char* message) {
    __android_log_write(ANDROID_LOG_FATAL, tag, message);
}
