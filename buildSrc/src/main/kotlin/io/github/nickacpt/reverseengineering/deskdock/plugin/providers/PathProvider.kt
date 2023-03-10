package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.div

val Project.intermediaryPath: Path
    get() = rootDir.toPath() / Constants.INTERMEDIARY_FOLDER_NAME

val Project.mappingsPath: Path
    get() = project.rootDir.toPath() / Constants.MAPPINGS_FOLDER_NAME