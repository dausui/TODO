# APK Signing Guide - Daus Todo App

## Overview

This guide explains the APK signing process used for the Daus Todo application and how to manage the signing configuration.

## Signing Configuration

### Keystore Details
- **Keystore File**: `app/daus-todo.keystore`
- **Key Alias**: `daus-todo`
- **Validity**: 10,000 days (~27 years)
- **Algorithm**: RSA 2048-bit
- **Certificate**: Self-signed

### Keystore Properties
The signing configuration is stored in `keystore.properties`:
```
storeFile=daus-todo.keystore
storePassword=daus123
keyAlias=daus-todo
keyPassword=daus123
```

## Build Configuration

The signing is configured in `app/build.gradle.kts`:

```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = keystoreProperties["storeFile"]?.let { file(it) }
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## Building Signed APK

### Prerequisites
1. Android SDK installed and configured
2. Keystore file in `app/daus-todo.keystore`
3. `keystore.properties` file in project root

### Build Command
```bash
./gradlew assembleRelease
```

### Output
The signed APK will be generated at:
```
app/build/outputs/apk/release/app-release.apk
```

## Available APK Files

### 1. Debug APK
- **File**: `releases/daus-todo-v1.0-debug.apk`
- **Purpose**: Development and testing
- **Features**: Debug logging, can be installed alongside release

### 2. Unsigned Release APK
- **File**: `releases/daus-todo-v1.0-release-unsigned.apk`
- **Purpose**: Manual signing or custom distribution
- **Features**: Optimized but requires manual signing

### 3. Signed Release APK ⭐
- **File**: `releases/daus-todo-v1.0-signed.apk`
- **Purpose**: Production distribution
- **Features**: Ready for Google Play Store submission

## Security Considerations

### Keystore Protection
- **Backup**: Always backup your keystore file securely
- **Password**: Use strong passwords in production
- **Location**: Keep keystore files in secure locations
- **Version Control**: Never commit keystore files to git

### Git Ignore
The following files are ignored by git for security:
```
*.keystore
*.jks
keystore.properties
```

## Google Play Store Submission

The signed APK (`daus-todo-v1.0-signed.apk`) is ready for Google Play Store submission:

1. **File Size**: ~13.1 MB (optimized)
2. **Signing**: Properly signed with release keystore
3. **Optimization**: Minified and optimized for production
4. **Compatibility**: Android 8.0+ (API 26+)

## Troubleshooting

### Common Issues

1. **Keystore not found**
   - Ensure keystore file is in `app/daus-todo.keystore`
   - Check `keystore.properties` file exists

2. **Invalid keystore password**
   - Verify passwords in `keystore.properties`
   - Check keystore file integrity

3. **Build fails during signing**
   - Ensure all required SDK components are installed
   - Check Android SDK path in `local.properties`

### Verification Commands

```bash
# Verify keystore
keytool -list -v -keystore app/daus-todo.keystore

# Verify APK signature
jarsigner -verify -verbose -certs releases/daus-todo-v1.0-signed.apk
```

## Future Updates

When updating the app:

1. **Version Update**: Update `versionCode` and `versionName` in `build.gradle.kts`
2. **Rebuild**: Run `./gradlew assembleRelease`
3. **Test**: Verify the new signed APK works correctly
4. **Upload**: Submit to Google Play Store

## Keystore Recovery

If the keystore is lost:
1. **Backup**: Check if you have a backup
2. **Recreate**: Generate a new keystore (requires app re-upload to Play Store)
3. **Contact**: If published, contact Google Play support

---

**Note**: This keystore is for development purposes. For production apps, use a more secure keystore with stronger passwords and proper backup procedures.