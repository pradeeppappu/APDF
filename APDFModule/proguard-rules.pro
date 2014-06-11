# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# To remove debug logs.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

-keepclassmembers class au.pappu.apdf.PDFFragment.PDFJavascriptInterface { *; }

