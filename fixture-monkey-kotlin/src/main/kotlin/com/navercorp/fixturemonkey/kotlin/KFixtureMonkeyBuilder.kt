package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.kotlin.generator.PrimaryConstructorArbitraryGenerator

class KFixtureMonkeyBuilder : FixtureMonkeyBuilder() {
    init {
        nullableArbitraryEvaluator(KotlinNullableArbitraryEvaluator())
        defaultGenerator(PrimaryConstructorArbitraryGenerator.INSTANCE)
    }
}
