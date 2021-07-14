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

data class KotlinDataObject(
    @field:NotNull var name: String? = null,
    @field:NotNull val address: String? = null,
    var description: String? = null,
    var time: String?,
    val company: String? = null,
    val money: Int?,
)
