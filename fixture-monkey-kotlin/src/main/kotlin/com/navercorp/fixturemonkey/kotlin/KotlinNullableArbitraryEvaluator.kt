package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.arbitrary.NullableArbitraryEvaluator
import java.lang.reflect.Field
import kotlin.reflect.jvm.kotlinProperty

class KotlinNullableArbitraryEvaluator : NullableArbitraryEvaluator {
    override fun isNullable(field: Field): Boolean {
        return field.kotlinProperty?.returnType?.isMarkedNullable ?: true
    }
}
