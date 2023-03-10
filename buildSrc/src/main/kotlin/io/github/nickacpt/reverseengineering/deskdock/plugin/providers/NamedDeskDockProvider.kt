package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.MappingUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.nio.file.Path

object NamedDeskDockProvider : DeskDockArtifactProvider() {
    override val classifier: String = "named"

    override val addToProject: Boolean
        get() = true

    override fun provideArtifact(project: Project, newDependency: DependencyInfo): Path {
        val intermediaryDependency = IntermediaryDeskDockProvider.provide(project)
        val mappings = NamedMappingProvider.provide(project)

        return project.workspace.repository.publishDependency(newDependency, override = false) {
            MappingUtils.remapFile(
                intermediaryDependency,
                mappings,
                it,
                Constants.INTERMEDIARY_NAMESPACE,
                Constants.NAMED_NAMESPACE
            )
        }
    }
}