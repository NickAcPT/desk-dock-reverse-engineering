plugins {
    `java-gradle-plugin`

    kotlin("jvm") version "1.8.0"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("net.fabricmc:stitch:0.6.2")
    implementation("net.fabricmc:mapping-io:0.3.0")
    implementation("net.fabricmc:tiny-remapper:0.8.6")
}

val pluginName = "desk-dock-reverse-engineering-plugin"
gradlePlugin {
    plugins.create(pluginName) {
        id = pluginName
        implementationClass = "io.github.nickacpt.reverseengineering.deskdock.plugin.DeskDockReverseEngineeringPlugin"
    }
}