package com.profgroep8.interfaces.repositories

import org.jetbrains.exposed.dao.IntEntity

interface GenericRepository<T: IntEntity> {
    fun getAll(): List<T>
    fun getSingle(id: Int): T?
    fun create(createBlock: T.() -> Unit): T?
    fun update(id: Int, updateBlock: T.() -> Unit): T?
    fun delete(id: Int): Boolean
}