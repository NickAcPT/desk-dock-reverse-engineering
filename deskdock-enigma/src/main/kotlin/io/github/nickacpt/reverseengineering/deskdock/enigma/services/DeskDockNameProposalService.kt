package io.github.nickacpt.reverseengineering.deskdock.enigma.services

import cuchaz.enigma.api.service.NameProposalService
import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.*
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.AbstractDeskDockIndexer
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import java.util.*

class DeskDockNameProposalService : NameProposalService {
    override fun proposeName(obfEntry: Entry<*>, remapper: EntryRemapper): Optional<String> {
        val indexEntry = enigmaEntryToIndexEntry(obfEntry) ?: return Optional.empty()

        var content = DeskDockJarIndexerService.getEntryKeyIndexContent(indexEntry)
        if (indexEntry is IndexEntryKey.MethodParameterIndexEntry && content.isEmpty()) {
            content = DeskDockJarIndexerService.getEntryKeyIndexContent(indexEntry.method)
        }

        val entries = content.takeIf { it.isNotEmpty() }
            ?: return Optional.empty()

        val name =
            entries.associateBy { it.indexer }.mapValues { it.value.value }.firstNotNullOfOrNull { (indexer, entry) ->
                @Suppress("UNCHECKED_CAST")
                val idx = indexer as AbstractDeskDockIndexer<Any>
                idx.proposeName(obfEntry, remapper, indexEntry, entry)
            }

        return Optional.ofNullable(name)
    }

    fun enigmaEntryToIndexEntry(obfEntry: Entry<*>): IndexEntryKey? = when (obfEntry) {
        is ClassEntry -> IndexEntryKey.ClassIndexEntry(obfEntry.fullName)
        is MethodEntry -> IndexEntryKey.MethodIndexEntry(
            enigmaEntryToIndexEntry(obfEntry.parent!!) as IndexEntryKey.ClassIndexEntry,
            obfEntry.name,
            obfEntry.desc.toString(),
        )

        is FieldEntry -> IndexEntryKey.FieldIndexEntry(
            enigmaEntryToIndexEntry(obfEntry.parent!!) as IndexEntryKey.ClassIndexEntry,
            obfEntry.name,
            obfEntry.desc.toString()
        )

        is LocalVariableEntry -> {
            if (obfEntry.isArgument) {
                IndexEntryKey.MethodParameterIndexEntry(
                    enigmaEntryToIndexEntry(obfEntry.parent!!) as IndexEntryKey.MethodIndexEntry,
                    obfEntry.name
                )
            } else {
                null
            }
        }

        else -> null
    }
}