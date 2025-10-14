import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

val ApplicationCall.userContext: UserContext?
    get() {
        val principal = principal<JWTPrincipal>() ?: return null
        val email = principal.payload.getClaim("email")?.asString()
        val userId = principal.payload.getClaim("userId")?.asString()
        return if (email != null && userId != null) UserContext(userId, email) else null
    }

data class UserContext(
    val id: String,
    val email: String
)