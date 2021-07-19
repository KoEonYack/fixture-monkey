package com.navercorp.fixturemonkey.kotlin.customizer

import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers
import com.navercorp.fixturemonkey.generator.FieldArbitraries
import kotlin.reflect.KClass

internal fun <T : Any> ArbitraryCustomizers.customizeFields(
    type: KClass<T>,
    fieldArbitraries: FieldArbitraries
): Unit = this.customizeFields(type.java, fieldArbitraries)
