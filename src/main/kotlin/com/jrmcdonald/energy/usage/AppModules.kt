package com.jrmcdonald.energy.usage

import com.jrmcdonald.energy.usage.core.UsageApplication
import com.jrmcdonald.energy.usage.core.service.CsvOutputService
import com.jrmcdonald.energy.usage.core.service.EnergyProviderService
import com.jrmcdonald.energy.usage.external.provider.hildebrand.GlowClient
import com.jrmcdonald.energy.usage.external.provider.hildebrand.GlowConfig
import com.jrmcdonald.energy.usage.external.provider.hildebrand.GlowProviderService
import org.http4k.client.OkHttp
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.koin.dsl.module

val usageApplicationModule = module {
    single { ClientFilters.SetHostFrom(Uri.of(getProperty("glow_base_url"))).then(OkHttp()) }
    single {
        GlowConfig(
            getProperty("glow_username"),
            getProperty("glow_password"),
            getProperty("glow_application_id")
        )
    }
    single { GlowClient(get(), get()) }
    single<EnergyProviderService> { GlowProviderService(get()) }
    single { CsvOutputService(getProperty("output_directory", ""), getProperty("output_filename", "usage.csv")) }
    single { UsageApplication(get(), get()) }
}
