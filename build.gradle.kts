import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.task.NodeSetupTask
import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("com.github.node-gradle.node") version "7.1.0"
    id("com.diffplug.spotless") version "8.0.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    archiveFileName.set("app.jar")
}

node {
    version = "22.17.0"
    download = true
}

spotless {
    fun isWindows() =
        org.gradle.internal.os.OperatingSystem
            .current()
            .isWindows

    fun resolveExecutable(command: String) = if (isWindows()) "bin/$command.exe" else "bin/$command"

    fun resolveNodeExecutable() = "${tasks.named<NodeSetupTask>("nodeSetup").get().nodeDir.get()}/${resolveExecutable("node")}"

    fun resolveNpmExecutable() = "${tasks.named<NpmSetupTask>("npmSetup").get().npmDir.get()}/${resolveExecutable("npm")}"

    val defaultTargetExcludes =
        listOf(
            ".git/**",
            ".gradle/**",
            ".idea/**",
            "bin/**",
            "build/**",
            "gradle/**",
            "frontend/.nuxt/**",
            "frontend/.output/**",
            "frontend/dist/**",
            "frontend/node_modules/**",
            "src/main/resources/static/**",
        )
    val prettierVersion = "prettier" to "3.6.2"
    val prettierPluginShVersion = "prettier-plugin-sh" to "0.18.0"

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
        prettier(mapOf(prettierVersion))
            .nodeExecutable(resolveNodeExecutable())
            .npmExecutable(resolveNpmExecutable())
        endWithNewline()
    }
    format("sh") {
        target("**/Dockerfile", "**/*.env", "**/.gitignore", "**/*.sh")
        targetExclude(defaultTargetExcludes)
        prettier(mapOf(prettierVersion, prettierPluginShVersion))
            .nodeExecutable(resolveNodeExecutable())
            .npmExecutable(resolveNpmExecutable())
            .config(
                mapOf(
                    "plugins" to listOf("prettier-plugin-sh"),
                    "indent" to 4,
                ),
            )
        trimTrailingWhitespace()
        endWithNewline()
    }

    tasks.configureEach {
        when (name) {
            "spotlessPrettier", "spotlessSh" ->
                dependsOn(
                    tasks.named("nodeSetup"),
                )
        }
    }
}
