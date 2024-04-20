plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}


group = "com.cjcameron92.crytheria"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.auxilor.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")

}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("co.aikar:acf-paper:0.5.1-SNAPSHOT")


    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    compileOnly(fileTree("lib"))
//    implementation(fileTree("libs"))

}

tasks.shadowJar {
    minimize()
}

