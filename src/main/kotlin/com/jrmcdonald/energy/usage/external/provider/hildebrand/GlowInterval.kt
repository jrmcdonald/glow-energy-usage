package com.jrmcdonald.energy.usage.external.provider.hildebrand

import java.time.Instant

data class GlowInterval(
    val start: Instant,
    val end: Instant
)
