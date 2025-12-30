package com.example.network.services

import com.example.network.interfaces.services.UserService
import com.example.network.models.domain.User
import com.example.network.models.remote.CreateUserDTO
import com.example.network.models.remote.RemoteUser
import com.example.network.models.remote.toDomainUser

class UserServiceImpl : BaseServiceImpl(), UserService {

    override suspend fun register(request: CreateUserDTO): ApiResult<User> {
        return safeExecute {
            post<RemoteUser, CreateUserDTO>(
                "users/register",
                request
            ).toDomainUser()
        }
    }
}
