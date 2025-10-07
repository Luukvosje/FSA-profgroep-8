package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.GenericRepository
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

class GenericRepositoryImpl<T : IntEntity>(
    private val entity: IntEntityClass<T>
) : GenericRepository<T> {

    override fun getAll(): List<T> = transaction {
        entity.all().toList()
    }

    override fun getSingle(id: Int): T? = transaction {
        entity.findById(id)
    }

    override fun create(init: T.() -> Unit): T = transaction {
        entity.new(init)
    }

    override fun delete(id: Int) = transaction {
        entity.findById(id)?.let { it.delete(); true } ?: false
    }
}