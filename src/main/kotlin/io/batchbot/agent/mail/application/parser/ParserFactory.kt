package io.batchbot.agent.mail.application.parser

import io.batchbot.api.models.job.JobEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class ParserFactory(
        @Autowired val jsonParser: JobEventJsonParser,
        @Autowired val xmlParser: JobEventXmlParser
) {
    companion object {
        private val log = LoggerFactory.getLogger(ParserFactory::class.java)
    }

    fun parseData(contentType: String, inputStream: InputStream): JobEvent? {
        return try {
            when (contentType) {
                "application/json" -> jsonParser.parse(inputStream)
                "application/xml" -> xmlParser.parse(inputStream)
                else -> null
            }
        } catch (ex: Exception) {
            log.warn("Error parsing JobEvent content", ex)
            return null
        }
    }
}