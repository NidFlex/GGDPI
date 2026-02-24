// C:\GGDPI\build.gradle.kts

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // ✅ ДОБАВЬТЕ Compose Compiler plugin
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

// ✅ Опционально: управление версиями зависимостей
extra.apply {
    set("composeBomVersion", "2023.10.01")
    set("kotlinCoroutinesVersion", "1.7.3")
}