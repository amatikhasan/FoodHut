# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


### OKHTTP


# JSR 305 annotations are for embedding nullability information.

-dontwarn javax.annotation.**



# A resource is loaded with a relative path so the package of this class must be preserved.

-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase



# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.

-dontwarn org.codehaus.mojo.animal_sniffer.*



# OkHttp platform used only on JVM and when Conscrypt dependency is available.

-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Platform calls Class.forName on types which do not exist on Android to determine platform.

-dontnote okhttp3.internal.Platform

## Square Picasso specific rules ##

## https://square.github.io/picasso/ ##

-dontwarn com.squareup.okhttp.**

### OKIO

# java.nio.file.* usage which cannot be used at runtime. Animal sniffer annotation.

-dontwarn okio.Okio

# JDK 7-only method which is @hide on Android. Animal sniffer annotation.

-dontwarn okio.DeflaterSink
