# Validation — Fold Glass 0.3.0

## Completed locally

- Android debug APK built successfully with JDK 17, Gradle 8.11.1, AGP 8.9.2 and SDK/Build Tools 35.
- Android Lint: zero issues in the final build.
- APK signature verified; the 0.3 APK has the same certificate as the previously shared 0.2 APK.
- Manifest verified: `studio.foldglass`, version `0.3.0`, versionCode `3`, minSdk `33`, targetSdk `35`.
- Pure Java math and motion-controller tests passed.

The alpha APK is debug-signed. Its SHA-256 is:

```text
d32b4e5b395a6dff1b3dca7f40ad1bd958b247097c7bdc434973f0bbee3dc7c3
```

## Controller regression coverage

- A visible first-frame reveal even when the first sensor event is 180°.
- Reveal retained past the former 950 ms timeout and completed after 1.8 seconds.
- Replay on resume without new sensor readings.
- Missing sensors do not disable replay or invent physical hinge readings.
- Partial postures become clear after the extended rest period.
- Slow 0.1° increments are not mistaken for rest.
- Non-finite sensor values are ignored.
- Finite, monotonic reveal output at 30/60/120 Hz.
- Rendering stops requesting frames after the effect settles.

## Not yet verified

**Version 0.3 was not installed or run on a phone or emulator during developer-side validation.** Compilation and the controller tests do not execute real Android window callbacks, gestures, AGSL on a GPU, or Samsung's display activation policies.

An earlier build was reported to become visible already sharp when unfolding. Version 0.3 changes the affected code paths, but no device logs established the exact cause and no subsequent device test has confirmed the fix.

The fullscreen layout was inspected in source. GPU appearance, sensor timing, display classification, physical continuity and battery use remain open hardware-validation tasks.

The original browser visualization was a separate concept and is not evidence that the APK behaves identically.

[Spanish development notes](VALIDATION.es.md)
