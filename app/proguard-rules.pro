-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.synco.**$$serializer { *; }
-keepclassmembers class app.synco.** {
    *** Companion;
}
-keepclasseswithmembers class app.synco.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class app.synco.** {
    <fields>;
    static **$* *;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}

-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }
-keep class org.bouncycastle.crypto.** { *; }
-keep class org.bouncycastle.math.** { *; }
-keep class org.bouncycastle.util.** { *; }
-keep class org.bouncycastle.asn1.** { *; }
-keepclassmembers class org.bouncycastle.** {
    <init>(...);
}
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**

-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy

-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }
-keep class rikka.sui.** { *; }
-dontwarn rikka.shizuku.**
-dontwarn moe.shizuku.**

-keep class org.lsposed.hiddenapibypass.** { *; }
-dontwarn org.lsposed.hiddenapibypass.**

-keep class android.content.IClipboard { *; }
-keep class android.content.IClipboard$* { *; }
-keep class android.content.IOnPrimaryClipChangedListener { *; }
-keep class android.content.IOnPrimaryClipChangedListener$* { *; }
-dontwarn android.content.IOnPrimaryClipChangedListener

-keep class app.synco.shizuku.** { *; }
