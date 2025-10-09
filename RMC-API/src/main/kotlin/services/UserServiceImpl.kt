package com.profgroep8.services

import com.profgroep8.interfaces.repositories.UserRepository
import com.profgroep8.interfaces.services.UserService
import com.profgroep8.models.domain.User
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UserDTO
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.profgroep8.models.dto.LoginResponseDTO
import java.util.*
import com.profgroep8.models.entity.UserEntity
import com.profgroep8.plugins.JwtConfig.generateToken

class UserServiceImpl : UserService, UserRepository<User> {

    override fun register(item: CreateUserDTO): UserDTO {
        val existingUser = findByEmail(item.email)
        if (existingUser != null) throw BadRequestException("User already exists with this email")

        val createdUser = create(item) ?: throw BadRequestException("Unexpected error during registration")
        return createdUser.toUserDTO()
    }

    override fun findByEmail(email: String): User? = transaction {
        User.find { UserEntity.email eq email }.singleOrNull()
    }

    override fun create(user: CreateUserDTO): User? = transaction {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        User.new {
            fullName = user.fullName
            email = user.email
            password = hashedPassword
            phone = user.phone
            address = user.address
            zipcode = user.zipcode
            city = user.city
            countryISO = user.countryISO
            points = 0
        }
    }

    override fun login(item: LoginUserDTO): LoginResponseDTO {
        val user = transaction {
            User.find { UserEntity.email eq item.email }.singleOrNull()
        } ?: throw BadRequestException("Invalid email or password")

        val validPassword = BCrypt.checkpw(item.password, user.password)
        if (!validPassword) throw BadRequestException("Invalid email or password")

        val token = generateToken(user.id.value.toString(), user.email)

        return LoginResponseDTO(
            user = user.toUserDTO(),
            token = token
        )
    }

    override fun getByEmail(email: String): UserDTO? {
        val user = transaction {
            User.find { UserEntity.email eq email }.singleOrNull()
        } ?: return null

        return user.toUserDTO()
    }

}