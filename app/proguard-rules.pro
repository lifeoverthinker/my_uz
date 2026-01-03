# --- Standardowe reguły Androida ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- SUPABASE & KTOR & SERIALIZATION ---
# Zapobiega usuwaniu kodu klienta Supabase i Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.github.jan.supabase.**
-dontwarn io.ktor.**

# Wymagane dla kotlinx.serialization (żeby widział adnotacje @Serializable)
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <init>(...);
}

# --- TWOJE MODELE DANYCH (BARDZO WAŻNE) ---
# To mówi ProGuardowi: "Nie zmieniaj nazw zmiennych w moich klasach danych",
# dzięki temu JSON z Supabase ("name": "...") trafi do pola "val name".

# Modele w folderze data/models (np. SettingsEntity, GroupEntity itp.)
-keepnames class com.example.my_uz_android.data.models.** { *; }

# Ewentualne modele w folderze data (np. TodoItem z SupabaseClient.kt)
-keepnames class com.example.my_uz_android.data.** { *; }

# --- INNE ---
# Jeśli używasz Coroutines
-keep class kotlinx.coroutines.** { *; }