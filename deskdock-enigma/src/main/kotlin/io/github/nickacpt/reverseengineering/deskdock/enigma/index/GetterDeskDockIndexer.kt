package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractFieldVariable
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class GetterDeskDockIndexer : AbstractDeskDockIndexer<GetterDeskDockIndexer.GetterFieldAccessData>() {
    data class GetterFieldAccessData(val fieldOwner: String, val fieldName: String)

    override fun indexMethod(
        clazz: ClassNode,
        method: MethodNode,
        cfrClazz: ClassFile,
        cfrMethod: Method,
        analysis: Op04StructuredStatement?,
        ownerEntry: IndexEntryKey.ClassIndexEntry,
        methodEntry: IndexEntryKey.MethodIndexEntry
    ): Map<IndexEntryKey, GetterFieldAccessData>? {
        if (analysis == null) return null

        val output = MiscStatementTools.linearise(analysis) ?: return null

        output.removeIf { it is BeginBlock || it is EndBlock }
        if (output.size != 1) return null

        val structuredReturn = output.first() as? StructuredReturn ?: return null

        val returnedValue = structuredReturn.value
        val expression = returnedValue as? LValueExpression ?: return null
        val fieldVariable = expression.lValue as? AbstractFieldVariable ?: return null

        return mapOf(
            methodEntry to GetterFieldAccessData(fieldVariable.owningClassType.rawName, fieldVariable.fieldName)
        )
    }

    override fun proposeName(
        enigmaEntry: Entry<*>,
        remapper: EntryRemapper,
        indexEntry: IndexEntryKey,
        entry: GetterFieldAccessData
    ): String? {

        val field =
            remapper.jarIndex.entryIndex.fields.firstOrNull { it.name == entry.fieldName }

        val result = remapper.extendedDeobfuscate(field)
        if (result.isObfuscated) return null

        return result.value?.name
    }
}