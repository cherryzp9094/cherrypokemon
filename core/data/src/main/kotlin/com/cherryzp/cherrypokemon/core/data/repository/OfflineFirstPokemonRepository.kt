package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cherryzp.cherrypokemon.core.data.model.asEntities
import com.cherryzp.cherrypokemon.core.data.model.asEntity
import com.cherryzp.cherrypokemon.core.data.model.asPokemonType
import com.cherryzp.cherrypokemon.core.data.model.chainId
import com.cherryzp.cherrypokemon.core.data.paging.PokemonRemoteMediator
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** 읽기는 항상 로컬에서 하고, 네트워크는 로컬을 갱신하는 데만 쓴다. */
internal class OfflineFirstPokemonRepository @Inject constructor(
    private val pokemonDao: PokemonDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val localizedNameDao: LocalizedNameDao,
    private val evolutionChainDao: EvolutionChainDao,
    private val network: PokemonNetworkDataSource,
    private val transaction: DatabaseTransactionRunner,
) : PokemonRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemonsStream(): Flow<PagingData<Pokemon>> = Pager(
        config = PagingConfig(pageSize = POKEMON_PAGE_SIZE),
        remoteMediator = PokemonRemoteMediator(pokemonDao, remoteKeyDao, network, transaction),
        pagingSourceFactory = pokemonDao::pagingSource
    ).flow.map { pagingData -> pagingData.map(PokemonEntity::asExternalModel) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?> = combine(
        pokemonDao.getPokemonDetailEntity(pokeId),
        localizedNameDao.getTypeNames(),
        localizedNameDao.getAbilityNames()
    ) { entity, typeNames, abilityNames ->
        entity to entity?.asExternalModel(
            typeNames = typeNames.associateBy { it.type },
            abilityNames = abilityNames.associate { it.asExternalModel() }
        )
    }.flatMapLatest { (entity, detail) ->
        // 체인 ID 는 상세에 들어 있어서 먼저 읽어야 구성원을 읽을 수 있다.
        val chainId = entity?.evolutionChainId ?: 0
        if (detail == null || chainId == 0) {
            flowOf(detail)
        } else {
            evolutionChainDao.getChainMembers(chainId)
                .map { members -> detail.copy(evolutionStages = members.asExternalModel()) }
        }
    }

    override suspend fun refreshPokemonDetail(pokeId: Int) {
        // 상세와 종 정보를 함께 받는다. 하나라도 실패하면 아무것도 쓰지 않는다.
        val detail = network.getPokemonDetail(pokeId)
        val species = network.getPokemonSpecies(pokeId)
        pokemonDao.insertPokemonDetail(detail.asEntity(species))
        refreshMissingNames(detail)
        refreshEvolutionChain(species)
    }

    /**
     * 진화 체인을 아직 받지 않았으면 받아 둔다.
     *
     * 같은 체인의 포켓몬은 하나만 열어보면 나머지도 함께 들어온다.
     * 체인을 받지 못해도 상세는 이미 저장했으므로 실패를 삼킨다.
     */
    private suspend fun refreshEvolutionChain(species: NetworkPokemonSpecies) {
        val chainId = species.evolutionChain?.url?.chainId() ?: return
        if (evolutionChainDao.countChainMembers(chainId) > 0) return

        runCatching {
            evolutionChainDao.insertChainMembers(network.getEvolutionChain(chainId).asEntities())
        }
    }

    /**
     * 아직 받지 않은 타입·특성 이름과 상성을 받아 둔다.
     *
     * 타입은 20여 개, 특성은 포켓몬당 한둘이라 쓸수록 호출이 사라진다.
     * 이름을 받지 못해도 상성 없이 상세는 보이므로 실패를 삼킨다.
     */
    private suspend fun refreshMissingNames(detail: NetworkPokemonDetail) {
        val savedAbilities = localizedNameDao.getSavedAbilities().toSet()
        val missingAbilities = detail.abilities
            .map { it.ability.name }
            .filter { it !in savedAbilities }
            .distinct()

        runCatching {
            val saved = refreshTypes(detail.types.map { it.type.name })
            // 약점·저항으로 나올 타입의 이름도 있어야 칩에 글자를 적을 수 있다.
            refreshTypes(
                saved.flatMap { it.doubleDamageFrom + it.halfDamageFrom + it.noDamageFrom }
                    .map { it.name.lowercase() }
            )
            localizedNameDao.insertAbilityNames(
                missingAbilities.map { network.getAbility(it).asEntity() }
            )
        }
    }

    /** 저장되지 않은 타입만 받아 쓰고, 이번에 받은 것들을 돌려준다. */
    private suspend fun refreshTypes(typeNames: List<String>): List<TypeNameEntity> {
        val savedTypes = localizedNameDao.getSavedTypes().toSet()
        val missing = typeNames
            .distinct()
            .filter { it.asPokemonType() != PokemonType.Unknown }
            .filter { it.asPokemonType().name !in savedTypes }
        if (missing.isEmpty()) return emptyList()

        val entities = missing.map { network.getType(it).asEntity(it.asPokemonType()) }
        localizedNameDao.insertTypeNames(entities)
        return entities
    }

    private companion object {
        const val POKEMON_PAGE_SIZE = 20
    }
}
