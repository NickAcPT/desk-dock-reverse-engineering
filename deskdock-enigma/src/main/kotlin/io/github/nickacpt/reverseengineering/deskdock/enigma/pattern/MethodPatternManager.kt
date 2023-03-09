package io.github.nickacpt.reverseengineering.deskdock.enigma.pattern

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Path

object MethodPatternManager {
    var patterns: List<MethodPattern> = emptyList()
        private set
    private val mapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun load(path: Path) {
        this.patterns = mapper.readValue<List<MethodPattern>>(path.toFile())
    }
}