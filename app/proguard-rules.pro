# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
# Hilt / Dagger
-keep,allowobfuscation @interface dagger.hilt.*
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { <init>(...); }
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class *_$HiltModules* { *; }
-keep class *_HiltComponents* { *; }
# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepattributes Signature
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class com.financeapp.** { @kotlinx.serialization.Serializable <fields>; }
# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }
# Vico
-dontwarn com.patrykandpatrick.vico.**
# Apache POI
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn org.etsi.**
-dontwarn org.w3.**
-dontwarn com.microsoft.schemas.**
-dontwarn org.osgi.**
-dontwarn org.apache.logging.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.commons.compress.**
-dontwarn org.bouncycastle.**
-dontwarn org.bouncycastle.jce.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.bouncycastle.pkcs.**
-dontwarn org.bouncycastle.cert.**
-dontwarn org.bouncycastle.operator.**
-dontwarn java.awt.**
-dontwarn javax.xml.**
-dontwarn org.xml.**
-dontwarn org.w3c.**
-dontwarn org.slf4j.**
-dontwarn com.graphbuilder.**
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
