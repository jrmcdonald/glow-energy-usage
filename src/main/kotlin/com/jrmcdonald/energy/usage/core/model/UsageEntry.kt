package com.jrmcdonald.energy.usage.core.model

import java.time.Instant

data class UsageEntry(
    val timestamp: Instant,
    val usage: Float
)
