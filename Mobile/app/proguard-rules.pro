# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Yee\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-ignorewarnings

# Keep attributes
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-renamesourcefileattribute SourceFile

# Keep classes that are referenced on the AndroidManifest
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Java native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Maintain enums
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep the R
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Library dependencies
-keep class org.glassfish.** {*;}
-keep interface org.glassfish.** {*;}
-keep class * implements org.glassfish.** {*;}
-keep class javax.** {*;}
-keep class com.jjoe64** {*;}
-keep class com.github.blackfizz.** {*;}
-keep class com.nineoldandroids.** {*;}
-keep interface javax.** {*;}
-keep class * implements javax.** {*;}

-dontwarn javax.***
-dontwarn org.glassfish.***
-dontwarn okio.**
-dontwarn com.android.support.**
-dontwarn com.google.guava
-dontwarn com.jjoe64
-dontwarn com.nineoldandroids
-dontwarn junit