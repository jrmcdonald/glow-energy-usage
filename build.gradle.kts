import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.detekt)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    jacoco
}

group = "com.jrmcdonald"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.http4k.dependencies))
    implementation(platform(libs.junit.dependencies))

    implementation(kotlin("stdlib"))
    implementation(libs.clikt)
    implementation(libs.apache.commons.csv)
    implementation(libs.bundles.http4k)
    implementation(libs.koin.core)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.koin.junit5)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.mockwebserver)
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.jrmcdonald.energy.usage.AppKt"
    }
}

tasks.named("build") { dependsOn("shadowJar") }
tasks.named("check") { dependsOn("detekt") }
