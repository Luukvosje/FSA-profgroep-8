package com.example.network.interfaces.services

import com.example.network.models.domain.User
import com.example.network.models.remote.CreateUserDTO
import com.example.network.services.ApiResult

interface UserService {
    suspend fun register(request: CreateUserDTO): ApiResult<User>
}
