package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.ZipUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.copyTo

object ProcessedOriginalDeskDockProvider : DeskDockArtifactProvider() {
    override val classifier: String = "original-processed"

    override fun provideArtifact(project: Project, newDependency: DependencyInfo): Path {
        val workspace = project.workspace

        return workspace.repository.publishDependency(newDependency) {
            OriginalDeskDockProvider.provide(project).copyTo(it)

            // Strip requested packages
            workspace.strippedPackages.forEach { pkg ->
                ZipUtils.stripPackage(it, pkg)
            }
        }
    }

}