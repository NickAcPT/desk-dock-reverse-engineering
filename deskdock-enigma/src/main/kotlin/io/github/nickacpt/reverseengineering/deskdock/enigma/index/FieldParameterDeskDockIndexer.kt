package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractFieldVariable
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class FieldParameterDeskDockIndexer : AbstractDeskDockIndexer<String>() {

    override fun indexMethod(
        clazz: ClassNode,
        method: MethodNode,
        cfrClazz: ClassFile,
        cfrMethod: Method,
        analysis: Op04StructuredStatement?,
        ownerEntry: IndexEntryKey.ClassIndexEntry,
        methodEntry: IndexEntryKey.MethodIndexEntry
    ): Map<IndexEntryKey, String>? {
        val output = MiscStatementTools.linearise(analysis ?: return null) ?: return null

        return output.mapNotNull { statement ->
            val assignment = statement as? StructuredAssignment ?: return@mapNotNull null

            val field = assignment.lvalue as? AbstractFieldVariable ?: return@mapNotNull null
            val argument = (assignment.rvalue as? LValueExpression)?.lValue as? LocalVariable ?: return@mapNotNull null

            IndexEntryKey.MethodParameterIndexEntry(methodEntry, argument.idx, "") to field.fieldName
        }.toMap()
    }

    override fun proposeName(
        enigmaEntry: Entry<*>,
        remapper: EntryRemapper,
        indexEntry: IndexEntryKey,
        entry: String
    ): String? {
        if (enigmaEntry !is LocalVariableEntry) return null

        val field = remapper.jarIndex.entryIndex.fields.firstOrNull { it.name == entry } ?: return null

        return remapper.extendedDeobfuscate(field).takeIf { it.isDeobfuscated }?.value?.name
    }
}