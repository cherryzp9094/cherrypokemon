package com.cherryzp.cherrypokemon.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/** 화면보다 오래 살아야 하는 작업에 쓰는 애플리케이션 수명의 [kotlinx.coroutines.CoroutineScope]. */
@Qualifier
@Retention(BINARY)
annotation class ApplicationScope
