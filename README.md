# Fold Glass

An experimental, open-source Android demo exploring glass-like transitions on foldable phones.

**Status: alpha. Android 13+.** Fold Glass renders its own fullscreen scene. Hinge access, display switching and optical alignment still need testing on real devices.

[Download the APK](https://github.com/SimonCym/fold-glass/releases) · [Español](README.es.md) · [Report a device issue](https://github.com/SimonCym/fold-glass/issues/new/choose)

## What it does

- Fullscreen demo with refraction, frosted blur and moving highlights, using an AGSL shader.
- Continuous hinge-driven animation when Android exposes `TYPE_HINGE_ANGLE`.
- A separate **1.8-second reveal when the display becomes visible**, including when the first available hinge reading is already 180°.
- Double-tap to replay; long-press for diagnostics. No persistent toolbar or bottom menu.
- Automatic attempts to use additional displays exposed through Android's `Presentation` API.
- A `RenderEffect` blur fallback if the device rejects the shader.

The timed reveal is a presentation effect, not a reconstructed measurement of the physical hinge. This app does not replace One UI, animate other apps, or force both screens to remain on.

## Try it

1. Download the `FoldGlass-0.3-debug.apk` attachment from the alpha release.
2. Install it on Android 13 or later and open **Fold Glass**.
3. Watch the initial reveal, then fold and unfold with the app in the foreground.
4. **Double-tap** anywhere to replay without moving the hinge.
5. **Long-press** to open diagnostics and copy a report if something does not work.

Swipe from a screen edge to temporarily reveal Android's navigation bars. The current public APK is a **debug-signed test build**, not a production release. CI artifacts are also debug builds and may use a different signing key; build artifacts from different signers cannot update each other directly.

The app requests no Internet, screen-capture, accessibility-service, overlay or root permission.

## Limitations and test status

Version 0.3 was compiled and checked with Android Lint, and the pure Java motion-controller tests pass. **The developer-side validation did not run this APK on a phone or emulator.** An earlier build was reported to appear sharp immediately on opening; 0.3 changes the visibility/reveal path, but the fix needs device confirmation.

- Hinge readings and simultaneous screen availability depend on the device and firmware.
- Cover/inner classification currently uses a 600 dp width threshold.
- The artwork is a fixed demo scene. Icons do not launch apps.
- The timed reveal, physical hinge effect and real panel handover are distinct mechanisms.
- GPU appearance, latency, battery use and display alignment need hardware testing.

See [validation details](VALIDATION.md). Please avoid treating a successful build or the earlier browser concept as proof of on-device behavior.

## Build

Use **JDK 17**, Android SDK **35**, and Build Tools **35.0.0**. Gradle 8.11.1 and Android Gradle Plugin 8.9.2 are pinned.

```sh
./gradlew :app:assembleDebug :app:lintDebug
```

On Windows use `gradlew.bat`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Android Studio can also open the repository directly.

## Controller tests

```sh
sh scripts/test-controller.sh
```

The standalone Java tests cover fractional angles, late 180° readings, missing sensors, visibility changes, slow movement, invalid sensor values and timing at 30/60/120 Hz. They do not emulate Android window callbacks or GPU rendering.

The GitHub Actions workflow builds the APK, runs these tests and checks Android Lint. APK artifacts from CI are intended for testing.

## Contribute

See [CONTRIBUTING.md](CONTRIBUTING.md). Helpful contributions include real-device reports, display-handover fixes, shader calibration and performance measurements. Start a device report with your Fold model, Android/One UI version and the copied in-app diagnostics.

## Credits and license

Inspired by [u/moomanjohnny's folding-animation experiment on r/GalaxyFold](https://www.reddit.com/r/GalaxyFold/comments/1wcacld/tried_to_recreate_the_iphone_duo_animation_on_my/).

Developed with AI assistance. Fold Glass is an independent community experiment and is not affiliated with Samsung or Apple.

Project code: [MIT](LICENSE). The Gradle Wrapper retains its Apache-2.0 license; see [third-party notices](THIRD_PARTY_NOTICES.md).
