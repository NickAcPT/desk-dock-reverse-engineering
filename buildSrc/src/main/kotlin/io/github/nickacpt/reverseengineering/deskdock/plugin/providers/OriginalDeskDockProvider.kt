package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.DownloadUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.net.URL

object OriginalDeskDockProvider : DeskDockArtifactProvider {
    override fun provide(project: Project) {
        val type = project.workspace.type ?: throw Exception("Workspace has no type defined")
        val version = project.workspace.version ?: throw Exception("Workspace has no version defined")

        val zipFile = DownloadUtils.downloadFile(project, URL(type.getDownloadUrl(version)), "deskdock-${type}-${version}.zip")
    }
}