package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginResponseDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UserDTO

interface UserService {
    fun register(item: CreateUserDTO): UserDTO
    fun login(item: LoginUserDTO): LoginResponseDTO

}
