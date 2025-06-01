import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.t0xodile"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.portswigger.burp.extensions:montoya-api:2025.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.json:json:20250517")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}