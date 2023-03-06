package io.github.nickacpt.reverseengineering.deskdock.enigma.services

import cuchaz.enigma.api.service.NameProposalService
import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.ClassEntry
import cuchaz.enigma.translation.representation.entry.Entry
import cuchaz.enigma.translation.representation.entry.FieldEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.AbstractDeskDockIndexer
import io.github.nickacpt.reverseengineering.deskdock.enigma.index.model.IndexEntryKey
import java.util.*

class DeskDockNameProposalService : NameProposalService {
    override fun proposeName(obfEntry: Entry<*>, remapper: EntryRemapper): Optional<String> {
        val parentEntry = obfEntry.topLevelClass

        val indexEntry = enigmaEntryToIndexEntry(obfEntry) ?: return Optional.empty()
        val entries = DeskDockJarIndexerService.getEntryKeyIndexContent(indexEntry).takeIf { it.isNotEmpty() }
            ?: return Optional.empty()

        val name = entries.associateBy { it.indexer }.mapValues { it.value.value }.firstNotNullOfOrNull { (indexer, entry) ->
            @Suppress("UNCHECKED_CAST")
            (indexer as AbstractDeskDockIndexer<Any>).proposeName(obfEntry, remapper, indexEntry, entry)
        }

        return Optional.ofNullable(name)
    }

    private fun enigmaEntryToIndexEntry(obfEntry: Entry<*>): IndexEntryKey? = when (obfEntry) {
        is ClassEntry -> IndexEntryKey.ClassIndexEntry(obfEntry.fullName)
        is MethodEntry -> IndexEntryKey.MethodIndexEntry(
            enigmaEntryToIndexEntry(obfEntry.parent!!) as IndexEntryKey.ClassIndexEntry,
            obfEntry.name,
            obfEntry.desc.toString()
        )

        is FieldEntry -> IndexEntryKey.FieldIndexEntry(
            enigmaEntryToIndexEntry(obfEntry.parent!!) as IndexEntryKey.ClassIndexEntry,
            obfEntry.name,
            obfEntry.desc.toString()
        )

        else -> null
    }
}