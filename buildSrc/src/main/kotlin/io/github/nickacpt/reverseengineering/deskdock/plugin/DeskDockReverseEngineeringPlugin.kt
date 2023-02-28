package io.github.nickacpt.reverseengineering.deskdock.plugin

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.WorkspaceExtension
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.OriginalDeskDockProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class DeskDockReverseEngineeringPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create<WorkspaceExtension>("workspace")

        target.afterEvaluate {
            OriginalDeskDockProvider.provide(this)
        }
    }
}