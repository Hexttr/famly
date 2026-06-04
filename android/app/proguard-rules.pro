# Add project specific ProGuard rules here.
-keep class com.famly.app.data.local.entity.** { *; }
-keep class com.famly.app.domain.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Apache POI / xmlbeans optional deps (not on Android classpath)
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.apache.xmlbeans.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.logging.log4j.**
