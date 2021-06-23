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

data class KotlinDataObject(val name: String, val description: String?, @field:NotNull val address: String?)
