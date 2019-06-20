package io.batchbot.agent.mail.infrastructure.amqp.service

import io.batchbot.agent.mail.infrastructure.amqp.config.MessagingConfiguration
import io.batchbot.agent.mail.model.BatchEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BatchEventMessagingService(
        @Autowired
        val rabbitTemplate: RabbitTemplate
) {
    companion object {
        private val log = LoggerFactory.getLogger(BatchEventMessagingService::class.java)
    }

    fun sendBatchEvents(batchEvents: List<BatchEvent>) {
        batchEvents.forEach { event ->
            log.debug("Sending new BatchEvent to queue ${MessagingConfiguration.QUEUE_BATCH_EVENTS}")
            rabbitTemplate.convertAndSend(MessagingConfiguration.QUEUE_BATCH_EVENTS, event)
        }
    }
}