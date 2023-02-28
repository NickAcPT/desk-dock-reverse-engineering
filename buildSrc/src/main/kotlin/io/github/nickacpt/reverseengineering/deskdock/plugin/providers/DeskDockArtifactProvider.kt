package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import org.gradle.api.Project
import java.nio.file.Path

interface DeskDockArtifactProvider {
    fun provide(project: Project): Path
}