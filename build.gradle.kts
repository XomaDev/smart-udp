plugins {
    kotlin("jvm") version "2.0.21"
    id("maven-publish")
}

group = "com.github.XomaDev"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupId
            artifactId = "SmartUDP"
            version = version

            from(components["kotlin"])
        }
    }
}
