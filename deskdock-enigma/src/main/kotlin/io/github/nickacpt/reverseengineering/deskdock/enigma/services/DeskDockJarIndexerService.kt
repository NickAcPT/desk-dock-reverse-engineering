package io.github.nickacpt.reverseengineering.deskdock.enigma.services

import cuchaz.enigma.analysis.index.JarIndex
import cuchaz.enigma.api.service.JarIndexerService
import cuchaz.enigma.classprovider.ClassProvider
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.AbstractDeskDockIndexer
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.GetterSetterDeskDockIndexer
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import io.github.nickacpt.reverseengineering.deskdock.enigma.utils.cfr.ClassNodeViewCfrClassSource
import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn
import org.benf.cfr.reader.state.DCCommonState
import org.benf.cfr.reader.util.getopt.OptionsImpl

object DeskDockJarIndexerService : JarIndexerService {
    private val cfrOptions = OptionsImpl(emptyMap())

    private val indexers = listOf<AbstractDeskDockIndexer<*>>(
            GetterSetterDeskDockIndexer()
    )

    private val gigaIndexResult = mutableMapOf<IndexEntryKey, MutableList<Any>>()

    private fun getEntryKeyIndexContent(key: IndexEntryKey): MutableList<Any> =
            gigaIndexResult.getOrPut(key) { mutableListOf() }

    override fun acceptJar(scope: Set<String>, classProvider: ClassProvider, jarIndex: JarIndex) {
        // Clear everything
        gigaIndexResult.clear()

        // Prepare everything
        val scopeNodes = scope.asSequence()
                .mapNotNull { classProvider.get(it) }
                .associateBy { it.name }

        val cfrSource = ClassNodeViewCfrClassSource(scopeNodes)
        val cfrState = DCCommonState(cfrOptions, cfrSource)

        // Do the indexing here
        scopeNodes.forEach { (name, clazzNode) ->
            val cfrClazz = cfrState.getClassFileMaybePath(name)
            val clazzIndexEntry = IndexEntryKey.ClassIndexEntry(name)

            indexers.forEach { indexer ->
                val results = mutableListOf<Map<IndexEntryKey, Any>?>()

                // We have to index the class
                results += indexer.indexClass(clazzNode, cfrClazz)

                if (clazzNode.name == "com/floriandraschbacher/deskdock/server/class_65") {
                    println()
                }

                // Now index the methods
                results += cfrClazz.methods.map { cfrMethod ->

                    if (cfrMethod.name == "method_160") {
                        val w = WildcardMatch()

                        val expr = w.getLValueWildCard("returnField")
                        val ret = StructuredReturn(BytecodeLoc.NONE, LValueExpression(expr), null)

                        MiscStatementTools.linearise(cfrMethod.analysis).map {
                            w.match(ret, it)
                        }
                    }

                    val methodNode = clazzNode.methods.first { it.name == cfrMethod.name && cfrMethod.methodPrototype.originalDescriptor == it.desc }
                    val methodIndexEntry = IndexEntryKey.MethodIndexEntry(clazzIndexEntry, cfrMethod.name, cfrMethod.methodPrototype.originalDescriptor)

                    indexer.indexMethod(clazzNode, methodNode, cfrClazz, cfrMethod, clazzIndexEntry, methodIndexEntry)
                }

                // Save all indexed results
                results.asSequence().filterNotNull().forEach {
                    it.forEach { (k, v) ->
                        getEntryKeyIndexContent(k).add(v)
                    }
                }
            }
        }
    }
}
