package com.cherryzp.cherrypokemon.core.model

/**
 * 앱에서 고를 수 있는 언어.
 *
 * 기기 언어를 따르는 선택지는 두지 않는다. 처음 실행할 때 기기 언어로 정해 두고,
 * 그다음부터는 사용자가 고른 것만 쓴다.
 *
 * @param tag 리소스와 PokeAPI 응답에서 쓰는 언어 코드
 */
enum class AppLanguage(val tag: String) {
    KOREAN("ko"),
    ENGLISH("en"),
    ;

    companion object {
        /** 기기 언어가 한국어면 한국어, 아니면 영어로 시작한다. */
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { tag?.startsWith(it.tag) == true } ?: ENGLISH
    }
}
