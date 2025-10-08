package com.profgroep8.models.domain

import com.profgroep8.models.dto.UserDTO
import com.profgroep8.models.entity.UserEntity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(userId: EntityID<Int>) : IntEntity(userId) {
    companion object : IntEntityClass<User>(UserEntity)

    var fullName by UserEntity.fullName
    var email by UserEntity.email
    var password by UserEntity.password
    var phone by UserEntity.phone
    var address by UserEntity.address
    var zipcode by UserEntity.zipcode
    var city by UserEntity.city
    var countryISO by UserEntity.countryISO
    var points by UserEntity.points

    fun toUserDTO(): UserDTO {
        return UserDTO(
            userId = this.id.value,
            fullName = this.fullName,
            email = this.email,
            phone = this.phone,
            address = this.address,
            zipcode = this.zipcode,
            city = this.city,
            countryISO = this.countryISO,
            points = this.points
        )
    }
}
