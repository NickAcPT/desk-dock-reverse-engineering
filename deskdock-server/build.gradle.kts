import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy
import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy.DirectlyInvoke

plugins {
    id("desk-dock-reverse-engineering-plugin")
}

repositories {
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://maven.quiltmc.org/repository/snapshot/")
}

dependencies {
    deskdock("com.floriandraschbacher:deskdock:1.3.1:server")

    enigma("org.quiltmc:enigma-swing:1.7.0-SNAPSHOT")
    enigmaDep("org.quiltmc:enigma-cli:1.7.0-SNAPSHOT")
    enigmaDep("net.fabricmc:name-proposal:0.1.4")
    enigmaDep("org.quiltmc:quilt-enigma-plugin:1.2.1")
}

workspace {
    intermediaryObfuscationPattern = ".+"

    strippedPackages = listOf(
            "com/github/kwhat/jnativehook",
            "com/intellij/uiDesigner"
    )

    stringDecryptionStrategy = StringDecryptionStategy.MultipleInvoke(listOf(
            DirectlyInvoke(project,
                    "com/floriandraschbacher/deskdock/server/class_9",
                    "method_26",
                    "([I)Ljava/lang/String;"),
            DirectlyInvoke(project,
                    "com/floriandraschbacher/deskdock/server/class_18",
                    "method_74",
                    "([I)Ljava/lang/String;"),
    ))
}