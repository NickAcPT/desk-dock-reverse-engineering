package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import org.gradle.api.Project

interface DeskDockArtifactProvider {
    fun provide(project: Project)
}