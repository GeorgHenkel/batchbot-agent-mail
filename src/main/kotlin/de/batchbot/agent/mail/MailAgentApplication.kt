package de.batchbot.agent.mail

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class MailAgentApplication

fun main(args: Array<String>) {
    runApplication<MailAgentApplication>(*args)
}