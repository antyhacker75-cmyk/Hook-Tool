# Astro Hook — Android Project

Android Studio project configured with the Astro Hook app identity.

## Project contents

- Android Java source files and resources
- App package: `com.astrotool.hook`
- Bundled native and asset files, including `libmain.so`, `Mgubs1`, and `Mgubs2` (original payload filenames retained)

## Open in Android Studio

1. Open the repository folder in Android Studio
2. Let Gradle sync
3. Build with `./gradlew assembleDebug`

## Notes

- `com.mgubs.lib.dumper.BinaryFileProcessor` remains a legacy stub referenced by the manifest
- `libmain.so` is a bundled native library

