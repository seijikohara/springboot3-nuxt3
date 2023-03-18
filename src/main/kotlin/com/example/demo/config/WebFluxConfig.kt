package com.example.demo.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebFluxConfig(
    val applicationProperties: ApplicationProperties
) : WebFluxConfigurer {

    override fun addCorsMappings(corsRegistry: CorsRegistry) {
        val corsProperties = applicationProperties.cors
        corsRegistry.addMapping(corsProperties.mappingPathPattern)
            .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
            .allowedMethods(*corsProperties.allowedMethods.toTypedArray())
            .maxAge(corsProperties.maxAge)
    }

}