package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import java.nio.file.Path

abstract class EnigmaTask : JavaExec() {
    init {
        group = Constants.DESKDOCK_TASK_GROUP
    }

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
}