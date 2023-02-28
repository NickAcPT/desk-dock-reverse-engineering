package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.HashUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants.INTERMEDIARY_FOLDER_NAME
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import net.fabricmc.stitch.commands.CommandGenerateIntermediary
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.*

object IntermediaryDeskDockProvider : DeskDockArtifactProvider() {
    override val classifier: String = "intermediary"

    private fun intermediaryName(hash: String) = "${hash}.tiny"

    override fun provideArtifact(project: Project): Path {
        val dependency = project.workspace.deskDockDependencyInfo
        val type = project.workspace.type

        // Create directories for the intermediary path
        val intermediaryPath = project.rootDir.toPath() / INTERMEDIARY_FOLDER_NAME / type.toString()
        intermediaryPath.createDirectories()

        val originalJarPath = OriginalDeskDockProvider.provide(project)
        val hash = HashUtils.getFileHash(originalJarPath)

        val intermediaryFilePath = intermediaryPath / intermediaryName(hash)

        if (intermediaryFilePath.notExists()) {
            // We don't have an intermediary for the current version
            buildIntermediary(intermediaryPath, hash, originalJarPath, intermediaryFilePath)
        }

        TODO()
    }

    @OptIn(ExperimentalPathApi::class)
    private fun buildIntermediary(
        intermediaryPath: Path,
        hash: String,
        originalJarPath: Path,
        outputIntermediary: Path) {
        val count = intermediaryPath.walk().count()
        if (count == 0) {
            // We don't have any intermediaries at all, we have to generate the first version
            object : CommandGenerateIntermediary() {}
        } else {
            // Intermediaries exist, but we have to match stuff to update the old one
            throw Exception("Please generate intermediaries for version ${hash}")
        }
    }
}