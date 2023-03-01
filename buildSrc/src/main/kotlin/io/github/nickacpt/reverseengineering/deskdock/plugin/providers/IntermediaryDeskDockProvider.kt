package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.MappingUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.nio.file.Path

object IntermediaryDeskDockProvider : DeskDockArtifactProvider() {
    override val classifier: String = "intermediary"

    override val addToProject: Boolean
        get() = true

    override fun provideArtifact(project: Project, newDependency: DependencyInfo): Path {
        val intermediaryFilePath = IntermediaryMappingProvider.provide(project)
        val originalJarPath = ProcessedOriginalDeskDockProvider.provide(project)

        val intermediary = MappingUtils.loadMappings(intermediaryFilePath)

        return project.workspace.repository.publishDependency(newDependency) {
            MappingUtils.remapFile(
                originalJarPath,
                intermediary,
                it,
                Constants.OFFICIAL_NAMESPACE,
                Constants.INTERMEDIARY_NAMESPACE
            )
        }
    }
}