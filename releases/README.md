# Daus Todo App - Release APKs

This directory contains the compiled APK files for the Daus Todo application.

## Available APKs

### daus-todo-v1.0-debug.apk
- **Type**: Debug build
- **Size**: ~18.9 MB
- **Purpose**: For testing and development
- **Features**: 
  - Debug logging enabled
  - Can be installed alongside release version
  - Suitable for development and testing

### daus-todo-v1.0-release-unsigned.apk
- **Type**: Release build (unsigned)
- **Size**: ~13.5 MB
- **Purpose**: For distribution (requires signing)
- **Features**:
  - Optimized for production
  - Smaller file size due to optimizations
  - Requires signing with a keystore before distribution

## Installation

### Debug APK
```bash
adb install daus-todo-v1.0-debug.apk
```

### Release APK (after signing)
```bash
# First sign the APK with your keystore
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore your-keystore.jks daus-todo-v1.0-release-unsigned.apk alias_name

# Then install
adb install daus-todo-v1.0-release-signed.apk
```

## App Information

- **Package Name**: com.daustodo.app
- **Version**: 1.0
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Build Date**: July 19, 2025

## Features

- ✅ Todo task management
- ✅ Pomodoro timer integration
- ✅ Material 3 design
- ✅ Dark/light theme support
- ✅ Notifications and background services
- ✅ Modern date/time APIs
- ✅ KSP annotation processing

## Build Information

- **Gradle Version**: 8.6
- **Android Gradle Plugin**: 8.4.0
- **Java Version**: 21
- **Kotlin Version**: 1.9.20
- **Build Status**: ✅ SUCCESS