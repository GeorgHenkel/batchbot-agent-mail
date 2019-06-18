package de.batchbot.agent.mail.service.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.batchbot.agent.mail.model.JobEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.xml.bind.JAXBContext

@Component
class MailboxPoller(
        @Value("\${mail.protocol}") val protocol: String,
        @Value("\${mail.host}") val host: String,
        @Value("\${mail.port}") val port: String,
        @Value("\${mail.tlsEnabled}") val enableTls: Boolean,
        @Value("\${mail.user}") val user: String,
        @Value("\${mail.password}") val password: String
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailboxPoller::class.java)
    }

    @Scheduled(fixedDelay = 30000)
    fun pollMailbox() {
        log.info("polling mailbox for new mails")

        val mailSession = initMailSession()

        mailSession.store.use { store ->
            store.connect(user, password)

            store.getFolder("INBOX").use { inbox ->
                inbox.open(Folder.READ_WRITE)

                if (inbox.messageCount == 0) {
                    log.debug("Found no new emails")
                    return
                }

                var processCount = 0
                inbox.messages
                        .filter { it.content is Multipart }
                        .forEach { msg ->
                            val from = (msg.from[0] as InternetAddress).address

                            val multipart = msg.content as Multipart
                            for (k in 0 until multipart.count) {
                                val attachment = parseAttachment(multipart.getBodyPart(k))

                                if (attachment != null) processCount++
                            }

                            msg.setFlag(Flags.Flag.DELETED, true)
                        }

                log.debug("$processCount emails processed")
            }
        }
    }

    private fun initMailSession(): Session {
        val properties = Properties()
        properties["mail.store.protocol"] = protocol

        properties[String.format("mail.%s.host", protocol)] = host
        properties[String.format("mail.%s.port", protocol)] = port

        if (protocol == "pop3s" || protocol == "imaps") {
            // SSL setting
            properties[String.format("mail.%s.socketFactory.class", protocol)] = "javax.net.ssl.SSLSocketFactory"
            properties[String.format("mail.%s.socketFactory.fallback", protocol)] = "false"
            properties[String.format("mail.%s.socketFactory.port", protocol)] = port
        }

        if (protocol == "pop3" && enableTls) properties["mail.pop3.starttls.enable"] = enableTls

        return Session.getDefaultInstance(properties)
    }

    private fun parseAttachment(bodyPart: BodyPart): JobEvent? {
        if (!Part.ATTACHMENT.equals(bodyPart.disposition, ignoreCase = true)) return null

        return try {
            when {
                bodyPart.fileName.endsWith(".json", ignoreCase = true) -> parseJson(bodyPart.inputStream)
                bodyPart.fileName.endsWith(".xml", ignoreCase = true) -> parseXml(bodyPart.inputStream)
                else -> null
            }
        } catch (ex: Exception) {
            log.warn("Error parsing JobEvent content", ex)
            return null
        }
    }

    private fun parseJson(content: InputStream): JobEvent? =
            ObjectMapper().registerKotlinModule()
                    .readValue(content, JobEvent::class.java)

    private fun parseXml(content: InputStream): JobEvent? =
            (JAXBContext.newInstance(JobEvent::class.java)
                    .createUnmarshaller()
                    .unmarshal(content)) as JobEvent
}