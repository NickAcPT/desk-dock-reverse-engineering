package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.WorkspaceType
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.DownloadUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.ConfigurationConstants.DESKDOCK_CONFIGURATION_NAME
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.DependencyConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.DependencyConstants.DESKDOCK_ARTIFACT
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.DependencyConstants.DESKDOCK_GROUP
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.fetchOrGetCachedFile
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.getDependencyInfo
import org.gradle.api.Project
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

object OriginalDeskDockProvider : DeskDockArtifactProvider() {

    override val classifier: String
        get() = DependencyConstants.DESKDOCK_ORIGINAL_CLASSIFIER

    @OptIn(ExperimentalPathApi::class)
    override fun provideArtifact(project: Project): Path {
        val dependency = project.getDependencyInfo(DESKDOCK_CONFIGURATION_NAME)

        check(dependency.group == DESKDOCK_GROUP) { "Expected deskdock group to be $DESKDOCK_GROUP." }
        check(dependency.classifier != DESKDOCK_ARTIFACT) { "Expected deskdock group to be $DESKDOCK_ARTIFACT." }
        check(dependency.version != null) { "Artifact version cannot be missing!" }

        val version = dependency.version
        val type = WorkspaceType.values().firstOrNull { it.name.equals(dependency.classifier, true) }

        check(type != null) {
            "Expected artifact classifier to be any of [${WorkspaceType.values().joinToString { it.toString() }}]"
        }

        val zipFile =
            DownloadUtils.downloadFile(project, URL(type.getDownloadUrl(version)), "deskdock-${type}-${version}.zip")

        return project.fetchOrGetCachedFile("downloads", "deskdock-${type}-${version}.jar") { outPath ->
            FileSystems.newFileSystem(zipFile).use { fs ->
                val jarPath = fs.getPath("/").walk().firstOrNull {
                    it.extension == "jar"
                } ?: throw Exception("Unable to find DeskDock jar inside zip")

                outPath.writeBytes(jarPath.readBytes())
            }
        }
    }
}