package de.batchbot.agent.mail.model

import javax.xml.bind.annotation.XmlEnum

@XmlEnum
enum class JobStatus {
    STARTED, ABORTED, FINISHED;
}