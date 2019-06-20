package io.batchbot.agent.mail.application.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.batchbot.api.models.job.JobEvent
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class JobEventJsonParser : JobEventParser {
    override fun parse(inputStream: InputStream): JobEvent =
            ObjectMapper().registerKotlinModule().readValue(inputStream, JobEvent::class.java)
}