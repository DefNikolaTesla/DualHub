# DualSpace Engine – Personal Multi-Instance Foundation

## What this is

This is a **foundation** for a VirtualApp-style multi-account / dual-space engine for personal use only.

It is **not** a finished commercial product. It provides:

- VirtualCore singleton and registry of clones
- APK rewriting pipeline (package name change)
- Isolated data directories per clone
- PackageManagerProxy / ActivityManagerProxy structure (hooks not fully activated)
- Keep-alive foreground service
- Compose host launcher UI to list apps, create clones, and manage them

## What still needs work for full dual-app behavior

1. **Proper binary AndroidManifest rewriting**  
   The current ApkRewriter uses a naive byte patch. Production engines use a real binary XML rewriter.

2. **APK signing**  
   Rewritten APKs are unsigned. You must sign them (apksigner / jarsigner) before they can be installed.

3. **Deep PackageManager + ActivityManager hooks**  
   Full multi-instance requires replacing the system binders via reflection. These hooks break across Android versions and must be maintained.

4. **Process & ClassLoader isolation**  
   Launching a virtual package as a real running process with its own identity needs additional engine work.

5. **Device testing**  
   Every phone / Android version behaves differently. You must test on your own device and fix crashes with logcat.

## How to open and build

1. Open the `DualSpaceEngine` folder in Android Studio (Hedgehog or newer recommended).
2. Let Gradle sync. Fix any dependency version mismatches if they appear.
3. Build → Build Bundle(s) / APK(s) → Build APK(s).
4. Install the debug APK on your phone (enable “Install unknown apps”).
5. Grant the requested permissions.
6. Use the UI to attempt cloning an app (e.g. a simple test app first).

## Realistic expectations

- You will be able to create rewritten APKs and isolated data folders.
- Full “two WhatsApp running side-by-side with separate accounts” requires completing the proxy + process layers and signing.
- This foundation gives you the correct architecture and the critical classes so you are not starting from zero.

## Legal note

This project is for personal use on devices you own. Cloning apps and running multiple accounts may violate the terms of service of the target applications. Use responsibly.

---

Generated as a personal dual-space foundation.
