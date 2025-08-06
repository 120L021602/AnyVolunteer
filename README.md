# AnyVolunteer - Privacy-Protecting AI Assistant

## Project Overview

AnyVolunteer is an Android application that provides a privacy-protecting AI assistant with advanced face obfuscation capabilities. The application uses large language models (LLMs) to process user queries while ensuring sensitive personal information is protected through local desensitization and intelligent face detection/obfuscation algorithms.

### Key Features

- **🔒 Privacy Protection**: Automatically detects and desensitizes sensitive information like phone numbers, IDs, addresses, etc.
- **👤 Face Obfuscation**: Advanced face detection and multiple obfuscation techniques (pixelation, solid mask, translucent, color jitter, face replacement)
- **🤖 Multi-Model Support**: Supports multiple LLM models including deepseek-r1, doubao-1.5-lite-32k, doubao-1.5-pro-32k, and doubao-vision-pro-32k
- **👤 User Authentication**: Secure login and registration system with SHA-256 password hashing
- **📝 Chat History**: Comprehensive chat and image processing history management
- **🖼️ Image Processing**: Upload images for AI analysis with automatic face protection
- **💾 Local Data Storage**: SQLite database for secure local data management
- **🎯 Three-Part Display**: Shows desensitized content, LLM's reply, and restored information

### Technical Highlights

- **Computer Vision**: OpenCV integration for face detection and image processing
- **Security**: SHA-256 password hashing and local data desensitization
- **Database**: SQLite with structured tables for users, chat history, and image tasks
- **API Integration**: Volcengine Ark Runtime for LLM services
- **Multi-threading**: Asynchronous processing for image operations and API calls

## System Requirements

### Development Environment

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Gradle 8.0 or newer
- Android SDK 34 (minimum SDK 24)

### Device Requirements

- Android 7.0 (API level 24) or higher
- At least 100MB of free storage space
- Internet connection for LLM API calls
- Camera access for image upload functionality

## Installation and Setup

### Prerequisites

1. Install Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Make sure you have Git installed to clone the repository

### Clone the Repository

```bash
git clone <repository-url>
cd AnyVolunteer_GroupProject
```

### API Key Configuration

The application uses the Volcengine Ark Runtime service for LLM API calls. You need to configure your API key:

1. Open the `app/src/main/java/com/hkucs/groupproject/config/Config.java` file
2. Replace the placeholder API key with your actual key from Volcengine

```java
public static final String API_KEY = "your_api_key_here";
```

## Compilation and Building

### Using Android Studio

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the cloned repository folder and select it
4. Wait for Gradle sync to complete
5. Build the project by selecting **Build > Make Project** or pressing **Ctrl+F9** (Windows/Linux) or **Cmd+F9** (Mac)

### Using Command Line

You can also build the project using the command line:

```bash
# On Windows
.\gradlew assembleDebug

# On macOS/Linux
./gradlew assembleDebug
```

The APK file will be generated in `app/build/outputs/apk/debug/app-debug.apk`

## Running the Application

### On an Emulator

1. In Android Studio, click on **Run > Run 'app'** or press **Shift+F10** (Windows/Linux) or **Control+R** (Mac)
2. Select an existing emulator or create a new one
3. Wait for the application to install and launch

### On a Physical Device

1. Enable USB debugging on your Android device:
   - Go to **Settings > About phone**
   - Tap **Build number** seven times to enable developer options
   - Go back to **Settings > System > Developer options** and enable **USB debugging**
2. Connect your device to your computer via USB
3. In Android Studio, click on **Run > Run 'app'** and select your device
4. Allow the installation when prompted on your device

### Using the APK Directly

1. Transfer the generated APK file to your Android device
2. On your device, navigate to the APK file using a file manager
3. Tap the APK file and follow the installation prompts
4. You may need to enable installation from unknown sources in your device settings

## Usage Guide

### Registration and Login

1. When you first launch the app, you'll see the welcome screen
2. If you don't have an account, tap on "Register" to create one
3. Enter your username and password, then tap "Register"
4. After registration, you'll be redirected to the login screen
5. Enter your credentials and tap "Login"

### Using the Chat Interface

1. After logging in, you'll see the main chat interface
2. Select an LLM model from the dropdown menu at the bottom
3. Type your message in the input field and tap the send button
4. The app will process your message, desensitize any personal information, and send it to the LLM
5. You'll see three parts in the response:
   - The desensitized version of your message
   - The LLM's reply to your query
   - The information with sensitive data restored

### Image Processing with Face Obfuscation

1. Select the "doubao-vision-pro-32k" model from the dropdown
2. Tap the camera button to upload an image
3. The app will automatically detect faces in the image
4. Enter your prompt describing what you want to do with the image
5. The system will choose appropriate obfuscation techniques based on your prompt
6. View the processed image with faces protected according to privacy requirements

### Viewing Chat History

1. Tap the history button (top left corner) to view your chat history
2. Select any chat from the list to view the full conversation
3. For image tasks, you can view both original and processed images

### User Profile and Logout

1. Tap the user icon (top right corner) to access your profile
2. You can see your username, remaining tokens, and image credits
3. Tap "Back to Main" to return to the chat interface
4. Tap "Logout" to end your session

## Technical Architecture

### Core Components

- **User Management**: Registration, login, session management with SHA-256 password hashing
- **Face Detection**: OpenCV-based face detection using Haar cascade classifiers
- **Image Processing**: Multiple obfuscation techniques (pixelation, solid mask, translucent, color jitter, face replacement)
- **Data Storage**: SQLite database with structured tables for users, chat history, and image tasks
- **API Integration**: Volcengine Ark Runtime for LLM services
- **Privacy Protection**: Local data desensitization with regex pattern matching

### Database Schema

- **users**: User accounts with hashed passwords, token quotas, and image credits
- **chat_history**: Text conversation history with desensitization tracking
- **image_tasks**: Image processing tasks with original and processed image paths

### Security Features

- SHA-256 password hashing for secure authentication
- Local data desensitization to protect sensitive information
- Face obfuscation to protect privacy in images
- SQLite database with proper access controls

## Troubleshooting

### Common Issues

1. **App crashes on startup**
   - Make sure you have the correct Android SDK version installed
   - Check if your device meets the minimum requirements
   - Ensure OpenCV is properly initialized

2. **Cannot connect to LLM service**
   - Verify your internet connection
   - Check if your API key is correctly configured
   - Ensure the LLM service is available

3. **Login issues**
   - If you forgot your password, you'll need to reinstall the app (password recovery not implemented)
   - Make sure you're entering the correct username and password

4. **Image processing fails**
   - Check if you have sufficient image credits
   - Ensure the image contains detectable faces
   - Verify camera permissions are granted

### Debugging

If you encounter issues, you can enable logging:

1. Open `app/src/main/java/com/hkucs/groupproject/config/Config.java`
2. Set the debug flag to true:
   ```java
   public static final boolean DEBUG = true;
   ```
3. Check the Android logcat for detailed error messages

## Project Structure

```
app/src/main/java/com/hkucs/groupproject/
├── activity/           # UI activities and screens
├── adapter/            # RecyclerView adapters
├── config/             # Configuration and constants
├── database/           # Database management classes
├── handler/            # Business logic handlers
├── model/              # LLM model implementations
├── response/           # API response classes
├── utils/              # Utility classes (ImageProcessor)
└── util/               # Helper utilities

app/src/main/res/
├── layout/             # XML layout files
├── drawable/           # Graphics and drawable resources
└── values/             # String, color, and dimension resources
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- OpenCV for computer vision capabilities
- Volcengine Ark Runtime for LLM services
- Android development community for best practices
