import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "dev.phpdollar"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

// PhpStorm 2026.2 runs on JBR 25.
kotlin { jvmToolchain(25) }

dependencies {
    intellijPlatform {
        // -PlocalIde=/path/to/PhpStorm uses an installed IDE (no download); otherwise PhpStorm is downloaded.
        val localIde = providers.gradleProperty("localIde").orNull
        if (localIde != null) local(localIde) else phpstorm(providers.gradleProperty("platformVersion").get())
        bundledPlugin("com.jetbrains.php")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262"
            untilBuild = provider { null }
        }
    }
    pluginVerification {
        ides {
            val localIde = providers.gradleProperty("localIde").orNull
            if (localIde != null) local(localIde) else recommended()
        }
    }
}
