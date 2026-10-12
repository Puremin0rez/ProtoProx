plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.ktfmt)
}

version = providers.gradleProperty("mod_version").get()

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

kotlin {
    // Minecraft 1.20.1's Java version
    jvmToolchain(17)
}

ktfmt {
    kotlinLangStyle()
    maxWidth = 120
}

dependencies {
    // provided by Minecraft and the loaders
    compileOnly(libs.netty.codec)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.mixin)
    compileOnly(libs.asm.tree)

    implementation(libs.netty.codec.haproxy) { isTransitive = false }

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.netty.codec)
    testRuntimeOnly(libs.slf4j.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    wrapper {
        gradleVersion = "9.8.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    processResources {
        val properties = mapOf("version" to version)
        inputs.properties(properties)
        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) { expand(properties) }
    }

    test { useJUnitPlatform() }

    jar { archiveClassifier = "dev" }

    // Release jar, with the Kotlin stdlib and HAProxy codec relocated inside it
    shadowJar {
        archiveClassifier = ""
        relocate("kotlin", "com.dethemium.protoprox.shadow.kotlin")
        relocate("io.netty.handler.codec.haproxy", "com.dethemium.protoprox.shadow.haproxy")
        dependencies { exclude(dependency("org.jetbrains:annotations")) }
        addMultiReleaseAttribute = false
        manifest { attributes("Automatic-Module-Name" to "com.dethemium.protoprox") }
        exclude(
            "META-INF/versions/**",
            "META-INF/maven/**",
            "META-INF/native-image/**",
            "META-INF/*.kotlin_module",
            "META-INF/io.netty.versions.properties",
            "**/module-info.class",
            "DebugProbesKt.bin",
        )
    }
}
