package com.profgroep8.services

import com.profgroep8.interfaces.repositories.UserRepository
import com.profgroep8.interfaces.services.UserService
import com.profgroep8.models.domain.User
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.UserDTO
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import com.profgroep8.models.entity.UserEntity

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
}