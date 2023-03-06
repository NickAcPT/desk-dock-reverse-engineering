package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools
import org.benf.cfr.reader.bytecode.analysis.parse.Expression
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractFieldVariable
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ExpressionStatement
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class PreferenceFieldDeskDockIndexer : AbstractDeskDockIndexer<String>() {

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

        // We want to match the following:
        // this.field = PreferenceManager.instance().getInt("PREF_KEY_SOMETHING");

        return output.mapNotNull { statement ->
            val assignment = statement as? StructuredAssignment ?: return@mapNotNull null

            val field = assignment.lvalue as? AbstractFieldVariable ?: return@mapNotNull null
            val value = getFunctionInvocation(assignment.rvalue) ?: return@mapNotNull null

            if (value.methodPrototype.owner.rawName != "com.floriandraschbacher.deskdock.server.class_81") {
                return@mapNotNull null
            }

            val keyName = ((value.args?.firstOrNull() as? Literal)?.value?.value as? String)?.removeSurrounding("\"")
                ?: return@mapNotNull null

            val properName = keyName.removePrefix("PREF_KEY_").split('_')
                .mapIndexed { index, s ->
                    if (index == 0) s.lowercase() else s.mapIndexed { i, c -> if (i == 0) c.titlecaseChar() else c.lowercaseChar() }
                        .joinToString("")
                }
                .joinToString("")

            IndexEntryKey.FieldIndexEntry(methodEntry.owner, field.fieldName, field.descriptor) to properName
        }.toMap()
    }

    private fun getFunctionInvocation(
        expr: Expression?
    ): AbstractFunctionInvokation? {
        return when (expr) {
            is AbstractFunctionInvokation -> {
                expr
            }
            is ExpressionStatement -> {
                getFunctionInvocation(expr.expression)
            }
            else -> {
                null
            }
        }
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