Zippy icon pack (speed play, black and white)

Contents
  android/res/                 drop-in Android resources (merge into app/src/main/res)
    mipmap-*dpi/ic_launcher.png, ic_launcher_round.png   legacy icons, 48/72/96/144/192 px (Android 7.x)
    mipmap-anydpi-v26/ic_launcher.xml, ic_launcher_round.xml   adaptive icon (Android 8+), with monochrome layer (Android 13+ themed icons)
    drawable/ic_launcher_foreground.xml, ic_launcher_monochrome.xml   108dp vector layers, glyph inside the 66dp safe zone
    drawable/ic_stat_zippy.xml   24dp white notification small icon
    values/ic_launcher_background.xml   background color #000000
  playstore/ic_launcher-512.png   512x512 Play Console icon (full-bleed square, Play applies the mask)
  logo/                           zippy-logo-1024/512/256.png (rounded tile), zippy-logo.svg, zippy-glyph-black-transparent.svg
  png-fallback/                   PNG versions of the adaptive foreground (108dp at each density) and notification icon, only if you prefer PNG over vector

How to install
  1. Delete the old launcher files first (ic_launcher*.webp / .png in every mipmap-* folder, old mipmap-anydpi-v26 xml,
     old ic_launcher_foreground and ic_launcher_background). Duplicate resource names with different extensions break the build.
  2. Copy android/res/* into app/src/main/res/.
  3. AndroidManifest.xml: android:icon="@mipmap/ic_launcher" and android:roundIcon="@mipmap/ic_launcher_round".
  4. Notifications: builder.setSmallIcon(R.drawable.ic_stat_zippy).
  5. Clean and rebuild, then check the launcher with a circle, squircle and square icon shape, and Android 13+ themed icons.
  6. Upload playstore/ic_launcher-512.png to the Play Console store listing if you publish.
