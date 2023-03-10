package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.WorkspaceType
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.HashUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.ModifiedGenState
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import net.fabricmc.stitch.representation.JarReader
import net.fabricmc.stitch.representation.JarRootEntry
import org.gradle.api.Project
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.*

object IntermediaryMappingProvider {
    private fun intermediaryName(hash: String) = "${hash}.tiny"

    fun provide(project: Project): Path {
        val type = project.workspace.type

        // Create directories for the intermediary path
        val intermediaryDirPath = project.intermediaryPath / type.toString()
        intermediaryDirPath.createDirectories()

        val originalJarPath = ProcessedOriginalDeskDockProvider.provide(project)
        val hash = HashUtils.getFileHash(OriginalDeskDockProvider.provide(project))

        val intermediaryFilePath = intermediaryDirPath / intermediaryName(hash)

        if (intermediaryFilePath.notExists()) {
            // We don't have an intermediary for the current version
            buildIntermediary(
                project,
                intermediaryDirPath,
                hash,
                type,
                originalJarPath,
                intermediaryFilePath
            )
        }

        return intermediaryFilePath
    }

    @OptIn(ExperimentalPathApi::class)
    private fun buildIntermediary(
        project: Project,
        intermediaryPath: Path,
        hash: String,
        type: WorkspaceType,
        originalJarPath: Path,
        outputIntermediary: Path
    ) {
        val count = intermediaryPath.walk().count()
        if (count == 0) {
            // We don't have any intermediaries at all, we have to generate the first version
            val workspace = project.workspace

            val state = ModifiedGenState()

            val jarEntry = JarRootEntry(originalJarPath.toFile())
            try {
                val reader = JarReader(jarEntry)
                reader.apply()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            state.setTargetNamespace("com/floriandraschbacher/deskdock/${type}/")
            workspace.intermediaryObfuscationPattern?.also {
                state.addObfuscatedPattern(it)
            }

            state.generate(outputIntermediary.toFile(), jarEntry, null)
        } else {
            // Intermediaries exist, but we have to match stuff to update the old one
            throw Exception("Please generate intermediaries for version $hash")
        }
    }

}