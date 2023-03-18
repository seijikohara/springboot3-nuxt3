package com.example.demo.config

import com.example.demo.handler.HelloHandler
import com.example.demo.handler.IndexHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RoutingConfig(
    private val applicationProperties: ApplicationProperties,
    private val indexHandler: IndexHandler,
    private val helloHandler: HelloHandler,
) {

    @Bean
    fun apiRouter() = coRouter {
        accept(MediaType.ALL).nest {
            GET("/*", indexHandler::getIndex)
            (applicationProperties.apiBasePath).nest {
                GET("/hello", helloHandler::getHello)
            }
        }
    }

}
