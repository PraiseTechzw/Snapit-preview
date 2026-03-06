# Snapit 📸

**Snapit** is a modern, high-performance Android application designed for seamless media capture and screen recording. Featuring a sleek, premium interface built entirely with Jetpack Compose, Snapit offers a "Power Mode" floating bubble that allows you to take photos or record your screen from anywhere on your device.

## 🚀 Key Features

-   **Floating Snap Bubble**: A persistent, draggable overlay for quick access to camera and screen recording actions without leaving your current app.
-   **Smart Camera**: Pro-grade photography and video recording using CameraX, with high-quality output and intuitive controls.
-   **Screen Recording**: Smooth, high-definition screen recording with optional audio support, perfect for tutorials or capturing game highlights.
-   **Premium UI**: A stunning, dark-themed design featuring animated gradients, glassmorphism effects, and smooth transitions.
-   **Integrated Gallery**: Easily browse, preview, and manage your captured photos and recordings.
-   **Usage Statistics**: Keep track of your creative output with an integrated captures counter.

## 🛠 Tech Stack

-   **UI**: Jetpack Compose (Material 3)
-   **Language**: Kotlin
-   **Dependency Injection**: Hilt
-   **Camera API**: CameraX
-   **Screen Capture**: MediaProjection API
-   **Data Storage**: DataStore (Settings) & MediaStore (Assets)
-   **Architecture**: MVVM with Repository Pattern

## 📥 Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/PraiseTechzw/Snapit.git
    ```
2.  Open the project in **Android Studio (Ladybug or newer)**.
3.  Ensure you have the latest **Kotlin** and **Compose** plugins installed.
4.  Build and run on a device or emulator running **Android 9.0 (API 28)** or higher.

## 📝 Permissions

Snapit requires the following permissions for full functionality:
-   `CAMERA`: For taking photos and recording videos.
-   `RECORD_AUDIO`: For capturing sound during screen recordings.
-   `SYSTEM_ALERT_WINDOW`: For the "Power Mode" floating bubble overlay.
-   `POST_NOTIFICATIONS`: For foreground service status and controls.

## 🛡 License

Distributed under the MIT License. See `LICENSE` for more information.

---

*Developed by PraiseTechzw*
