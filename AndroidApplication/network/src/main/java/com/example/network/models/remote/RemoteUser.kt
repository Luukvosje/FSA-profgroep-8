package com.example.network.models.remote

import com.example.network.models.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class RemoteUser(
    val userID: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val countryISO: String,
    val points: Int
)

fun RemoteUser.toDomainUser(token: String = ""): User {
    return User(
        userID = userID,
        fullName = fullName,
        email = email,
        phone = phone,
        address = address,
        zipcode = zipcode,
        city = city,
        countryISO = countryISO,
        bonusPoints = points,
        token = token
    )
}

@Serializable
data class CreateUserDTO(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val countryISO: String,
)

@Serializable
data class LoginUserDTO(
    val email: String,
    val password: String,
)

@Serializable
data class RemoteLoginResponse(
    val user: RemoteUser,
    val token: String,
)

fun RemoteLoginResponse.toDomain(): Pair<User, String> {
    return user.toDomainUser(token) to token
}

@Serializable
data class RemoteBonusPointsResponse(
    val bonusPoints: Int
)