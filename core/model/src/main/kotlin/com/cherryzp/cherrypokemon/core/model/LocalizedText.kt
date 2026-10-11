package com.cherryzp.cherrypokemon.core.model

/**
 * 언어별 문자열.
 *
 * PokeAPI 가 한 번의 응답에 여러 언어를 함께 주므로 둘 다 저장해 둔다.
 * 언어를 바꿔도 다시 받지 않고, 비행기 모드에서도 바뀐다.
 */
data class LocalizedText(val korean: String, val english: String) {
    /** 고른 언어로 읽는다. 그 언어가 비어 있으면 다른 언어로 떨어뜨린다. */
    fun of(language: AppLanguage): String = when (language) {
        AppLanguage.KOREAN -> korean.ifEmpty { english }
        AppLanguage.ENGLISH -> english.ifEmpty { korean }
    }

    val isEmpty: Boolean get() = korean.isEmpty() && english.isEmpty()

    companion object {
        val Empty = LocalizedText(korean = "", english = "")
    }
}
