package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import org.gradle.process.CommandLineArgumentProvider
import kotlin.io.path.div

abstract class LaunchEnigmaTask : EnigmaTask() {

    init {
        mainClass.set("cuchaz.enigma.gui.Main")

        argumentProviders.add(CommandLineArgumentProvider {
            listOf(
                "--jar",
                inputJarPath.toAbsolutePath().toString(),

                "--mappings",
                mappingsPath.toAbsolutePath().toString(),

                "--profile",
                (workDirPath / "enigma_profile.json").toAbsolutePath().toString()
            )
        })

    }

}