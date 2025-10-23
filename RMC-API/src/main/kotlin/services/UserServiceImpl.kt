package com.profgroep8.services

import com.profgroep8.interfaces.services.UserService
import com.profgroep8.models.domain.User
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.LoginResponseDTO
import com.profgroep8.models.dto.UserDTO
import com.profgroep8.plugins.JwtConfig.generateToken
import io.ktor.server.plugins.BadRequestException
import org.mindrot.jbcrypt.BCrypt

class UserServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl) : UserService {

    override fun register(item: CreateUserDTO): UserDTO {
        val existingUser = serviceFactoryImpl.databaseFactory.userRepository.findByEmail(item.email)
        if (existingUser != null) throw BadRequestException("User already exists with this email")

        val createdUser = serviceFactoryImpl.databaseFactory.userRepository.create(item)
            ?: throw BadRequestException("Unexpected error during registration")

        return createdUser.toUserDTO()
    }

    override fun login(item: LoginUserDTO): LoginResponseDTO {
        val user = serviceFactoryImpl.databaseFactory.userRepository.findByEmail(item.email)
            ?: throw BadRequestException("Invalid email or password")

        if (!BCrypt.checkpw(item.password, user.password))
            throw BadRequestException("Invalid email or password")

        val token = generateToken(user.id.value.toString(), user.email)

        return LoginResponseDTO(
            user = user.toUserDTO(),
            token = token
        )
    }

    override fun getByEmail(email: String): UserDTO? {
        val user = serviceFactoryImpl.databaseFactory.userRepository.findByEmail(email)
            ?: return null
        return user.toUserDTO()
    }

    override fun getBonusPoints(userId: Int): Int {
        val user = serviceFactoryImpl.databaseFactory.userRepository.getSingle(userId)
            ?: throw BadRequestException("User not found")
        return user.points
    }

    override fun updateBonusPoints(userId: Int, points: Int): UserDTO {
        val updatedUser = serviceFactoryImpl.databaseFactory.userRepository.update(userId) {
            this.points = points
        } ?: throw BadRequestException("User not found")

        return updatedUser.toUserDTO()
    }
}
