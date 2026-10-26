# Lock

100% Kotlin Android app lock. Jetpack Compose (black + blue UI, bottom navigation), Hilt, Room, MVVM.

## Build on GitHub
1. Push this whole folder to the `main` branch.
2. Open **Actions > Android Build** (it runs on every push).
3. When it is green, download **CustomAppLockStudio-release-apk** / **-aab** from the run's Artifacts.

Optional Play Store signing: add repo secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Without them every build uses a fresh temporary key, so uninstall the old app before installing a new APK.

## Before publishing
Replace the **test** AdMob IDs in `app/src/main/res/values/strings.xml`.

## First run
A step-by-step wizard asks for Accessibility, Display over other apps, Notifications and Camera, then you
create a mandatory master lock that protects the Lock app itself.
