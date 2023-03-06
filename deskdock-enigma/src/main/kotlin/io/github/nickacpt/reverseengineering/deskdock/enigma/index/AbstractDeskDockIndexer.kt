package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

abstract class AbstractDeskDockIndexer<T : Any> {

    open fun indexClass(clazz: ClassNode, cfrClazz: ClassFile): Map<IndexEntryKey, T>? = null

    open fun indexMethod(clazz: ClassNode,
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