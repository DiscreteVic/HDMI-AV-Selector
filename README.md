# HDMI-AV Selector

A tiny Android TV app that pops up a list of your TV's physical inputs (HDMI, AV, ...) and switches to the one you pick.

<p align="center">
  <img src="app/src/main/res/drawable-xhdpi/banner.png" width="320" alt="Input Selector banner">
</p>

## Why

Many Android TVs ship with a stock launcher that has an **Inputs** / **Sources** menu. If you replace it with a custom launcher such as **ARC Launcher**, that menu is gone, and there's often no easy way left to switch between HDMI ports or AV.

This app adds that back as a normal launcher tile: open **Input Selector**, pick an input, done.

## Features

- Lists the TV's hardware inputs: HDMI, AV (composite), component, S-Video, SCART, VGA, DVI, DisplayPort.
- Uses the names you set on the TV (e.g. "HDMI 2 (ARC)", or "PlayStation" if you renamed it).
- Marks inputs with nothing plugged in as **Not connected** and powered-off devices as **Standby**.
- Refreshes on its own when a cable is plugged in or removed.
- Remote-friendly popup: D-pad to move, OK to switch, Back to close.
- No permissions, no internet, no tracking, no dependencies. The APK is under 30 KB.
- Works on any CPU (armv7, arm64, x86): it's pure Java with no native code.

## Requirements

- An Android TV / Google TV with **Android 7.0 (API 24) or newer**.
- The TV's built-in Live TV app must still be installed (it's what actually shows the input). You can keep using any launcher you like.

## Tested on

| TV | Android | CPU | Launcher |
|---|---|---|---|
| Xiaomi Mi TV (MiTV-MSSP3) | 9 | armeabi-v7a | ARC Launcher |

If it works (or doesn't) on your TV, please open an issue with the model so this list can grow.

## Install

### Option A: build and install with adb

1. On the TV, enable **Developer options** (Settings → Device Preferences → About → press **Build** 7 times), then turn on **USB debugging** / **Network debugging**.
2. On your PC, install the build tools once. Android Studio is not needed. You need:
   - **JDK 17 or newer** (e.g. `java-21-openjdk-devel` on Fedora, `openjdk-21-jdk` on Debian/Ubuntu)
   - the Android [command-line tools](https://developer.android.com/studio#command-line-tools-only), unzipped to `$ANDROID_HOME/cmdline-tools/latest`
   - the SDK packages: `sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"`
   - `ANDROID_HOME` exported, and `$ANDROID_HOME/platform-tools` on your `PATH` (for `adb`)

   Gradle itself is downloaded by the included wrapper.

3. Build and install:

   ```bash
   git clone https://github.com/DiscreteVic/HDMI-AV-Slector.git
   cd HDMI-AV-Slector

   adb connect <tv-ip>:5555              # accept the prompt on the TV
   ./gradlew installDebug
   ```

### Option B: sideload an APK

Build it with `./gradlew assembleDebug`. Then copy `app/build/outputs/apk/debug/app-debug.apk` to the TV (USB stick, or an app like *Send Files to TV*) and open it with a file manager.

After installing, **Input Selector** appears in the TV's app list. In ARC Launcher (or any other launcher) you can add it to your favourites or home row.

## How it works

- Inputs come from [`TvInputManager.getTvInputList()`](https://developer.android.com/reference/android/media/tv/TvInputManager#getTvInputList()). The app keeps only hardware *passthrough* inputs and skips tuners, streaming-app channels, and HDMI-CEC sub-devices (they appear under their HDMI port).
- Selecting an input sends `ACTION_VIEW` with [`TvContract.buildChannelUriForPassthroughInput(id)`](https://developer.android.com/reference/android/media/tv/TvContract#buildChannelUriForPassthroughInput(java.lang.String)). The TV's Live TV app opens that input. This is the standard way for a regular (non-system) app to change input.
- If a manufacturer has removed that path, the app shows a message instead of crashing.

All the logic is in [`InputSelectorActivity.java`](app/src/main/java/com/discretevic/avselector/InputSelectorActivity.java).

## Project layout

```
app/src/main/
  AndroidManifest.xml                 TV launcher entry, popup theme
  java/.../InputSelectorActivity.java list inputs + switch
  res/                                layouts, colours, banner
scripts/
  make-banner.py                      regenerates the launcher banner
```

To change the tile text: `python3 scripts/make-banner.py "My Text"`, then rebuild.

## Troubleshooting

- **"No HDMI or AV inputs were found"**: the TV doesn't expose its inputs through the standard Android TV input framework. Please open an issue with the TV model.
- **"This TV does not allow switching to ..."**: no app on the TV handles passthrough input links (the Live TV app is probably disabled or was removed). Re-enable it under Settings → Apps → Show system apps.
- **`adb` says `unauthorized`**: accept the "Allow debugging?" prompt on the TV, ticking *Always allow from this computer*.
- **Old icon still showing after an update**: restart the TV; launchers cache banners.

## License

[GPL-2.0](LICENSE)
