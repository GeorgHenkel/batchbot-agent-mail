package io.batchbot.agent.mail.service.messaging

import io.batchbot.agent.mail.config.MessagingConfiguration
import io.batchbot.agent.mail.model.BatchEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BatchEventMessagingService(
        @Autowired
        val rabbitTemplate: RabbitTemplate
) {
    fun sendBatchEvents(batchEvents: List<BatchEvent>) {
        batchEvents.forEach { event ->
            rabbitTemplate.convertAndSend(MessagingConfiguration.QUEUE_BATCH_EVENTS, event)
        }
    }
}