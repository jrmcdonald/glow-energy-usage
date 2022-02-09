rootProject.name = "glow-energy-usage"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Kotlin Versions
            version("kotlin", "1.6.10")

            // Plugin Dependencies
            version("detekt", "1.19.0")
            version("shadow", "7.0.0")
            version("spotless", "6.2.1")

            plugin("detekt", "io.gitlab.arturbosch.detekt").versionRef("detekt")
            plugin("shadow", "com.github.johnrengelman.shadow").versionRef("shadow")
            plugin("spotless", "com.diffplug.spotless").versionRef("spotless")

            // Platform Dependencies
            version("http4k", "4.19.0.0")
            version("junit", "5.8.2")

            library("http4k-dependencies", "org.http4k", "http4k-bom").versionRef("http4k")
            library("junit-dependencies", "org.junit", "junit-bom").versionRef("junit")

            // Core Dependencies
            version("clikt", "3.4.0")
            version("commonsCsv", "1.8")
            version("koin", "3.1.5")

            library("apache-commons-csv", "org.apache.commons", "commons-csv").versionRef("commonsCsv")
            library("clikt", "com.github.ajalt.clikt", "clikt").versionRef("clikt")
            library("http4k-core", "org.http4k", "http4k-core").withoutVersion()
            library("http4k-client-okhttp", "org.http4k", "http4k-client-okhttp").withoutVersion()
            library("http4k-cloudnative", "org.http4k", "http4k-cloudnative").withoutVersion()
            library("http4k-format-jackson", "org.http4k", "http4k-format-jackson").withoutVersion()
            library("http4k-security-oauth", "org.http4k", "http4k-security-oauth").withoutVersion()
            library("koin-core", "io.insert-koin", "koin-core").versionRef("koin")

            bundle(
                "http4k",
                listOf(
                    "http4k-core",
                    "http4k-client-okhttp",
                    "http4k-cloudnative",
                    "http4k-format-jackson",
                    "http4k-security-oauth"
                )
            )

            // Test Dependencies
            version("kotest", "5.1.0")
            version("mockk", "1.12.2")
            version("mockwebserver", "5.0.0-alpha.4")

            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").withoutVersion()
            library("koin-junit5", "io.insert-koin", "koin-test-junit5").versionRef("koin")
            library("kotest-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-json", "io.kotest", "kotest-assertions-json").versionRef("kotest")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            library("mockwebserver", "com.squareup.okhttp3", "mockwebserver3").versionRef("mockwebserver")

            bundle(
                "kotest",
                listOf(
                    "kotest-core",
                    "kotest-json"
                )
            )
        }
    }
}
