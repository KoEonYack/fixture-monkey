package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.generator.JacksonArbitraryGenerator
import com.navercorp.fixturemonkey.kotlin.KotlinNullableArbitraryEvaluator

class KFixtureMonkeyBuilder : FixtureMonkeyBuilder() {
    init {
        nullableArbitraryEvaluator(KotlinNullableArbitraryEvaluator())
    }
}
