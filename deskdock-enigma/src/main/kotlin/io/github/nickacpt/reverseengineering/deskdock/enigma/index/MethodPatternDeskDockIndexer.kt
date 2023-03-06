package io.github.nickacpt.reverseengineering.deskdock.enigma.index

import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement
import org.benf.cfr.reader.bytecode.analysis.parse.Expression
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.Wildcard
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.entities.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class MethodPatternDeskDockIndexer<T : Any>(val patterns: WildcardMatch.() -> List<Wildcard<out Expression>>) : AbstractDeskDockIndexer<T>() {

    inner class ExpressionVisitor() : AbstractExpressionRewriter() {
        override fun rewriteExpression(expression: Expression?, ssaIdentifiers: SSAIdentifiers<*>?, statementContainer: StatementContainer<*>?, flags: ExpressionRewriterFlags?): Expression {
            return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags)
        }
    }

    override fun indexMethod(
        clazz: ClassNode,
        method: MethodNode,
        cfrClazz: ClassFile,
        cfrMethod: Method,
        analysis: Op04StructuredStatement?,
        ownerEntry: IndexEntryKey.ClassIndexEntry,
        methodEntry: IndexEntryKey.MethodIndexEntry
    ): Map<IndexEntryKey, T>? {
        val wildcardMatch = WildcardMatch()

        return super.indexMethod(clazz, method, cfrClazz, cfrMethod, analysis, ownerEntry, methodEntry)
    }
}