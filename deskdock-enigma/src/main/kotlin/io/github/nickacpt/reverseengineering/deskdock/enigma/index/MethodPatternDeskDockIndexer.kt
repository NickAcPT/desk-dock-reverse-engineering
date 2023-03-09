package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import io.github.nickacpt.reverseengineering.deskdock.enigma.pattern.MethodPatternManager
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class MethodPatternDeskDockIndexer : AbstractDeskDockIndexer<String>() {
    override fun indexMethod(
        clazz: ClassNode,
        method: MethodNode,
        cfrClazz: ClassFile,
        cfrMethod: Method,
        analysis: Op04StructuredStatement?,
        ownerEntry: IndexEntryKey.ClassIndexEntry,
        methodEntry: IndexEntryKey.MethodIndexEntry
    ): Map<IndexEntryKey, String> {
        val resultMap = mutableMapOf<IndexEntryKey, String>()

        MethodPatternManager.patterns.forEach {
            if (!it.matches(clazz, method)) return@forEach

            if (it.name != null) {
                resultMap[methodEntry] = it.name
            }

            if (it.parameters != null) {
                val methodParameters = Type.getArgumentTypes(method.desc)

                it.parameters.forEachIndexed { index, parameter ->
                    if (parameter?.type != null && methodParameters.getOrNull(index)?.internalName == parameter.type && parameter.name != null) {
                        resultMap[IndexEntryKey.MethodParameterIndexEntry(methodEntry, index, "")] = parameter.name
                    }
                }
            }
        }

        return resultMap
    }

    override fun proposeName(
        enigmaEntry: Entry<*>,
        remapper: EntryRemapper,
        indexEntry: IndexEntryKey,
        entry: String
    ): String {
        return entry
    }
}