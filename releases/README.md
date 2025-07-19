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

### daus-todo-v1.0-signed.apk
- **Type**: Release build (signed)
- **Size**: ~13.1 MB
- **Purpose**: Ready for distribution and installation
- **Features**:
  - Optimized for production
  - Properly signed with release keystore
  - Ready to install on Android devices
  - Can be uploaded to Google Play Store

### daus-todo-v1.1-optimized.apk ⭐
- **Type**: Release build (optimized)
- **Size**: ~3.3 MB
- **Purpose**: Latest version with performance improvements
- **Features**:
  - All previous features plus:
  - **Performance Optimizations**: Lazy loading, caching, pagination
  - **Pull-to-Refresh**: Smooth refresh experience
  - **Skeleton Loading**: Better perceived performance
  - **Batch Operations**: Multi-select and bulk actions
  - **Performance Monitoring**: Built-in performance tracking
  - **Database Optimizations**: Faster queries and operations
  - **Background Processing**: Non-blocking operations

### daus-todo-v1.1-signed.apk ⭐
- **Type**: Release build (signed & optimized)
- **Size**: ~3.3 MB
- **Purpose**: Latest signed version ready for distribution
- **Features**:
  - All v1.1 features plus:
  - **Properly Signed**: APK Signature Scheme v2
  - **Ready for Distribution**: Can be uploaded to Google Play Store
  - **Production Ready**: Optimized and signed for release
  - **Performance Optimizations**: All v1.1 performance improvements

## Installation

### Debug APK
```bash
adb install daus-todo-v1.0-debug.apk
```

### Release APK (signed)
```bash
# Install the latest signed APK directly
adb install daus-todo-v1.1-signed.apk
```

### Release APK (unsigned - requires signing)
```bash
# First sign the APK with your keystore
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore your-keystore.jks daus-todo-v1.0-release-unsigned.apk alias_name

# Then install
adb install daus-todo-v1.0-release-signed.apk
```

### Manual Installation
1. Download `daus-todo-v1.1-signed.apk` (Latest)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK file
4. Follow the installation prompts
5. Launch the app and start managing your tasks!

**Note**: The v1.1 signed APK is the latest version with all performance optimizations and is ready for production use.

## App Information

- **Package Name**: com.daustodo.app
- **Version**: 1.1 (Latest)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Build Date**: July 19, 2025
- **Signature**: APK Signature Scheme v2

## Features

### Core Features
- ✅ Todo task management
- ✅ Pomodoro timer integration
- ✅ Material 3 design
- ✅ Dark/light theme support
- ✅ Notifications and background services
- ✅ Modern date/time APIs
- ✅ KSP annotation processing

### Performance Features (v1.1)
- ✅ **Lazy Loading**: Efficient list rendering with pagination
- ✅ **Pull-to-Refresh**: Smooth refresh experience
- ✅ **Skeleton Loading**: Better perceived performance during loading
- ✅ **Caching System**: Smart caching for faster data access
- ✅ **Batch Operations**: Multi-select and bulk actions
- ✅ **Performance Monitoring**: Built-in performance tracking
- ✅ **Database Optimizations**: Faster queries and operations
- ✅ **Background Processing**: Non-blocking operations
- ✅ **Search Debouncing**: Reduced database queries
- ✅ **Memory Management**: Automatic cache cleanup

## Build Information

- **Gradle Version**: 8.6
- **Android Gradle Plugin**: 8.4.0
- **Java Version**: 21
- **Kotlin Version**: 1.9.20
- **Build Status**: ✅ SUCCESS