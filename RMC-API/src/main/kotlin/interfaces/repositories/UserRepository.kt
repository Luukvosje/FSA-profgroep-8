package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.User
import com.profgroep8.models.dto.CreateUserDTO
import org.jetbrains.exposed.dao.IntEntity

interface UserRepository<T: IntEntity> {
    fun findByEmail(email: String): User?
    fun create(user: CreateUserDTO): User?
}