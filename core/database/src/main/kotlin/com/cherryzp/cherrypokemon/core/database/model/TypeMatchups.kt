package com.cherryzp.cherrypokemon.core.database.model

import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo
import com.cherryzp.cherrypokemon.core.model.TypeMatchup

/**
 * 받는 쪽 상성을 계산한다.
 *
 * 공격 타입마다 방어 타입들의 배율을 곱한다. 1 배는 보여줄 게 없으므로 뺀다.
 *
 * @param defending 이 포켓몬의 타입들
 * @param relations 타입별 상성 캐시. 없는 타입은 1 배로 둔다
 */
fun typeMatchups(
    defending: List<PokemonType>,
    relations: Map<String, TypeNameEntity>,
): List<TypeMatchup> {
    if (defending.isEmpty()) return emptyList()

    return PokemonType.entries
        .filter { it != PokemonType.Unknown }
        .map { attacking ->
            val multiplier = defending.fold(1f) { acc, defender ->
                acc * (relations[defender.name]?.multiplierAgainst(attacking) ?: 1f)
            }
            TypeMatchup(
                type = PokemonTypeInfo(
                    type = attacking,
                    name = relations[attacking.name]
                        ?.let { LocalizedText(korean = it.korean, english = it.english) }
                        ?: LocalizedText.Empty
                ),
                multiplier = multiplier
            )
        }
        .filter { it.multiplier != 1f }
        .sortedByDescending { it.multiplier }
}

private fun TypeNameEntity.multiplierAgainst(attacking: PokemonType): Float = when (attacking) {
    in noDamageFrom -> 0f
    in doubleDamageFrom -> 2f
    in halfDamageFrom -> 0.5f
    else -> 1f
}
