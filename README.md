# Daus Todo 📝⏰

A modern, feature-rich Todo List Android app with integrated Pomodoro Timer, built with Kotlin and Jetpack Compose.

## 🚀 Features

### Core Functionality
- **📋 Task Management**: Create, edit, delete, and organize tasks with ease
- **⏰ Pomodoro Timer**: Integrated 25-minute work sessions with 5/15-minute breaks
- **🎯 Priority System**: High, Medium, Low priority levels with color coding
- **🏷️ Categories**: Organize tasks by custom categories
- **📅 Due Dates**: Set and track task deadlines
- **✅ Completion Tracking**: Mark tasks as complete with smooth animations

### Advanced Features
- **🔍 Search & Filter**: Find tasks quickly with advanced filtering options
- **📊 Productivity Tracking**: Monitor completed pomodoro sessions and focus time
- **🔔 Smart Notifications**: Background notifications for pomodoro completion
- **🎵 Audio Feedback**: Custom sounds for work/break completion
- **🌙 Dark/Light Mode**: Automatic theme switching based on system settings
- **📱 Modern UI**: Material Design 3 with smooth animations and transitions

### Technical Highlights
- **🏗️ Clean Architecture**: MVVM pattern with Repository layer
- **💾 Local Storage**: Room database for offline functionality
- **🔄 Background Processing**: Foreground service for timer continuity
- **🎨 Jetpack Compose**: Modern declarative UI toolkit
- **🧪 Dependency Injection**: Hilt for clean dependency management

## 🛠️ Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room Database
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **Concurrency**: Coroutines & Flow
- **Background Tasks**: Foreground Services
- **Audio**: MediaPlayer with custom sound management
- **Build System**: Gradle with Kotlin DSL

## 📱 Screenshots

*Coming soon - App screenshots will be added here*

## 🏃‍♂️ Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 17 or later
- Android SDK with API level 24+ (Android 7.0)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/daus-todo.git
   cd daus-todo
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use the "Run" button in Android Studio

### Sound Files Setup

The app uses custom sound files for the Pomodoro timer. You'll need to add the following files to `app/src/main/res/raw/`:

- `bell_sound.mp3` - Work session completion sound
- `break_sound.mp3` - Break completion sound  
- `tick_sound.mp3` - Timer tick sound (optional)
- `notification_sound.mp3` - General notification sound

**Recommended Sound Sources:**
- [Freesound.org](https://freesound.org) - Free sound effects
- [Zapsplat.com](https://zapsplat.com) - Professional sound library
- [Pixabay](https://pixabay.com/sound-effects/) - Royalty-free sounds

Search for: "pomodoro bell", "gentle chime", "notification sound"

## 🏗️ Project Structure

```
app/
├── src/main/java/com/daustodo/app/
│   ├── data/
│   │   ├── database/          # Room database components
│   │   ├── repository/        # Repository pattern implementations
│   │   └── model/            # Data models and entities
│   ├── ui/
│   │   ├── components/       # Reusable UI components
│   │   ├── screens/          # Screen composables
│   │   │   ├── todo/         # Task management screens
│   │   │   └── pomodoro/     # Pomodoro timer screens
│   │   └── theme/           # App theming and colors
│   ├── viewmodel/           # ViewModels for state management
│   ├── service/             # Background services
│   │   └── PomodoroService.kt
│   ├── utils/               # Utility classes
│   │   ├── SoundManager.kt  # Audio management
│   │   └── TimeUtils.kt     # Time formatting utilities
│   └── di/                  # Dependency injection modules
├── src/main/res/
│   ├── raw/                 # Audio files
│   ├── drawable/            # Vector drawables and icons
│   ├── values/              # Strings, colors, themes
│   └── xml/                 # XML resources
└── build.gradle.kts         # App-level build configuration
```

## 🎨 Design System

### Color Palette
- **Primary**: #2196F3 (Daus Blue)
- **Secondary**: #FF9800 (Orange for Pomodoro)
- **Accent**: #4CAF50 (Green for completed tasks)
- **Error**: #F44336 (Red for high priority/errors)

### Typography
- **Headings**: Roboto Bold
- **Body**: Roboto Regular
- **Captions**: Roboto Light

### Spacing
- Base unit: 8dp
- Consistent spacing scale: 4dp, 8dp, 16dp, 24dp, 32dp

## 🔧 Configuration

### Build Variants
- **Debug**: Development build with logging enabled
- **Release**: Production build with optimizations

### Gradle Configuration
```kotlin
android {
    compileSdk = 34
    minSdk = 24
    targetSdk = 34
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
}
```

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests  
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

### Test Coverage
- Unit tests for ViewModels and Repository classes
- UI tests for critical user flows
- Integration tests for database operations

## 📦 Building for Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/release/`

### Signing Configuration
For release builds, configure signing in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "your_store_password"
            keyAlias = "your_key_alias"
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

## 🚀 CI/CD Pipeline

The project includes a complete GitHub Actions workflow:

### Automated Checks
- **Code Quality**: Lint, ktlint, detekt
- **Testing**: Unit tests and instrumented tests
- **Building**: Debug and release APK generation
- **Artifacts**: Automatic APK uploads

### Workflow Triggers
- Push to `main` or `develop` branches
- Pull requests to `main` branch

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Ensure all tests pass before submitting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Material Design 3** for the beautiful design system
- **Jetpack Compose** team for the modern UI toolkit
- **Android Architecture Components** for clean architecture patterns
- **Freesound.org** for audio resources
- **Material Icons** for the comprehensive icon set

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/yourusername/daus-todo/issues) page
2. Create a new issue with detailed description
3. Include device info, Android version, and steps to reproduce

## 🔄 Changelog

### Version 1.0.0 (Initial Release)
- ✅ Complete task management system
- ✅ Integrated Pomodoro timer
- ✅ Priority and category system
- ✅ Dark/Light theme support
- ✅ Background service for timer
- ✅ Audio feedback system
- ✅ Material Design 3 UI
- ✅ Room database integration
- ✅ Search and filter functionality

---

**Made with ❤️ by Daus Todo Team**

*Building productivity, one pomodoro at a time.*