package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.MappingUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import net.fabricmc.mappingio.adapter.MappingDstNsReorder
import net.fabricmc.mappingio.adapter.MappingNsCompleter
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.format.EnigmaReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.Project

object NamedMappingProvider {

    fun provide(project: Project): MemoryMappingTree {
        val mergedMappings = MemoryMappingTree()
        val intermediaryMappingsPath = IntermediaryMappingProvider.provide(project)

        // Load our intermediary mappings (Tiny V2 format)
        val intermediaryMappings = MappingUtils.loadMappings(intermediaryMappingsPath)

        // Load our named mappings (Enigma format)
        val namedMappings = MemoryMappingTree()
        EnigmaReader.read(
            project.workspace.type.getMappingsDirectory(project),
            Constants.INTERMEDIARY_NAMESPACE,
            Constants.NAMED_NAMESPACE,
            MappingSourceNsSwitch(namedMappings, Constants.INTERMEDIARY_NAMESPACE)
        )

        arrayOf(intermediaryMappings, namedMappings).forEach {
            it.accept(mergedMappings)
        }

        val outputTree = MemoryMappingTree()

        val completer = MappingNsCompleter(outputTree, mapOf("named" to "intermediary"))
        val namedDstNs = MappingDstNsReorder(completer, "named")
        val intermediarySrcNs = MappingSourceNsSwitch(namedDstNs, "intermediary")

        mergedMappings.accept(intermediarySrcNs)

        return outputTree
    }
}