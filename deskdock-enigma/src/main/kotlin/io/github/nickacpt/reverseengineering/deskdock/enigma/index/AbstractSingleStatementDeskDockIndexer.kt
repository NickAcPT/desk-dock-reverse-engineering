package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.benf.cfr.reader.util.MiscUtils
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

abstract class AbstractSingleStatementDeskDockIndexer<T : Any> : AbstractDeskDockIndexer<T>() {

    override fun indexMethod(
        clazz: ClassNode,
        method: MethodNode,
        cfrClazz: ClassFile,
        cfrMethod: Method,
        analysis: Op04StructuredStatement?,
        ownerEntry: IndexEntryKey.ClassIndexEntry,
        methodEntry: IndexEntryKey.MethodIndexEntry
    ): Map<IndexEntryKey, T>? {
        if (analysis == null || cfrMethod.isConstructor) return null

        val output = MiscStatementTools.linearise(analysis) ?: return null

        output.removeIf { statement ->
            statement is BeginBlock || statement is EndBlock || (statement is StructuredAssignment && statement.rvalue?.let {
                MiscUtils.isThis(it, cfrClazz.classType)
            } == true)
        }
        if (output.size != 1) return null

        val statement = output.first()
        val result = getValueForStatement(statement) ?: return null

        return mapOf(
            methodEntry to result
        )
    }

    protected abstract fun getValueForStatement(statement: StructuredStatement): T?
}