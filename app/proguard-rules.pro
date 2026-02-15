-keep class com.ggdpi.app.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-dontwarn timber.log.Timber