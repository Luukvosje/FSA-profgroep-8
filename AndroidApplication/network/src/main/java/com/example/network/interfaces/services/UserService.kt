package com.example.network.interfaces.services

import com.example.network.models.domain.User
import com.example.network.models.remote.CreateUserDTO
import com.example.network.models.remote.LoginUserDTO
import com.example.network.services.ApiResult

interface UserService {
    suspend fun register(request: CreateUserDTO): ApiResult<User>
    suspend fun login(request: LoginUserDTO): ApiResult<User>
    suspend fun getMe(): ApiResult<User>
    fun logout()
    suspend fun getBonusPoints(userId: Int): ApiResult<Int>
    suspend fun updateBonusPoints(userId: Int, points: Int): ApiResult<User>
    fun loginWithToken(token: String)
    suspend fun restoreSession(token: String): ApiResult<User>
}
