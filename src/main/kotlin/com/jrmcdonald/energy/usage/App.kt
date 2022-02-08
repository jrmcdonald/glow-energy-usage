package com.jrmcdonald.energy.usage

import com.jrmcdonald.energy.usage.core.UsageApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.environmentProperties
import org.koin.fileProperties

fun main(args: Array<String>) {
    val app = startKoin {
        printLogger(Level.ERROR)
        fileProperties()
        environmentProperties()
        modules(usageApplicationModule)
    }

    app.koin.get<UsageApplication>().main(args)
}
