package io.batchbot.agent.mail.model

import io.batchbot.api.models.job.JobEvent
import java.io.Serializable

data class BatchEvent(
        val originator: String,
        val processDate: String,
        val jobEvent: JobEvent
) : Serializable