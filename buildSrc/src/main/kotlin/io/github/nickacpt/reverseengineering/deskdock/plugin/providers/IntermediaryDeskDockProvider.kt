package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.IntermediaryConstants.INTERMEDIARY_FOLDER_NAME
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.deskDockDependency
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.deskDockWorkspaceType
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div

object IntermediaryDeskDockProvider : DeskDockArtifactProvider() {
    override val classifier: String = "intermediary"

    override fun provideArtifact(project: Project): Path {
        val dependency = project.deskDockDependency
        val type = project.deskDockWorkspaceType

        // Create directories for the intermediary path
        val intermediaryPath = project.rootDir.toPath() / INTERMEDIARY_FOLDER_NAME / type.toString()
        intermediaryPath.createDirectories()



        TODO()
    }
}