import com.profgroep8.exceptions.UnauthorizedException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

fun Application.configureUserContext() {
    intercept(ApplicationCallPipeline.Setup) {
        val principal = call.principal<JWTPrincipal>()
        if (principal != null) {
            val userId = principal.payload.getClaim("userId").asString()
            val email = principal.payload.getClaim("email").asString()

            if (!userId.isNullOrEmpty()) {
                val userContext = UserContext(userId.toInt(), email)
                val contextElement = UserContextElement(userContext)

                withContext(contextElement) {
                    proceed()
                }

                return@intercept
            }
        }
        proceed()
    }
}

fun Application.configureUserContextPlugin() {
    intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<JWTPrincipal>() ?: return@intercept

        val userId = principal.payload.getClaim("userId").asString()
        val email = principal.payload.getClaim("email").asString()

        if (!userId.isNullOrEmpty()) {
            val userContext = UserContext(userId.toInt(), email)
            val element = UserContextElement(userContext)
            withContext(element) {
                proceed()
            }
        } else {
            proceed()
        }
    }
}

suspend fun getUserContext(): UserContext {
    return currentCoroutineContext()[UserContextElement]?.userContext ?: throw UnauthorizedException()
}

data class UserContext(
    val userID: Int,
    val email: String
)

class UserContextElement(val userContext: UserContext) :
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<UserContextElement>
}
