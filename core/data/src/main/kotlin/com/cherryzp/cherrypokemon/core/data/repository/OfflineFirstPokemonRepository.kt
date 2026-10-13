package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cherryzp.cherrypokemon.core.data.model.asAbilityNameEntity
import com.cherryzp.cherrypokemon.core.data.model.asEntity
import com.cherryzp.cherrypokemon.core.data.model.asPokemonType
import com.cherryzp.cherrypokemon.core.data.model.asTypeNameEntity
import com.cherryzp.cherrypokemon.core.data.paging.PokemonRemoteMediator
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** 읽기는 항상 로컬에서 하고, 네트워크는 로컬을 갱신하는 데만 쓴다. */
internal class OfflineFirstPokemonRepository @Inject constructor(
    private val pokemonDao: PokemonDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val localizedNameDao: LocalizedNameDao,
    private val network: PokemonNetworkDataSource,
    private val transaction: DatabaseTransactionRunner,
) : PokemonRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemonsStream(): Flow<PagingData<Pokemon>> = Pager(
        config = PagingConfig(pageSize = POKEMON_PAGE_SIZE),
        remoteMediator = PokemonRemoteMediator(pokemonDao, remoteKeyDao, network, transaction),
        pagingSourceFactory = pokemonDao::pagingSource
    ).flow.map { pagingData -> pagingData.map(PokemonEntity::asExternalModel) }

    override fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?> = combine(
        pokemonDao.getPokemonDetailEntity(pokeId),
        localizedNameDao.getTypeNames(),
        localizedNameDao.getAbilityNames()
    ) { detail, typeNames, abilityNames ->
        detail?.asExternalModel(
            typeNames = typeNames.associate { it.asExternalModel() },
            abilityNames = abilityNames.associate { it.asExternalModel() }
        )
    }

    override suspend fun refreshPokemonDetail(pokeId: Int) {
        // 상세와 종 정보를 함께 받는다. 하나라도 실패하면 아무것도 쓰지 않는다.
        val detail = network.getPokemonDetail(pokeId)
        val species = network.getPokemonSpecies(pokeId)
        pokemonDao.insertPokemonDetail(detail.asEntity(species))
        refreshMissingNames(detail)
    }

    /**
     * 아직 받지 않은 타입·특성 이름만 받아 둔다.
     *
     * 타입은 20여 개, 특성은 포켓몬당 한둘이라 쓸수록 호출이 사라진다.
     * 이름을 받지 못해도 상세는 이미 저장했으므로 실패를 삼킨다.
     */
    private suspend fun refreshMissingNames(detail: NetworkPokemonDetail) {
        val savedTypes = localizedNameDao.getSavedTypes().toSet()
        val missingTypes = detail.types
            .map { it.type.name }
            .filter { it.asPokemonType().name !in savedTypes }
            .distinct()
        val savedAbilities = localizedNameDao.getSavedAbilities().toSet()
        val missingAbilities = detail.abilities
            .map { it.ability.name }
            .filter { it !in savedAbilities }
            .distinct()

        runCatching {
            localizedNameDao.insertTypeNames(
                missingTypes.map { typeName ->
                    network.getType(typeName).asTypeNameEntity(typeName.asPokemonType())
                }
            )
            localizedNameDao.insertAbilityNames(
                missingAbilities.map { network.getAbility(it).asAbilityNameEntity() }
            )
        }
    }

    private companion object {
        const val POKEMON_PAGE_SIZE = 20
    }
}
