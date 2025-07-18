# 🚀 Daus Todo - Deployment Guide

This guide explains how to deploy and release the Daus Todo Android app.

## 📋 Prerequisites

- ✅ Android SDK installed and configured
- ✅ Java 17 JDK
- ✅ Git repository access
- ✅ GitHub repository with Actions enabled

## 🔧 Environment Setup

### Required Environment Variables
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
export PATH=$JAVA_HOME/bin:$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

### Local Properties
Ensure `local.properties` file exists with:
```properties
sdk.dir=/opt/android-sdk
```

## 🚀 Quick Release (Automated)

### Using the Deployment Script
```bash
# Create and push a new release
./scripts/deploy.sh 1.0.0 --push

# Create release locally (without pushing)
./scripts/deploy.sh 1.0.0
```

The script will:
1. ✅ Validate version format
2. ✅ Update version in `build.gradle.kts`
3. ✅ Build debug and release APKs
4. ✅ Commit version changes
5. ✅ Create and push git tag
6. ✅ Trigger GitHub Actions CI/CD

## 🏗️ Manual Build Process

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Build Both
```bash
./gradlew assemble
```

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow
The CI/CD pipeline automatically:

1. **Test Job**
   - Runs lint checks
   - Executes unit tests
   - Uploads test reports

2. **Build Job**
   - Builds debug APK
   - Builds release APK
   - Uploads APK artifacts

3. **Release Job** (on tag push)
   - Downloads APK artifacts
   - Creates GitHub release
   - Uploads APKs to release

### Triggering CI/CD
```bash
# Push to main branch (runs test + build)
git push origin main

# Push tag (runs test + build + release)
git push origin v1.0.0
```

## 📦 Release Process

### 1. Create a New Release
```bash
# Option 1: Using deployment script (recommended)
./scripts/deploy.sh 1.0.0 --push

# Option 2: Manual process
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 2. GitHub Actions will automatically:
- Build APKs
- Create GitHub release
- Upload APKs to release
- Generate release notes

### 3. Download APKs
- Go to GitHub Releases page
- Download APK files from the latest release

## 📱 APK Information

### Debug APK
- **Purpose**: Testing and development
- **Size**: ~15-20 MB
- **Features**: Debug symbols, logging enabled
- **Installation**: Can be installed alongside release version

### Release APK
- **Purpose**: Production deployment
- **Size**: ~10-15 MB (optimized)
- **Features**: Optimized, minified
- **Note**: Currently unsigned (requires signing for Play Store)

## 🔐 Signing for Play Store

### Generate Keystore
```bash
keytool -genkey -v -keystore release-key.keystore -alias daus-todo -keyalg RSA -keysize 2048 -validity 10000
```

### Configure Signing in `build.gradle.kts`
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("release-key.keystore")
            storePassword = "your_store_password"
            keyAlias = "daus-todo"
            keyPassword = "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Build Signed APK
```bash
./gradlew assembleRelease
```

## 🎯 Version Management

### Version Format
- Use semantic versioning: `MAJOR.MINOR.PATCH`
- Example: `1.0.0`, `1.1.0`, `2.0.0`

### Version Code
- Automatically incremented by deployment script
- Used by Android for version comparison

### Current Version
Check `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 1
    versionName = "1.0.0"
}
```

## 🐛 Troubleshooting

### Common Issues

#### SDK Not Found
```bash
# Set Android SDK path
export ANDROID_HOME=/opt/android-sdk
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

#### Java Version Mismatch
```bash
# Use Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
java -version
```

#### Build Failures
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

#### Permission Denied
```bash
# Make gradlew executable
chmod +x gradlew
```

## 📊 Build Statistics

### Current Build Status
- ✅ Debug APK: ~15 MB
- ✅ Release APK: ~12 MB
- ✅ Build time: ~18 seconds
- ✅ Test coverage: Unit tests passing

### Supported Android Versions
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Compile SDK**: Android 14 (API 34)

## 🔗 Useful Commands

```bash
# Check build status
./gradlew build

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Clean build
./gradlew clean

# List all tasks
./gradlew tasks

# Build with verbose output
./gradlew assembleDebug --info
```

## 📈 Next Steps

1. **Set up signing**: Configure release signing for Play Store
2. **Add testing**: Implement more comprehensive tests
3. **Performance**: Optimize APK size and performance
4. **Security**: Add ProGuard/R8 configuration
5. **Analytics**: Integrate crash reporting and analytics

---

## 🤝 Contributing

When contributing to releases:
1. Create feature branch from `main`
2. Make changes and test thoroughly
3. Create pull request to `main`
4. After merge, use deployment script for release
5. Tag releases follow semantic versioning

## 📞 Support

For deployment issues:
- Check GitHub Actions logs
- Review build logs
- Verify environment setup
- Check Android SDK installation

Happy deploying! 🚀