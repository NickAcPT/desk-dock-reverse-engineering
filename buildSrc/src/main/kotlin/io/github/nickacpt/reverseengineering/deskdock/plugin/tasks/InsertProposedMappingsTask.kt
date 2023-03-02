package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import org.gradle.process.CommandLineArgumentProvider
import kotlin.io.path.div

abstract class InsertProposedMappingsTask : EnigmaCommandTask() {
    init {
        argumentProviders.add(CommandLineArgumentProvider {
            listOf(
                    "insert-proposed-mappings",

                    // input jar
                    inputJarPath.toAbsolutePath().toString(),

                    // source and target
                    mappingsPath.toAbsolutePath().toString(),
                    mappingsPath.toAbsolutePath().toString(),

                    // format
                    "enigma",

                    (workDirPath / "enigma_profile.json").toAbsolutePath().toString()
            )
        })

    }
}