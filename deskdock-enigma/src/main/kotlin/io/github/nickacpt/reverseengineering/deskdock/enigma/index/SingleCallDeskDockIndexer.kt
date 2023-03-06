package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement

class SingleCallDeskDockIndexer : AbstractSingleStatementDeskDockIndexer<SingleCallDeskDockIndexer.MethodCallData>() {
    data class MethodCallData(val methodName: String, val methodDesc: String)

    override fun getValueForStatement(statement: StructuredStatement): MethodCallData? {
        val exprStatement = statement as? StructuredExpressionStatement ?: return null
        val invocation = exprStatement.expression as AbstractFunctionInvokation

        return MethodCallData(invocation.name, invocation.methodPrototype.originalDescriptor)
    }

    override fun proposeName(
        enigmaEntry: Entry<*>,
        remapper: EntryRemapper,
        indexEntry: IndexEntryKey,
        entry: MethodCallData
    ): String? {
        val entryIndex = remapper.jarIndex.entryIndex
        val targetMethod =
            entryIndex.methods.firstOrNull { it.name == entry.methodName && it.desc.toString() == entry.methodDesc }
                ?: return null

        if (enigmaEntry is LocalVariableEntry) {
            val targetParams =
                CfrUtils.getParameters(targetMethod, entryIndex, remapper).filter { it.isDeobfuscated }
                    .takeIf { it.isNotEmpty() }
                    ?: return null

            val param = targetParams.firstOrNull { r ->
                r.value.index == enigmaEntry.index.let {
                    it + if (enigmaEntry.parent?.let { m -> entryIndex.getMethodAccess(m)?.isStatic } == true) {
                        1
                    } else 0
                }
            } ?: return null

            return param.value.name
        } else if (enigmaEntry is MethodEntry) {
            return remapper.extendedDeobfuscate(targetMethod).takeIf { it.isDeobfuscated }?.value?.name
        }

        return null
    }
}