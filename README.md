# 🎸 Rock Dial Dates

A vintage-style Android music app that lets you explore and listen to classic rock songs by decade, with a retro radio aesthetic built entirely in Jetpack Compose.

---

## 📱 Screenshots

[Rock Dial Dates running on tablet Samsung Galaxy S6](https://www.youtube.com/watch?v=Rl3tOrr5bec)

[Rock Dial Dates running on phone Pixel 8A](https://www.youtube.com/watch?v=rQAgr1SCh48)

### Videos

| Tablet (Samsung Galaxy S6) | Phone (Pixel 8A) |
|:---:|:---:|
| [![Tablet](https://img.youtube.com/vi/Rl3tOrr5bec/0.jpg)](https://www.youtube.com/watch?v=Rl3tOrr5bec) | [![Phone](https://img.youtube.com/vi/rQAgr1SCh48/0.jpg)](https://www.youtube.com/watch?v=rQAgr1SCh48) |

### Phone

| Phone (Pixel 8A) |
|:---:|:---:|:---:|
| ![](https://raw.githubusercontent.com/graffiti75/RockWoodDial/master/media/phone/Screenshot_20260301-151629.png) | ![](https://raw.githubusercontent.com/graffiti75/RockWoodDial/master/media/phone/Screenshot_20260301-151648.png) | ![](https://raw.githubusercontent.com/graffiti75/RockWoodDial/master/media/phone/Screenshot_20260301-151707.png) |

### Tablet

| Tablet (Samsung Galaxy S6) |
|:---:|:---:|
| ![](https://raw.githubusercontent.com/graffiti75/RockWoodDial/master/media/tablet/Screenshot_20260301_151448_Rock%20Dial%20Dates.jpg) | ![](https://raw.githubusercontent.com/graffiti75/RockWoodDial/master/media/tablet/Screenshot_20260301_151542_Rock%20Dial%20Dates.jpg) |

---

## 🎯 Features

- **Decade Selector** — Browse classic rock songs from the 50s, 60s, 70s, 80s, 90s, and 2000s
- **YouTube Playback** — Streams songs via the YouTube IFrame Player API (fully ToS compliant)
- **VU Meters** — Animated Canvas-drawn VU meters reacting to device volume (Power) and playback state (Signal)
- **Volume Knob** — Drag-to-rotate knob that controls device media volume, synced with hardware buttons
- **Play / Pause / Next** — Full playback controls in the bottom bar
- **Progress Slider** — Seek to any position in the current song
- **Song Info** — Displays band name, song title, and year
- **Retro Radio UI** — Custom background assets, metallic textures, and a vintage dial aesthetic
- **Splash Screen** — Custom branded splash screen using the AndroidX SplashScreen API
- **Tablet & Phone Support** — Fully responsive layout with separate configs for phone and tablet
- **Landscape Lock** — App locks to landscape orientation for the best radio experience

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + StateFlow |
| DI | Hilt |
| Navigation | Compose Navigation |
| YouTube | [android-youtube-player](https://github.com/PierfrancescoSoffritti/android-youtube-player) by Pierfrancesco Soffritti |
| Splash Screen | AndroidX Core SplashScreen |
| Serialization | Kotlin Serialization |
| Build | Gradle (KTS) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## 🎨 UI Architecture

The screen is divided into three vertical sections:

```
┌─────────────────────────────────────────┐
│              Toolbar                    │
├──────────────┬──────────────────────────┤
│  VU Meters   │  Decade Selector         │
│              │  Song Timeline (Trails)  │
│  YouTube     │  Song Info               │
│  Player      │                          │
├──────────────┴──────────────────────────┤
│  Slider   ▶  ⏭  [Knob]                 │
└─────────────────────────────────────────┘
```

- **Left column**: VU meters on top, YouTube player (touch-disabled) below
- **Right column**: Decade tabs, song trail/timeline, and song info
- **Bottom bar**: Progress slider, Play/Pause button, Next button, and volume knob

---

## 🔧 Project Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android device or emulator running API 24+

### Clone & Run

```bash
git clone https://github.com/your-username/RockWoodDial.git
cd RockWoodDial
```

Open in Android Studio, sync Gradle, and run on a device or emulator.

### Dependencies

All dependencies are managed via `libs.versions.toml` (version catalog). Key versions:

```toml
kotlin = "2.0.21"
composeBom = "2025.06.01"
hilt = "2.51.1"
youtubePlayer = "12.1.0"
splashScreenVersion = "1.0.1"
```

---

## 📁 Project Structure

```
app/src/main/java/com/cericatto/rockwooddial/
├── data/
│   ├── Song.kt                     # Song data model
│   └── SongParser.kt               # Parses songs by decade
├── ui/
│   ├── common/
│   │   ├── CommonComposables.kt    # Shared composables (Loading, Error, Knob)
│   │   ├── MainScreenComposables.kt# Main screen sections
│   │   └── VuMetersComposables.kt  # Canvas-drawn VU meters
│   ├── main_screen/
│   │   ├── MainScreen.kt           # Root screen + YouTube player lifecycle
│   │   ├── MainScreenViewModel.kt  # MVVM ViewModel
│   │   ├── MainScreenState.kt      # UI state
│   │   ├── MainScreenAction.kt     # User actions
│   │   └── LayoutConfig.kt         # Phone/tablet layout configs
│   ├── navigation/
│   │   └── NavHostComposable.kt    # Navigation graph
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── MainActivity.kt
└── RockDialApp.kt                  # Hilt application class
```

---

## 🎛️ VU Meters

The VU meters are drawn entirely using Compose `Canvas`:

- **Power Meter** — Polls device media volume every 300ms via `AudioManager` and animates the needle accordingly
- **Signal Meter** — Reacts to the YouTube player state (`PLAYING` → full, `BUFFERING` → center, else → minimum)
- Both meters feature a blue arc, tick marks, animated needle, double border, and letter-spaced labels

---

## 🔊 Volume Knob

The rotary volume knob:

- Reads the initial angle from the current device volume on startup
- Detects circular drag gestures (like turning a real knob) using `detectDragGestures`
- Clamps rotation between **30°** (min) and **150°** (max)
- Maps the angle to `AudioManager.STREAM_MUSIC` volume in real time
- Listens for external volume changes (hardware buttons, system UI) via `BroadcastReceiver` and rotates accordingly

---

## 📜 License

This project is for educational and personal use. It streams content from YouTube using the official IFrame Player API and does not host or distribute any copyrighted material directly.

---

## 🙏 Credits

- YouTube playback powered by [android-youtube-player](https://github.com/PierfrancescoSoffritti/android-youtube-player) — © Pierfrancesco Soffritti
- UI built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Dependency injection by [Hilt](https://dagger.dev/hilt/)
