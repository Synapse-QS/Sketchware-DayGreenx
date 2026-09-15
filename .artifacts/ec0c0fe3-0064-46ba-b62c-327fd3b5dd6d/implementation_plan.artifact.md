# Implementation Plan - Add Shizuku Install Feature

Add "Install projects with Shizuku access" feature to Sketchware settings, providing an alternative to root for automatic APK installation.

## User Review Required

> [!IMPORTANT]
> This feature requires the Shizuku app to be installed and running on the device.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle](file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/build.gradle)
- Add Shizuku API dependencies:
  ```gradle
  implementation 'dev.rikka.shizuku:api:13.1.5'
  implementation 'dev.rikka.shizuku:provider:13.1.5'
  ```

### App Manifest

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/src/main/AndroidManifest.xml)
- Add Shizuku V3 support meta-data:
  ```xml
  <meta-data
      android:name="moe.shizuku.client.V3_SUPPORT"
      android:value="true" />
  ```

### Settings UI

#### [MODIFY] [ConfigActivity.java](file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/src/main/java/mod/hilal/saif/activities/tools/ConfigActivity.java)
- Add constants:
    - `SETTING_SHIZUKU_AUTO_INSTALL_PROJECTS = "shizuku-auto-install-projects"`
    - `SETTING_SHIZUKU_AUTO_OPEN_AFTER_INSTALLING = "shizuku-auto-open-after-installing"`
- Add "Shizuku Features" category in `setupPreferences`.
- Implement Shizuku permission request when enabling the setting.

### Installation Logic

#### [MODIFY] [DesignActivity.java](file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/src/main/java/com/besome/sketch/design/DesignActivity.java)
- Modify `installBuiltApk` to check for Shizuku setting.
- Implement `installWithShizuku()` which uses Shizuku API to execute `pm install`.

## Verification Plan

### Manual Verification
1. Open Settings -> Shizuku Features.
2. Enable "Install projects with Shizuku access".
3. If Shizuku is not running or permission is denied, it should show an error and stay disabled.
4. If successful, build a project.
5. After building, the APK should be installed automatically via Shizuku.
6. If "Launch projects after installing" is enabled, the app should open after installation.
