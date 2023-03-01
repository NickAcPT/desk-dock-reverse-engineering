package io.github.nickacpt.reverseengineering.deskdock.plugin

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.DeskDockWorkspaceExtension
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

class DeskDockReverseEngineeringPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Apply Java plugin
        target.apply(plugin = "java")

        // Initialize our workspace
        val extension = target.extensions.create<DeskDockWorkspaceExtension>(Constants.DESKDOCK_EXTENSION_NAME)
        with(extension) {
            target.initWorkspace()
        }

        target.afterEvaluate {
            with(extension) {
                target.initAfterEvaluate()
            }
            // First, provide the original deskdock jar
            IntermediaryDeskDockProvider.provide(this)
        }
    }
}