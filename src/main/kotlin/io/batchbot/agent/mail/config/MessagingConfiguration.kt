package io.batchbot.agent.mail.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.rabbit.core.RabbitTemplate

@Configuration
class MessagingConfiguration {

    companion object {
        const val QUEUE_BATCH_EVENTS = "batch-events-queue"
        const val EXCHANGE_BATCH_EVENTS = "batch-events-exchange"
    }

    @Bean
    fun batchEventsQueue(): Queue =
            QueueBuilder.durable(QUEUE_BATCH_EVENTS).build()

    @Bean
    fun batchEventsExchange(): Exchange =
            ExchangeBuilder.topicExchange(EXCHANGE_BATCH_EVENTS).build()

    @Bean
    fun binding(batchEventsQueue: Queue, batchEventsExchange: TopicExchange): Binding =
            BindingBuilder.bind(batchEventsQueue).to(batchEventsExchange).with(QUEUE_BATCH_EVENTS)

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = producerJackson2MessageConverter()

        return rabbitTemplate
    }

    @Bean
    fun producerJackson2MessageConverter(): Jackson2JsonMessageConverter =
            Jackson2JsonMessageConverter()
}