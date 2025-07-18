#!/bin/bash

# Daus Todo - Deployment Script
# This script helps create releases and deploy the app

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if version is provided
if [ -z "$1" ]; then
    log_error "Version number is required!"
    echo "Usage: $0 <version> [--push]"
    echo "Example: $0 1.0.0 --push"
    exit 1
fi

VERSION="$1"
PUSH_TAG="$2"

# Validate version format (semantic versioning)
if [[ ! $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    log_error "Invalid version format! Use semantic versioning (e.g., 1.0.0)"
    exit 1
fi

log_info "Starting deployment for version v$VERSION"

# Check if we're on main branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    log_warning "You're not on the main branch. Current branch: $CURRENT_BRANCH"
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Deployment cancelled."
        exit 0
    fi
fi

# Check if working directory is clean
if [ -n "$(git status --porcelain)" ]; then
    log_error "Working directory is not clean. Please commit or stash your changes."
    exit 1
fi

# Update version in build.gradle.kts
log_info "Updating version in build.gradle.kts"
sed -i "s/versionName = \".*\"/versionName = \"$VERSION\"/" app/build.gradle.kts

# Update version code (increment by 1)
CURRENT_VERSION_CODE=$(grep -o 'versionCode = [0-9]*' app/build.gradle.kts | grep -o '[0-9]*')
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" app/build.gradle.kts

log_info "Updated version code from $CURRENT_VERSION_CODE to $NEW_VERSION_CODE"

# Build the APKs
log_info "Building debug APK..."
./gradlew assembleDebug

log_info "Building release APK..."
./gradlew assembleRelease

# Check if APKs were built successfully
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    log_error "Debug APK not found!"
    exit 1
fi

if [ ! -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
    log_error "Release APK not found!"
    exit 1
fi

log_success "APKs built successfully!"

# Commit version changes
log_info "Committing version changes..."
git add app/build.gradle.kts
git commit -m "Bump version to $VERSION"

# Create and push tag
log_info "Creating tag v$VERSION..."
git tag -a "v$VERSION" -m "Release version $VERSION

🚀 Daus Todo Release v$VERSION

### 📱 What's New
- Bug fixes and improvements
- Enhanced app icon and UI
- Better performance and stability

### 🎯 Features
- ✅ Todo task management
- ⏰ Pomodoro timer integration
- 🎨 Modern Material Design
- 🌙 Dark/light theme support
- 📱 Adaptive icon support

### 📦 APK Files
- Debug APK: $(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')
- Release APK: $(ls -lh app/build/outputs/apk/release/app-release-unsigned.apk | awk '{print $5}')
"

if [ "$PUSH_TAG" == "--push" ]; then
    log_info "Pushing changes and tag to remote..."
    git push origin main
    git push origin "v$VERSION"
    log_success "Tag v$VERSION pushed to remote! GitHub Actions will create the release automatically."
else
    log_warning "Changes committed and tag created locally."
    log_info "To push to remote and trigger release, run:"
    echo "  git push origin main"
    echo "  git push origin v$VERSION"
fi

# Display APK information
log_success "Deployment completed!"
echo
echo "📦 APK Information:"
echo "├── Debug APK: $(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')"
echo "├── Release APK: $(ls -lh app/build/outputs/apk/release/app-release-unsigned.apk | awk '{print $5}')"
echo "├── Version: $VERSION"
echo "└── Version Code: $NEW_VERSION_CODE"
echo
echo "🚀 Next Steps:"
echo "1. Test the APKs on your device"
echo "2. Push the tag to trigger GitHub release: git push origin v$VERSION"
echo "3. The CI/CD pipeline will automatically create a GitHub release"
echo "4. Download APKs from the GitHub release page"