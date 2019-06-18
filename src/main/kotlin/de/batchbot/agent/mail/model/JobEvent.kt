package de.batchbot.agent.mail.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "jobevent")
@XmlAccessorType(XmlAccessType.FIELD)
data class JobEvent(
        @XmlElement
        val status: JobStatus,
        val startTime: String
)