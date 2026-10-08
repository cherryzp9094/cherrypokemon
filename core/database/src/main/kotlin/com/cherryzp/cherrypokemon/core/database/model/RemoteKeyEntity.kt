package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * 다음에 받아올 목록의 offset. PokeAPI 의 offset 은 아이템에 대응하지 않아 따로 저장한다.
 *
 * @param id 행은 하나뿐이다.
 */
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(@PrimaryKey val id: Int = SINGLE_ROW_ID, val nextOffset: Int?) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
