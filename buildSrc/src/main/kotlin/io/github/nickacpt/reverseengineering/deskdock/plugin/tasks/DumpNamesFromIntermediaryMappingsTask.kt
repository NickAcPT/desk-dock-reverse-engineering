package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryMappingProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.MappingUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.div

abstract class DumpNamesFromIntermediaryMappingsTask : DeskDockTaskBase() {

    @TaskAction
    fun dumpNames() {
        val intermediaryFilePath = IntermediaryMappingProvider.provide(project)
        val intermediary = MappingUtils.loadMappings(intermediaryFilePath) as MappingTree

        val outTree = MemoryMappingTree().apply {
            visitNamespaces(Constants.OFFICIAL_NAMESPACE, listOf(Constants.NAMED_NAMESPACE))
        }

        intermediary.classes.forEach { clazz ->
            val clazzIntermediaryName = clazz.getName(0)
            val clazzName = clazz.getName(intermediary.srcNamespace)
            if (clazzName.contains("/")) {
                outTree.visitClass(clazzIntermediaryName)
                outTree.visitDstName(MappedElementKind.CLASS, 0, clazzName)

                (clazz.methods + clazz.fields).forEach members@{
                    val memberIntermediaryName = it.getName(0)
                    val memberIntermediaryDesc = it.getDesc(0)

                    val memberName = it.getName(intermediary.srcNamespace)

                    val visitFunc = when (it) {
                        is MethodMapping -> outTree::visitMethod
                        is FieldMapping -> outTree::visitField
                        else -> throw Exception("Unknown element kind")
                    }

                    visitFunc(memberIntermediaryName, memberIntermediaryDesc)

                    val kind = when (it) {
                        is MethodMapping -> MappedElementKind.METHOD
                        is FieldMapping -> MappedElementKind.FIELD
                        else -> throw Exception("Unknown element kind")
                    }
                    outTree.visitDstName(kind, 0, memberName)
                }
            }
        }

        val writer = MappingWriter.create(
            project.rootDir.toPath() / Constants.MAPPINGS_FOLDER_NAME / project.workspace.type.toString(),
            MappingFormat.ENIGMA
        )

        writer.use {
            outTree.accept(it)
        }
    }
}