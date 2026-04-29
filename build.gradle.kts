import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.11"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "com.qrcode.generator"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    gradlePluginPortal()
}

dependencies {
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    
    // QR Code generation
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    
    // JSON handling
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.qrcode.generator.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "QRCodeGenerator"
            packageVersion = "1.0.0"
            description = "QR Code Generator Application"
            copyright = "© 2024 QR Code Generator"
            vendor = "QR Code Generator"
            
            // 添加JVM参数来启用更详细的错误信息并解决字符集问题
            jvmArgs(
                "-Dfile.encoding=UTF-8",
                "-Dsun.stdout.encoding=UTF-8",
                "-Dsun.stderr.encoding=UTF-8",
                "-XX:+ShowCodeDetailsInExceptionMessages",
                "-Duser.language=zh",
                "-Duser.country=CN",
                "-Djava.awt.headless=false"
            )
            
            // 包含所有必要的模块，特别是字符集相关模块
            modules(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.prefs",
                "java.xml",
                "jdk.unsupported",
                "jdk.charsets",
                "java.naming",
                "jdk.localedata",
                "jdk.crypto.ec"
            )
            
            windows {
                menuGroup = "QR Code Generator"
                // upgrade uuid for each new release
                upgradeUuid = "BF9CDA6A-1391-46D5-9ED5-383D6E68CCEB"
                console = false // 关闭控制台窗口，提供更好的用户体验
                iconFile.set(file("src/main/resources/icons/app_icon.ico"))
            }
            
            linux {
                iconFile.set(file("src/main/resources/icons/qrcode_icon_256x256.png"))
            }
            
            macOS {
                iconFile.set(file("src/main/resources/icon.icns"))
            }
        }
    }
}
