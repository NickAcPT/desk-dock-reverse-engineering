package io.github.nickacpt.reverseengineering.deskdock.plugin.model

import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.mappingsPath
import org.gradle.api.Project
import kotlin.io.path.createDirectories
import kotlin.io.path.div

enum class WorkspaceType {
    SERVER {
        override fun getDownloadUrl(version: String): String {
            return "https://files.fdmobileinventions.com/DeskDockServer/${version}/DeskDockServer_${version}.zip"
        }
    };

    fun getMappingsDirectory(project: Project) =
        (project.mappingsPath / toString()).createDirectories()

    abstract fun getDownloadUrl(version: String): String

    override fun toString(): String {
        return name.lowercase()
    }
}