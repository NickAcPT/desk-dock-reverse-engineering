package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
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
                         ownerEntry: IndexEntryKey.ClassIndexEntry,
                         methodEntry: IndexEntryKey.MethodIndexEntry): Map<IndexEntryKey, T>? = null

}