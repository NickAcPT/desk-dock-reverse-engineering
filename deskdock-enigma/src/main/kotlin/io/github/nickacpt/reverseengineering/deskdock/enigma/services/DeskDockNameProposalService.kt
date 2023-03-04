package io.github.nickacpt.reverseengineering.deskdock.enigma.services

import cuchaz.enigma.api.service.NameProposalService
import cuchaz.enigma.translation.mapping.EntryRemapper
import cuchaz.enigma.translation.representation.entry.Entry
import java.util.*

class DeskDockNameProposalService : NameProposalService {
    override fun proposeName(obfEntry: Entry<*>, remapper: EntryRemapper): Optional<String> {
        return Optional.empty()
    }
}