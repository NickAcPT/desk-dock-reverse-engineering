package io.github.nickacpt.reverseengineering.deskdock.enigma.services

import cuchaz.enigma.analysis.index.JarIndex
import cuchaz.enigma.api.service.JarIndexerService
import cuchaz.enigma.classprovider.ClassProvider
import io.github.nickacpt.reverseengineering.deskdock.enigma.utils.cfr.ClassNodeViewCfrClassSource
import org.benf.cfr.reader.state.DCCommonState
import org.benf.cfr.reader.util.getopt.OptionsImpl

class DeskDockJarIndexerService : JarIndexerService {
    private val cfrOptions = OptionsImpl(emptyMap())

    override fun acceptJar(scope: Set<String>, classProvider: ClassProvider, jarIndex: JarIndex) {
        val scopeNodes = scope.asSequence()
                .mapNotNull { classProvider.get(it) }
                .associateBy { "/${it.name.replace('/', '.')}.class" }

        val cfrSource = ClassNodeViewCfrClassSource(scopeNodes)
        val cfrState = DCCommonState(cfrOptions, cfrSource)

    }
}
