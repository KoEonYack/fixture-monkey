package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkeyBuilder

class KFixtureMonkeyBuilder : FixtureMonkeyBuilder() {
    init {
        nullableArbitraryEvaluator(KotlinNullableArbitraryEvaluator())
    }
}
