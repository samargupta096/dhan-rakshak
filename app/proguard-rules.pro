# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.dhanrakshak.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }

# Keep Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.dhanrakshak.data.remote.dto.** { *; }

# Keep iText
-keep class com.itextpdf.** { *; }

# Keep SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.* { *; }
