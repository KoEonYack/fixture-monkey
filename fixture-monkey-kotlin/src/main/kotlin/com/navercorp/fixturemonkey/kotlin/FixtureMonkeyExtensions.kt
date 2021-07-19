package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.ArbitraryOption
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer
import com.navercorp.fixturemonkey.generator.FieldArbitraries
import com.navercorp.fixturemonkey.kotlin.customizer.KArbitraryCustomizer
import net.jqwik.api.Arbitrary
import kotlin.reflect.KClass

inline fun <reified T : Any> FixtureMonkey.giveMe(type: KClass<T>, size: Int): List<T> = this.giveMe(type.java, size)

inline fun <reified T : Any> FixtureMonkey.giveMe(
    type: KClass<T>,
    size: Int,
    customizer: KArbitraryCustomizer<T>
): List<T> = this.giveMe(type.java, size, customizer.toArbitraryCustomizer())

inline fun <reified T : Any> FixtureMonkey.giveMeOne(type: KClass<T>): T = this.giveMeOne(type.java)

inline fun <reified T : Any> FixtureMonkey.giveMeOne(
    type: KClass<T>,
    customizer: KArbitraryCustomizer<T>
): T = this.giveMeOne(type.java, customizer.toArbitraryCustomizer())

inline fun <reified T : Any> FixtureMonkey.giveMeArbitrary(
    type: KClass<T>
): Arbitrary<T> = this.giveMeArbitrary(type.java)

inline fun <reified T : Any> FixtureMonkey.giveMeArbitraryBuilder(
    type: KClass<T>
): ArbitraryBuilder<T> = this.giveMeBuilder(type.java)

inline fun <reified T : Any> FixtureMonkey.giveMeArbitraryBuilder(
    type: KClass<T>,
    options: ArbitraryOption
): ArbitraryBuilder<T> = this.giveMeBuilder(type.java, options)

inline fun <reified T : Any> FixtureMonkey.giveMeArbitraryBuilder(
    value: T
): ArbitraryBuilder<T> = this.giveMeBuilder(value)

inline fun <reified T : Any> KArbitraryCustomizer<T>.toArbitraryCustomizer(): ArbitraryCustomizer<T> {
    return object : ArbitraryCustomizer<T> {
        private val delegate = this@toArbitraryCustomizer

        override fun customizeFields(type: Class<T>, fieldArbitraries: FieldArbitraries) {
            delegate.customizeFields(type.kotlin, fieldArbitraries)
        }

        override fun customizeFixture(target: T?): T? {
            return delegate.customizeFixture(target)
        }
    }
}
