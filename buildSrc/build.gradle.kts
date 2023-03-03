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
    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("org.ow2.asm:asm-util:9.3")
    implementation("org.benf:cfr:0.152")
    implementation("org.codehaus.janino:janino:3.1.9")

    implementation("net.fabricmc:stitch:0.6.2")
    implementation("net.fabricmc:mapping-io:0.3.0")
    implementation("net.fabricmc:tiny-remapper:0.8.6")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
}

val pluginName = "desk-dock-reverse-engineering-plugin"
gradlePlugin {
    plugins.create(pluginName) {
        id = pluginName
        implementationClass = "io.github.nickacpt.reverseengineering.deskdock.plugin.DeskDockReverseEngineeringPlugin"
    }
}