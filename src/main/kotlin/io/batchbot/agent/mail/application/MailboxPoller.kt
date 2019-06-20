package io.batchbot.agent.mail.application

import io.batchbot.agent.mail.infrastructure.mail.MailAgent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class MailboxPoller(
        @Autowired val mailAgent: MailAgent
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailboxPoller::class.java)
    }

    @Scheduled(fixedDelayString = "\${scheduler.delay}")
    fun pollMailbox() {
        log.info("polling mailbox for new mails")
        mailAgent.loadAndProcessMails()
    }

}