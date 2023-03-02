package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.DeskDockWorkspaceExtension
import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy
import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptor
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.AsmUtils
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

            // Decrypt strings
            decryptStrings(workspace, it)

            // Strip requested packages
            workspace.strippedPackages.forEach { pkg ->
                ZipUtils.stripPackage(it, pkg)
            }
        }
    }

    private fun decryptStrings(workspace: DeskDockWorkspaceExtension, it: Path) {
        val strategy = workspace.stringDecryptionStrategy

        if (strategy is StringDecryptionStategy.None) return

        @Suppress("UNCHECKED_CAST")
        val decryptor = (strategy.decryptor as StringDecryptor<StringDecryptionStategy>)

        val originalNodes = AsmUtils.viewJarAsNodes(it)
        decryptor.prepareNodes(originalNodes, strategy)

        AsmUtils.updateJarClasses(it) { clazz ->
            decryptor.prepareClass(originalNodes, clazz, strategy)

            clazz.methods.map { m ->
                decryptor.decryptStrings(clazz, m, strategy)
            }.any { it }
        }
    }

}