package com.navercorp.fixturemonkey.kotlin.customizer

import com.navercorp.fixturemonkey.generator.FieldArbitraries
import kotlin.reflect.KClass

interface KArbitraryCustomizer<T : Any> {
    fun customizeFields(type: KClass<T>, fieldArbitraries: FieldArbitraries) {}

    fun customizeFixture(target: T?): T?
}
