package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.analysis.index.EntryIndex
import cuchaz.enigma.translation.TranslateResult
import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

abstract class AbstractDeskDockIndexer<T : Any> {

    open fun indexClass(clazz: ClassNode, cfrClazz: ClassFile): Map<IndexEntryKey, T>? = null

    open fun indexMethod(
        clazz: ClassNode,
        method: MethodNode,
        cfrClazz: ClassFile,
        cfrMethod: Method,
        analysis: Op04StructuredStatement?,
        ownerEntry: IndexEntryKey.ClassIndexEntry,
        methodEntry: IndexEntryKey.MethodIndexEntry,
    ): Map<IndexEntryKey, T>? = null

    open fun proposeName(
        enigmaEntry: Entry<*>,
        remapper: EntryRemapper,
        indexEntry: IndexEntryKey,
        entry: T
    ): String? = null

}

object CfrUtils {
    fun getParameters(
        method: MethodEntry,
        index: EntryIndex,
    ): List<LocalVariableEntry> {
        var p = if (index.getMethodAccess(method)!!.isStatic) 0 else 1
        val result = mutableListOf<LocalVariableEntry>()

        method.desc.argumentDescs.forEach {
            p += it.size

            result.add(LocalVariableEntry(method, p, "", true, null))
        }

        return result
    }

    fun getDeobfuscatedParameters(
        method: MethodEntry,
        index: EntryIndex,
        remapper: EntryRemapper
    ): List<TranslateResult<LocalVariableEntry>> {
        var p = if (index.getMethodAccess(method)!!.isStatic) 0 else 1
        val result = mutableListOf<TranslateResult<LocalVariableEntry>>()

        method.desc.argumentDescs.forEach { paramDesc ->
            val param = LocalVariableEntry(method, p, "", true, null)
            p += paramDesc.size

            result.add(remapper.extendedDeobfuscate(param))
        }

        return result
    }

}