package io.batchbot.agent.mail.infrastructure.mail

import io.batchbot.agent.mail.application.parser.ParserFactory
import io.batchbot.agent.mail.infrastructure.amqp.service.BatchEventMessagingService
import io.batchbot.agent.mail.model.BatchEvent
import org.apache.commons.mail.util.MimeMessageParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.search.FlagTerm

@Service
class MailAgent(
        @Value("\${mail.protocol}") val protocol: String,
        @Value("\${mail.host}") val host: String,
        @Value("\${mail.port}") val port: String,
        @Value("\${mail.tlsEnabled}") val enableTls: Boolean,
        @Value("\${mail.user}") val user: String,
        @Value("\${mail.password}") val password: String,
        @Autowired val parserFactory: ParserFactory,
        @Autowired val messageSender: BatchEventMessagingService
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailAgent::class.java)
    }

    fun loadAndProcessMails() {
        val mailSession = initMailSession()
        val batchEvents = mailSession.store.use { store ->
            store.connect(user, password)

            store.getFolder("INBOX").use { inbox ->
                inbox.open(Folder.READ_WRITE)

                if (inbox.unreadMessageCount == 0) {
                    log.info("found no new emails")
                    return
                }

                val unseenMessages = inbox.search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                log.info("processing ${unseenMessages.size} new mails")

                handleMessages(unseenMessages)
            }
        }

        log.info("${batchEvents.size} mails are processable")
        messageSender.sendBatchEvents(batchEvents)
    }

    private fun initMailSession(): Session {
        val properties = Properties()
        properties["mail.store.protocol"] = protocol

        properties[String.format("mail.%s.host", protocol)] = host
        properties[String.format("mail.%s.port", protocol)] = port
        properties[String.format("mail.%s.starttls.enable", protocol)] = enableTls

        if (protocol == "pop3s" || protocol == "imaps") {
            // SSL setting
            properties[String.format("mail.%s.socketFactory.class", protocol)] = "javax.net.ssl.SSLSocketFactory"
            properties[String.format("mail.%s.socketFactory.fallback", protocol)] = "false"
            properties[String.format("mail.%s.socketFactory.port", protocol)] = port
        }

        return Session.getDefaultInstance(properties)
    }

    private fun handleMessages(messages: Array<Message>): List<BatchEvent> =
            messages.filter { it.content is Multipart }.mapNotNull { processMail(it as MimeMessage) }

    private fun processMail(message: MimeMessage): BatchEvent? {
        message.setFlag(Flags.Flag.SEEN, true)

        val parser = MimeMessageParser(message).parse()
        return when {
            parser.isMultipart && parser.hasAttachments() ->
                parser.attachmentList
                        .firstOrNull { it.contentType == "application/json" || it.contentType == "application/xml" }
                        ?.let { ds -> parseAndDeleteMessage(ds, message) }
            else -> null
        }
    }

    private fun parseAndDeleteMessage(ds: DataSource, message: MimeMessage): BatchEvent? {
        return parserFactory.parseData(ds.contentType, ds.inputStream)?.let {
            message.setFlag(Flags.Flag.DELETED, true)

            val from = (message.from[0] as InternetAddress).address
            BatchEvent(from, Instant.now().toString(), it)
        }
    }
}