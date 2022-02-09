package com.jrmcdonald.energy.usage.core.service

import com.jrmcdonald.energy.usage.core.model.UsageEntry
import java.time.Instant

interface EnergyProviderService {
    fun getEnergyUsage(from: Instant, to: Instant): List<UsageEntry>
}
