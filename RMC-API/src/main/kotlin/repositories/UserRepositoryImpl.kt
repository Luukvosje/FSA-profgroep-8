package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.UserRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.User
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.entity.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserRepositoryImpl :
    UserRepository,
    GenericRepository<User> by GenericRepositoryImpl(User) {

    /**
     * Find user by email address (case-insensitive)
     */
    override fun findByEmail(email: String): User? = transaction {
        User.find { UserEntity.email eq email.lowercase().trim() }.singleOrNull()
    }

    /**
     * Create and persist a new user with hashed password and default bonus points = 0
     */
    override fun create(user: CreateUserDTO): User? = transaction {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

        User.new {
            fullName = user.fullName
            email = user.email.lowercase().trim()
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