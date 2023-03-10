package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.DownloadUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.fetchOrGetCachedFile
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

object OriginalDeskDockProvider : DeskDockArtifactProvider() {

    override val classifier: String
        get() = Constants.DESKDOCK_ORIGINAL_CLASSIFIER

    @OptIn(ExperimentalPathApi::class)
    override fun provideArtifact(project: Project, newDependency: DependencyInfo): Path {
        val dependency = project.workspace.deskDockDependencyInfo
        val type = project.workspace.type

        check(dependency.version != null) { "Artifact version cannot be missing!" }
        val version = dependency.version

        return project.fetchOrGetCachedFile("downloads", "deskdock-${type}-${version}.jar") { outPath ->
            val zipFile =
                DownloadUtils.downloadFile(
                    project,
                    URL(type.getDownloadUrl(version)),
                    "deskdock-${type}-${version}.zip"
                )

            FileSystems.newFileSystem(zipFile).use { fs ->
                val jarPath = fs.getPath("/").walk().firstOrNull {
                    it.extension == "jar"
                } ?: throw Exception("Unable to find DeskDock jar inside zip")

                outPath.writeBytes(jarPath.readBytes())
            }
        }
    }
}