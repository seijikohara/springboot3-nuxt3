import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.task.NodeSetupTask
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.gradle.node)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.spotless)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)

    developmentOnly(libs.spring.boot.devtools)

    annotationProcessor(libs.spring.boot.configuration.processor)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(platform(libs.kotest.bom))
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.extensions.spring)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.mockk)
                implementation(libs.reactor.test)
                implementation(libs.spring.boot.starter.test)
            }
        }
    }
}

tasks {
    withType<BootJar> {
        archiveFileName.set("app.jar")
    }
}

node {
    version = "24.11.0"
    download = true
}

spotless {
    val isWindows =
        org.gradle.internal.os.OperatingSystem
            .current()
            .isWindows
    val executable: (String) -> String = { if (isWindows) "$it.exe" else "bin/$it" }
    val nodeExecutable by lazy { "${tasks.named<NodeSetupTask>("nodeSetup").get().nodeDir.get()}/${executable("node")}" }
    val npmExecutable by lazy { "${tasks.named<NpmSetupTask>("npmSetup").get().npmDir.get()}/${executable("npm")}" }
    val prettier = "prettier" to "3.6.2"
    val prettierPluginSh = "prettier-plugin-sh" to "0.18.0"
    val defaultTargetExcludes =
        listOf(
            ".git/**",
            ".gradle/**",
            ".idea/**",
            "bin/**",
            "build/**",
            "gradle/**",
            "frontend/**",
            "src/main/resources/static/**",
        )

    kotlin {
        target("**/*.kt")
        targetExclude(defaultTargetExcludes)
        toggleOffOn()
        ktlint()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude(defaultTargetExcludes)
        toggleOffOn()
        ktlint()
    }
    format("prettier") {
        target("**/*.json", "**/*.js", "**/*.md", "**/*.yml", "**/*.yaml")
        targetExclude(defaultTargetExcludes)
        prettier(mapOf(prettier))
            .nodeExecutable(nodeExecutable)
            .npmExecutable(npmExecutable)
        endWithNewline()
    }
    format("sh") {
        target("**/Dockerfile", "**/*.env", "**/.gitignore", "**/*.sh")
        targetExclude(defaultTargetExcludes)
        prettier(mapOf(prettier, prettierPluginSh))
            .nodeExecutable(nodeExecutable)
            .npmExecutable(npmExecutable)
            .config(
                mapOf(
                    "plugins" to listOf("prettier-plugin-sh"),
                    "indent" to 4,
                ),
            )
        trimTrailingWhitespace()
        endWithNewline()
    }

    listOf("spotlessPrettier", "spotlessSh").forEach { taskName ->
        tasks.named(taskName) {
            dependsOn(tasks.named("nodeSetup"))
        }
    }
}
