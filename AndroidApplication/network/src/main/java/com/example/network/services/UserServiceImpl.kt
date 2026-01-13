package com.example.network.services

import com.example.network.interfaces.services.UserService
import com.example.network.models.domain.User
import com.example.network.models.remote.*
import kotlinx.serialization.Serializable
import com.example.network.models.remote.CreateUserDTO
import com.example.network.models.remote.LoginUserDTO
import com.example.network.models.remote.RemoteBonusPointsResponse
import com.example.network.models.remote.RemoteLoginResponse
import com.example.network.models.remote.RemoteUser
import com.example.network.models.remote.toDomainUser

internal final class UserServiceImpl : BaseServiceImpl(), UserService {

    @Serializable
    private data class RemoteBonusPointsResponse(val bonusPoints: Int)

    @Serializable
    private data class UpdateBonusPointsBody(val points: Int)

    override suspend fun register(request: CreateUserDTO): ApiResult<User> {
        return safeExecute {
            post<RemoteUser, CreateUserDTO>(
                "users/register",
                request
            ).toDomainUser()
        }
    }

    override suspend fun login(request: LoginUserDTO): ApiResult<User> {
        return safeExecute {
            val response = post<RemoteLoginResponse, LoginUserDTO>(
                "users/login",
                request
            )
            updateToken(response.token)
            UserProvider.user = response.user.toDomainUser(response.token)
            response.user.toDomainUser(response.token)
        }
    }

    override suspend fun getMe(): ApiResult<User> {
        return safeExecute {
           get<RemoteUser>("users/me").toDomainUser()
        }
    }

    override suspend fun getBonusPoints(userId: Int): ApiResult<Int> {
        return safeExecute {
            get<RemoteBonusPointsResponse>("users/$userId/bonuspoints").bonusPoints
        }
    }

    override suspend fun updateBonusPoints(userId: Int, points: Int): ApiResult<User> {
        return safeExecute {
            put<RemoteUser, UpdateBonusPointsBody>(
                "users/$userId/bonuspoints",
                UpdateBonusPointsBody(points)
            ).toDomainUser()
        }
    }

    override fun logout() {
        updateToken(null)
    }

    override fun loginWithToken(token: String) {
        updateToken(token)
    }

    override suspend fun restoreSession(token: String): ApiResult<User> {
        return safeExecute {
            updateToken(token)
            val user = get<RemoteUser>("users/me").toDomainUser(token)
            UserProvider.user = user
            user
        }
    }
}