package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import javax.validation.constraints.NotNull

class KotlinNullableArbitraryEvaluatorTest {
    private val fixture: FixtureMonkey = KFixtureMonkeyBuilder().build()

    @Property
    fun generate() {
        val actual = this.fixture.giveMe(KotlinDataObject::class.java, 10)
        actual.forEach {
            then(it.name).isNotNull
            then(it.address).isNotNull
        }
    }
}

class KotlinDataObject {
    @field:NotNull var name: String? = null
    var description: String? = null
    @field:NotNull var address: String? = null
}
