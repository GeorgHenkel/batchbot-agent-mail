package io.batchbot.agent.mail.application.parser

import io.batchbot.api.models.job.JobEvent
import org.springframework.stereotype.Component
import java.io.InputStream
import javax.xml.bind.JAXBContext

@Component
class JobEventXmlParser : JobEventParser {
    override fun parse(inputStream: InputStream): JobEvent =
            JAXBContext.newInstance(JobEvent::class.java).createUnmarshaller().unmarshal(inputStream) as JobEvent
}