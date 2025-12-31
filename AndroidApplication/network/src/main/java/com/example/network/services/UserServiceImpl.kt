package com.example.network.services

import com.example.network.interfaces.services.UserService
import com.example.network.models.domain.User
import com.example.network.models.remote.*
import com.example.network.services.ApiResult

abstract class UserServiceImpl : BaseServiceImpl(), UserService {

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
            response.user.toDomainUser()
        }
    }

    override suspend fun getMe(): ApiResult<User> {
        return safeExecute {
            get<RemoteUser>("users/me").toDomainUser()
        }
    }

    override suspend fun getBonusPoints(userId: Int): ApiResult<Int> {
        return safeExecute {
            get<RemoteBonusPointsResponse>(
                "users/$userId/bonuspoints"
            ).bonusPoints
        }
    }

    override fun logout() {
        updateToken(null)
    }
}
