import com.profgroep8.exceptions.UnauthorizedException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

fun ApplicationCall.getUserContext(): UserContext? {
    val principal = this.principal<JWTPrincipal>() ?: return null
    val userId = principal.payload.getClaim("userId").asString().toIntOrNull()
    val email = principal.payload.getClaim("email").asString()

    return if (userId != null && email != null) {
        UserContext(userId, email)
    } else {
        null
    }
}
fun ApplicationCall.requireUserContext(): UserContext {
    return getUserContext() ?: throw UnauthorizedException()
}

data class UserContext(
    val userID: Int,
    val email: String
)
