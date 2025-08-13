# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\AndroidSDK/tools/proguard/proguard-android.txt
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

-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
#noinspection ShrinkerUnresolvedReference
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keep public class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keepclasseswithmembers class com.google.firebase.FirebaseException

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Yandex AppMetrica SDK.
-keep class com.yandex.metrica.** { *; }
-dontwarn com.yandex.metrica.**

# ADMOST
        -keepattributes Exceptions, InnerClasses
        -dontwarn admost.sdk.**
        -keep class admost.sdk.** {*;}
        -dontwarn com.amr.unity.**
        -keep class com.amr.unity.** {*;}
        -dontwarn admost.adserver.**
        -keep class com.adjust.sdk.** {*;}
        -dontwarn com.adjust.sdk.**
        -keep class com.appsflyer.** {*;}
        -dontwarn com.appsflyer.**
        -keep class admost.adserver.** { *; }
        -dontwarn com.google.android.exoplayer2.**
        -keep class com.google.android.exoplayer2.**{ *;}
        -keep class android.support.v4.app.DialogFragment { *; }
        -keep class android.support.v4.util.LruCache { *; }

        # ADMOB / ADX / GOOGLE
        -keep class com.android.vending.billing.**
        -keep public class com.google.android.gms.ads.** { public *; }
        -keep public class com.google.ads.** { public *; }
        -keep class com.google.android.gms.** { *; }
        -dontwarn com.google.android.gms.**
        -keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }
        -keepnames class * implements android.os.Parcelable
        -keepclassmembers class * implements android.os.Parcelable { public static final *** CREATOR; }
        -keep @interface android.support.annotation.Keep
        -keep @android.support.annotation.Keep class *
        -keepclasseswithmembers class * { @android.support.annotation.Keep <fields>; }
        -keepclasseswithmembers class * { @android.support.annotation.Keep <methods>; }
        -keep @interface com.google.android.gms.common.annotation.KeepName
        -keepnames @com.google.android.gms.common.annotation.KeepName class *
        -keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *; }
        -keep @interface com.google.android.gms.common.util.DynamiteApi
        -keep public @com.google.android.gms.common.util.DynamiteApi class * { public <fields>; public <methods>; }
        -keep public @com.google.android.gms.common.util.DynamiteApi class * { *; }
        -keep class com.google.android.apps.common.proguard.UsedBy*
        -keep @com.google.android.apps.common.proguard.UsedBy* class *
        -keepclassmembers class * { @com.google.android.apps.common.proguard.UsedBy* *; }
        -dontwarn android.security.NetworkSecurityPolicy
        -dontwarn android.app.Notification

        # FACEBOOK
        -dontwarn com.facebook.ads.**
        -dontnote com.facebook.ads.**
        -keep class com.facebook.** { *; }
        -keepattributes Signature
        -keep class com.google.android.exoplayer2.** {*;}
        -dontwarn com.google.android.exoplayer2.**

        # MINTEGRAL
        -keepattributes Signature
        -keepattributes *Annotation*
        -keep class com.mbridge.** {*; }
        -keep interface com.mbridge.** {*; }
        -keep interface androidx.** { *; }
        -keep class androidx.** { *; }
        -keep public class * extends androidx.** { *; }
        -dontwarn com.mbridge.**

        #PANGLE
        -dontwarn com.bytedance.**
        -keep class com.bytedance.** {*;}
        -keep class **.R$* { public static final int mbridge*; }