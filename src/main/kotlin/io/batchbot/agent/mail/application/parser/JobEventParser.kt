package io.batchbot.agent.mail.application.parser

import io.batchbot.api.models.job.JobEvent
import java.io.InputStream

interface JobEventParser {
    fun parse(inputStream: InputStream): JobEvent
}