#include <android/log.h>
#include <cstdarg>
#include <cstdio>

#ifndef LOG_HPP
#define LOG_HPP
void LOG_D(const char* tag, const char* message, ...);
void LOG_I(const char* tag, const char* message);
void LOG_W(const char* tag, const char* message);
void LOG_E(const char* tag, const char* message);
void LOG_F(const char* tag, const char* message);
#endif  // LOG_HPP
