package com.jrmcdonald.energy.usage

import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules

class AppModulesKtTest {

    @Test
    fun `Should setup valid Koin modules`() {
        koinApplication {
            modules(usageApplicationModule)
            checkModules() {
                withProperty("glow_base_url", "http://localhost")
                withProperty("glow_application_id", "1234")
                withProperty("glow_username", "user@example.com")
                withProperty("glow_password", "not-so-secret")
            }
        }
    }
}
