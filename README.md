# Reflect

A calm, mindful app-blocking app. Kotlin + Jetpack Compose (Material 3).

## Important — read before building

I generated this project's full source, resources, and Gradle config by hand, but I have **no Android SDK, JVM, or emulator** in my own environment, so I could not compile it or produce a real APK myself. I also could not download the Gradle wrapper binary (`gradle-wrapper.jar`) because my network access is restricted to a small allowlist of package registries — it does not include `services.gradle.org`.

That means: **this zip does not include a working `gradlew` wrapper jar.** You need to generate it once inside Codespaces (takes 10 seconds), then everything builds normally. Steps below.

## Build in GitHub Codespaces

1. Push this project to a new GitHub repo, open it in a Codespace (or open the repo folder directly if you already cloned it into an existing Codespace).

2. Install a JDK 17 and Android command-line tools if not already present. Easiest path — use the community devcontainer feature, or manually:

```bash
sudo apt-get update && sudo apt-get install -y openjdk-17-jdk unzip
java -version   # confirm 17
```

3. Install Android SDK command-line tools + platform 34 + build-tools:

```bash
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
curl -o cmdtools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip cmdtools.zip && mv cmdline-tools latest
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

   Add a `local.properties` file in the project root pointing at it:
   ```
   sdk.dir=/home/<your-codespace-user>/android-sdk
   ```
   (`echo "sdk.dir=$ANDROID_HOME" > local.properties`)

4. Generate the Gradle wrapper (this downloads `gradle-wrapper.jar` for you — needs a system Gradle, which Codespaces' default Java image usually has, or install via `sdk install gradle` / apt):

```bash
gradle wrapper --gradle-version 8.7
```

5. Build the debug APK:

```bash
./gradlew assembleDebug
```

6. The APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```
Download it from the Codespaces file explorer, or install directly with `adb install` if you have a connected device/emulator.

## If the build fails

Paste me the **exact error output** from `./gradlew assembleDebug --stacktrace` and I will fix the source directly — this is a hand-written project so it's very possible there's a typo or a version mismatch I couldn't verify without a compiler. Common things to check first:
- `compileSdk`/`targetSdk` 34 and `minSdk` 26 are set in `app/build.gradle.kts`
- AGP version (8.4.2) needs Gradle 8.6+ — the wrapper above uses 8.7, that's fine
- Kotlin plugin version 1.9.24 must match the Compose compiler extension version 1.5.14 (already matched in `app/build.gradle.kts`)

## What's NOT included (you'll need to add for a polished release)

- Real launcher icon PNGs (a simple vector adaptive icon is included so it compiles and looks decent — swap in your own artwork later)
- Signing config for a release build (debug build works fine for testing on your own device)
- Any automated tests

## App overview

- **Home** — shows count of watched apps, permission status, entry to app picker
- **Select apps** — searchable, multi-select list of installed apps
- **Decision screen** — full-screen calm interstitial shown via `AccessibilityService` whenever a watched app is foregrounded
- **Foreground service + notification** — keeps the watcher alive, restarts on boot via `BootReceiver`
- **DataStore** — persists your selected apps across restarts/reboots automatically
