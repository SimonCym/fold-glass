# Contributing

Fold Glass is an alpha experiment. Small, focused contributions and clear device reports are welcome.

## Report behavior on a device

Use the device-report issue template. Include:

- Device model, Android version, and One UI version.
- Whether the issue happens on the cover screen, inner screen, or during handover.
- Whether double-tapping can replay the effect without folding.
- The copied diagnostics from a long press, after reproducing the issue.
- Expected and actual behavior. A short video helps if you can provide one.

Review anything you attach before posting. Do not include unrelated device logs, private content or credentials. The app's diagnostic report is text you choose to copy; the app does not upload it.

## Code changes

1. Fork the repository and make a focused branch.
2. Run `sh scripts/test-controller.sh` with JDK 17.
3. Run `./gradlew :app:assembleDebug :app:lintDebug` with Android SDK 35.
4. Explain the behavior changed and state which device, if any, you tested.

For motion changes, cover meaningful edge cases: late or missing sensor readings, reversal, slow opening, visibility changes and settling. Distinguish controller tests from actual GPU/device testing.

Do not commit SDKs, build outputs, local paths, signing keys or passwords. Preserve the distinction between the physical hinge effect and the timed screen-appearance reveal. Please do not describe either as a system-wide One UI transition.

Contributions to project code are made under the repository's MIT license. Existing third-party licenses remain in effect.
