package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.process.CommandLineArgumentProvider
import java.nio.file.Path
import kotlin.io.path.div

abstract class LaunchEnigmaTask : JavaExec() {
    @get:InputFile
    abstract var inputJarPath: Path

    @get:InputDirectory
    abstract var mappingsPath: Path

    @get:InputDirectory
    abstract var workDirPath: Path

    fun initClassPath() {
        val engima = project.workspace.enigmaDependencyInfo
        classpath = project.files(engima.resolve())
        workingDir = workDirPath.toFile()
    }

    init {
        group = Constants.DESKDOCK_TASK_GROUP
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