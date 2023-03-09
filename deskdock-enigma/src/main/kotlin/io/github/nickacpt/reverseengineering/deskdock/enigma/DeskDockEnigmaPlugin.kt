package io.github.nickacpt.reverseengineering.deskdock.enigma

import cuchaz.enigma.api.EnigmaPlugin
import cuchaz.enigma.api.EnigmaPluginContext
import cuchaz.enigma.api.service.*
import io.github.nickacpt.reverseengineering.deskdock.enigma.pattern.MethodPatternManager
import io.github.nickacpt.reverseengineering.deskdock.enigma.services.DeskDockJarIndexerService
import io.github.nickacpt.reverseengineering.deskdock.enigma.services.DeskDockNameProposalService
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

class DeskDockEnigmaPlugin : EnigmaPlugin {
    override fun init(ctx: EnigmaPluginContext) {
        ctx.registerDeskDockService("jar_index", JarIndexerService.TYPE) {
            DeskDockJarIndexerService.apply {
                this@registerDeskDockService.getArgument("method_patterns").getOrNull()?.let {
                    MethodPatternManager.load(Path(it))
                }
            }
        }

        ctx.registerDeskDockService("name_proposer", NameProposalService.TYPE) {
            DeskDockNameProposalService()
        }
    }

    private inline fun <reified S : EnigmaService> EnigmaPluginContext.registerDeskDockService(id: String, type: EnigmaServiceType<S>, crossinline factory: EnigmaServiceContext<S>.() -> S) {
        registerService("deskdock:$id", type) {
            factory(it)
        }
    }
}