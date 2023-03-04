plugins {
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://maven.quiltmc.org/repository/snapshot/")
}

dependencies {
    implementation("org.quiltmc:enigma:1.7.0-SNAPSHOT")

    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("org.ow2.asm:asm-util:9.3")
    implementation("org.benf:cfr:0.152")

    implementation("org.codehaus.janino:janino:3.1.9")
}