package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.fabricmc.tinyremapper.NonClassCopyMode
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import java.io.IOException
import java.nio.file.Path

object MappingUtils {
    fun loadMappings(path: Path): MemoryMappingTree {
        return MemoryMappingTree().apply { MappingReader.read(path, this) }
    }

    fun remapFile(input: Path, mappings: MappingTree, output: Path, sourceNs: String, destNs: String) {
        val remapper = TinyRemapper.newRemapper()
            .withMappings(TinyRemapperHelper.create(mappings, sourceNs, destNs, true))
            .fixPackageAccess(true)
            .rebuildSourceFilenames(true)
            .renameInvalidLocals(true)
            .resolveMissing(true)
            .build()

        try {
            OutputConsumerPath.Builder(output).build().use { outputConsumer ->
                outputConsumer.addNonClassFiles(input, NonClassCopyMode.FIX_META_INF, remapper)
                remapper.readInputs(input)
                remapper.apply(outputConsumer)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            remapper.finish()
        }
    }
}