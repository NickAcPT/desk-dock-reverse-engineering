package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.parse.Expression
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractFieldVariable
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn

class GetterSetterDeskDockIndexer :
    AbstractSingleStatementDeskDockIndexer<GetterSetterDeskDockIndexer.FieldAccessData>() {
    data class FieldAccessData(val fieldOwner: String, val fieldName: String, val isGetter: Boolean)

    private fun setterFieldResult(output: StructuredStatement): FieldAccessData? {
        val structuredAssignment = output as? StructuredAssignment ?: return null

        val fieldVariable = getFieldVariable(LValueExpression(structuredAssignment.lvalue)) ?: return null
        if (structuredAssignment.rvalue !is LValueExpression) return null

        return FieldAccessData(fieldVariable.owningClassType.rawName, fieldVariable.fieldName, false)
    }

    private fun getterFieldResult(output: StructuredStatement): FieldAccessData? {
        val structuredReturn = output as? StructuredReturn ?: return null

        val fieldVariable = getFieldVariable(structuredReturn.value) ?: return null
        return FieldAccessData(fieldVariable.owningClassType.rawName, fieldVariable.fieldName, true)
    }

    private fun getFieldVariable(expression: Expression): AbstractFieldVariable? {
        val lValueExpression = expression as? LValueExpression ?: return null
        return lValueExpression.lValue as? AbstractFieldVariable ?: return null
    }

    override fun getValueForStatement(statement: StructuredStatement): FieldAccessData? {
        return getterFieldResult(statement) ?: setterFieldResult(statement)
    }

    override fun proposeName(
        enigmaEntry: Entry<*>,
        remapper: EntryRemapper,
        indexEntry: IndexEntryKey,
        entry: FieldAccessData
    ): String? {

        if (!entry.isGetter && enigmaEntry is LocalVariableEntry && enigmaEntry.isArgument) {
            return entry.fieldName.takeUnless { it.startsWith("field_") }
        }

        val result =
            remapper.jarIndex.entryIndex.fields.firstOrNull { it.name == entry.fieldName }?.let {
                remapper.extendedDeobfuscate(it)
            }?.takeIf { !it.isObfuscated }?.value?.name ?: entry.fieldName.takeUnless { it.startsWith("field_") }

        return result
    }


}