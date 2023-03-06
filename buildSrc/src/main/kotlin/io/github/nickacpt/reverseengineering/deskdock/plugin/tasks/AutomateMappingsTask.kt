package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.AsmUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.MappingUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.format.MappingFormat
import org.gradle.api.tasks.TaskAction

abstract class AutomateMappingsTask : DeskDockTaskBase() {
    @TaskAction
    fun automate() {
        val mappingsDir = project.workspace.type.getMappingsDirectory(project)
        val intermediaryNodes = AsmUtils.viewJarAsNodes(IntermediaryDeskDockProvider.provide(project))

        val mappings = MappingUtils.loadMappings(mappingsDir)

        intermediaryNodes.forEach { (_, node) ->
            if (node.superName == "com/floriandraschbacher/deskdock/server/class_39") {
                mappings.visitClass(node.name)

                val name =
                    "com/floriandraschbacher/deskdock/server/logic/synergy/packets/" + node.name.removePrefix("com/floriandraschbacher/deskdock/server/")

                mappings.visitDstName(MappedElementKind.CLASS, 0, name)
            }
        }

        MappingWriter.create(mappingsDir, MappingFormat.ENIGMA).use {
            mappings.accept(it)
        }
    }

}