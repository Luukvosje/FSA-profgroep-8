package com.profgroep8.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.kotlin.datetime.Date
import java.sql.Date

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var realm: String
    private lateinit var algorithm: Algorithm

    fun init(config: ApplicationConfig) {
        secret = config.property("ktor.jwt.secret").getString()
        issuer = config.property("ktor.jwt.issuer").getString()
        audience = config.property("ktor.jwt.audience").getString()
        realm = config.property("ktor.jwt.realm").getString()
        algorithm = Algorithm.HMAC256(secret)
    }

    fun configureSecurity(config: Application) {
        config.install(io.ktor.server.auth.Authentication) {
            jwt("jwt") {
                realm = JwtConfig.realm
                verifier(
                    JWT.require(algorithm)
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.getClaim("userId").asString().isNotEmpty()) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }
    }

    fun generateToken(userId: String, email: String): String {
        val now = System.currentTimeMillis()
        // 24h token
        val expiresAt = Date(now + 1000 * 60 * 60 * 24)

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}