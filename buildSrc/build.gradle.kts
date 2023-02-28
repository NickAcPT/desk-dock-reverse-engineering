plugins {
    `java-gradle-plugin`

    kotlin("jvm") version "1.8.0"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

val pluginName = "desk-dock-reverse-engineering-plugin"
gradlePlugin {
    plugins.create(pluginName) {
        id = pluginName
        implementationClass = "io.github.nickacpt.reverseengineering.deskdock.plugin.DeskDockReverseEngineeringPlugin"
    }
}