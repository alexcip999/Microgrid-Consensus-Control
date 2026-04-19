package domain.service

import domain.model.token.TokenClaims
import domain.model.token.TokenConfig

interface TokenService {
    val config: TokenConfig
    fun generate(claims: TokenClaims): String
}