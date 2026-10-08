package com.cherryzp.cherrypokemon.core.database

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import javax.inject.Inject

/**
 * 여러 DAO 호출을 한 트랜잭션으로 묶는다.
 * 데이터베이스 클래스를 모듈 밖으로 내보내지 않으려고 둔 인터페이스다.
 */
interface DatabaseTransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}

internal class RoomDatabaseTransactionRunner @Inject constructor(
    private val database: CherryPokemonDatabase,
) : DatabaseTransactionRunner {
    override suspend fun <R> invoke(block: suspend () -> R): R =
        database.useWriterConnection { connection ->
            connection.immediateTransaction { block() }
        }
}
