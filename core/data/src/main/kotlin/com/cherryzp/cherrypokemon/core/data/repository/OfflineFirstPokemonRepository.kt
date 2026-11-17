package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cherryzp.cherrypokemon.core.data.model.asEntities
import com.cherryzp.cherrypokemon.core.data.model.asEntity
import com.cherryzp.cherrypokemon.core.data.model.asItemEntity
import com.cherryzp.cherrypokemon.core.data.model.asPokemonType
import com.cherryzp.cherrypokemon.core.data.model.chainId
import com.cherryzp.cherrypokemon.core.data.model.urlId
import com.cherryzp.cherrypokemon.core.data.paging.PokemonRemoteMediator
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonWithName
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
    ).flow.map { pagingData -> pagingData.map(PokemonWithName::asExternalModel) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchPokemonsStream(query: String): Flow<PagingData<Pokemon>> = Pager(
        config = PagingConfig(pageSize = POKEMON_PAGE_SIZE),
        // 색인이 이미 로컬에 있으므로 네트워크를 보지 않는다.
        pagingSourceFactory = { pokemonDao.searchPagingSource(query) }
    ).flow.map { pagingData -> pagingData.map(PokemonWithName::asExternalModel) }

    override suspend fun ensurePokemonIndex() {
        val page = network.getPokemons(offset = 0, limit = POKEMON_INDEX_LIMIT)
        // 이미 다 받아 뒀으면 다시 쓰지 않는다.
        if (pokemonDao.countPokemons() >= page.count) return
        transaction {
            pokemonDao.insertPokemons(page.results.mapNotNull { it.asEntity() })
            // 전부 받았으므로 목록이 끝에 닿아도 네트워크에서 더 받을 것이 없다.
            remoteKeyDao.upsertRemoteKey(RemoteKeyEntity(nextOffset = null))
        }
    }

    override suspend fun ensurePokemonNames() {
        if (localizedNameDao.countPokemonNames() > 0) return
        localizedNameDao.insertPokemonNames(network.getPokemonNames().asEntities())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?> {
        // Room 은 테이블에 쓰기만 있어도 내용이 같은 값을 다시 흘린다. 다른 포켓몬을 저장할 때도 그렇다.
        val entity = pokemonDao.getPokemonDetailEntity(pokeId).distinctUntilChanged()

        val detail = combine(
            entity,
            localizedNameDao.getTypeNames(),
            localizedNameDao.getAbilityNames(),
            localizedNameDao.getPokemonName(pokeId)
        ) { entity, typeNames, abilityNames, name ->
            val detail = entity?.asExternalModel(
                typeNames = typeNames.associateBy { it.type },
                abilityNames = abilityNames.associate { it.asExternalModel() }
            )
            // 종 정보의 이름은 종 이름(리자몽)이라 폼(메가리자몽X)에는 맞지 않는다. 받아 둔 이름을 쓴다.
            if (detail != null && name != null && name.korean.isNotEmpty()) {
                detail.copy(localizedName = LocalizedText(name.korean, name.english))
            } else {
                detail
            }
        }

        // 체인 ID 는 상세에 들어 있어서 먼저 읽어야 구성원을 읽을 수 있다. ID 가 바뀔 때만 구성원
        // 쿼리를 다시 연다. 이름이 하나 더 저장될 때마다 새로 열 이유가 없다.
        val evolutionStages = entity
            .map { it?.evolutionChainId ?: 0 }
            .distinctUntilChanged()
            .flatMapLatest { chainId ->
                if (chainId == 0) {
                    flowOf(emptyList())
                } else {
                    evolutionChainDao.getChainMembers(chainId)
                }
            }
            .map { members -> members.asExternalModel() }

        return combine(detail, evolutionStages) { detail, stages ->
            detail?.copy(evolutionStages = stages)
        }.distinctUntilChanged()
    }

    override suspend fun refreshPokemonDetail(pokeId: Int) {
        val (detail, species) = httpErrorAsIOException { fetchDetailAndSpecies(pokeId) }
        pokemonDao.insertPokemonDetail(detail.asEntity(species))
        // 이름과 진화 체인은 서로 기다릴 이유가 없다.
        coroutineScope {
            launch { refreshMissingNames(detail) }
            launch { refreshEvolutionChain(species) }
        }
    }

    /**
     * 상세와 종 정보를 받는다. 하나라도 실패하면 다른 쪽도 취소하고 아무것도 쓰지 않는다.
     *
     * 기본 모습(1~1025 번)은 종 번호가 같아 함께 받는다. 폼(10001 번부터)은 같은 번호의 종이
     * 없어(404) 상세를 먼저 받고 거기 적힌 종 주소를 따라간다.
     */
    private suspend fun fetchDetailAndSpecies(
        pokeId: Int,
    ): Pair<NetworkPokemonDetail, NetworkPokemonSpecies> = coroutineScope {
        val detail = async { network.getPokemonDetail(pokeId) }
        val species = if (pokeId < FIRST_FORM_ID) {
            async { network.getPokemonSpecies(pokeId) }
        } else {
            null
        }
        val received = detail.await()
        val speciesId = received.species.url.urlId() ?: pokeId
        received to (species?.await() ?: network.getPokemonSpecies(speciesId))
    }

    /**
     * HTTP 오류(404, 5xx)를 [IOException] 으로 바꿔 던진다.
     *
     * 상태를 만드는 ViewModel 은 Retrofit 을 모르므로 [HttpException] 을 잡지 못한다. 그대로 두면
     * 잡히지 않은 예외로 앱이 죽는다. 연결 실패와 똑같이 갱신 실패로 보여주면 된다.
     */
    private suspend fun <T> httpErrorAsIOException(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        throw IOException("HTTP ${e.code()}", e)
    }

    /**
     * 진화 체인을 아직 받지 않았으면 받아 둔다.
     *
     * 같은 체인의 포켓몬은 하나만 열어보면 나머지도 함께 들어온다.
     * 체인을 받지 못해도 상세는 이미 저장했으므로 실패를 삼킨다.
     */
    private suspend fun refreshEvolutionChain(species: NetworkPokemonSpecies) {
        val chainId = species.evolutionChain?.url?.chainId() ?: return

        runCatching {
            if (evolutionChainDao.countChainMembers(chainId) == 0) {
                evolutionChainDao.insertChainMembers(
                    network.getEvolutionChain(chainId).asEntities()
                )
            }
            // 체인을 전에 받아 둔 경우에도 도구 이름은 없을 수 있다. 그래서 체인과 따로 본다.
            refreshItemNames(chainId)
        }
    }

    /** 체인에 쓰이는 진화 도구 중 이름을 아직 받지 않은 것만 받는다. */
    private suspend fun refreshItemNames(chainId: Int) {
        val saved = localizedNameDao.getSavedItems().toSet()
        val missing = evolutionChainDao.getChainItems(chainId).filter { it !in saved }
        if (missing.isEmpty()) return

        val entities = coroutineScope {
            missing.map { async { network.getItem(it).asItemEntity() } }.awaitAll()
        }
        localizedNameDao.insertItemNames(entities)
    }

    /**
     * 아직 받지 않은 타입·특성 이름과 상성을 받아 둔다.
     *
     * 타입은 20여 개, 특성은 포켓몬당 한둘이라 쓸수록 호출이 사라진다.
     * 처음 여는 포켓몬은 약점·저항 타입까지 열 개 넘게 받을 수 있어 한꺼번에 요청한다.
     * 이름을 받지 못해도 상성 없이 상세는 보이므로 실패를 삼킨다.
     */
    private suspend fun refreshMissingNames(detail: NetworkPokemonDetail) {
        runCatching {
            coroutineScope {
                launch {
                    val saved = refreshTypes(detail.types.map { it.type.name })
                    // 약점·저항으로 나올 타입의 이름도 있어야 칩에 글자를 적을 수 있다.
                    // 그 목록은 자기 타입을 받아야 알 수 있어서 이 둘만은 차례로 받는다.
                    refreshTypes(
                        saved.flatMap { it.doubleDamageFrom + it.halfDamageFrom + it.noDamageFrom }
                            .map { it.name.lowercase() }
                    )
                }
                launch { refreshAbilities(detail) }
            }
        }
    }

    private suspend fun refreshAbilities(detail: NetworkPokemonDetail) {
        val savedAbilities = localizedNameDao.getSavedAbilities().toSet()
        val missing = detail.abilities
            .map { it.ability.name }
            .filter { it !in savedAbilities }
            .distinct()
        if (missing.isEmpty()) return

        val entities = coroutineScope {
            missing.map { async { network.getAbility(it).asEntity() } }.awaitAll()
        }
        localizedNameDao.insertAbilityNames(entities)
    }

    /** 저장되지 않은 타입만 받아 쓰고, 이번에 받은 것들을 돌려준다. */
    private suspend fun refreshTypes(typeNames: List<String>): List<TypeNameEntity> {
        val savedTypes = localizedNameDao.getSavedTypes().toSet()
        val missing = typeNames
            .distinct()
            .filter { it.asPokemonType() != PokemonType.Unknown }
            .filter { it.asPokemonType().name !in savedTypes }
        if (missing.isEmpty()) return emptyList()

        val entities = coroutineScope {
            missing.map { async { network.getType(it).asEntity(it.asPokemonType()) } }.awaitAll()
        }
        localizedNameDao.insertTypeNames(entities)
        return entities
    }

    private companion object {
        const val POKEMON_PAGE_SIZE = 20

        /** 이 번호부터는 폼이다. 종 번호가 포켓몬 번호와 다르다. */
        const val FIRST_FORM_ID = 10001

        /** 전체 포켓몬 수보다 넉넉하게. 한 번에 받아 이름 색인을 만든다. */
        const val POKEMON_INDEX_LIMIT = 2000
    }
}
