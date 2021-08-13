package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.ArbitraryOption
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer
import com.navercorp.fixturemonkey.generator.FieldArbitraries
import com.navercorp.fixturemonkey.kotlin.customizer.KArbitraryCustomizer
import net.jqwik.api.Arbitrary
import kotlin.reflect.KClass
import kotlin.streams.asSequence

inline fun <reified T : Any> FixtureMonkey.giveMe(): Sequence<T> = this.giveMe(T::class.java).asSequence()

inline fun <reified T : Any> FixtureMonkey.giveMe(
    customizer: KArbitraryCustomizer<T>
): Sequence<T> = this.giveMe(T::class.java, customizer.toArbitraryCustomizer()).asSequence()

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMe(size: Int)")
)
inline fun <reified T : Any> FixtureMonkey.giveMe(type: KClass<T>, size: Int): List<T> =
    this.giveMe(type.java, size)

inline fun <reified T : Any> FixtureMonkey.giveMe(size: Int): List<T> =
    this.giveMe(T::class.java, size)

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMe(size: Int, customizer: KArbitraryCustomizer<T>)")
)
inline fun <reified T : Any> FixtureMonkey.giveMe(
    type: KClass<T>,
    size: Int,
    customizer: KArbitraryCustomizer<T>
): List<T> = this.giveMe(type.java, size, customizer.toArbitraryCustomizer())

inline fun <reified T : Any> FixtureMonkey.giveMe(
    size: Int,
    customizer: KArbitraryCustomizer<T>
): List<T> = this.giveMe(T::class.java, size, customizer.toArbitraryCustomizer())

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMeOne()")
)
inline fun <reified T : Any> FixtureMonkey.giveMeOne(type: KClass<T>): T = this.giveMeOne(type.java)

inline fun <reified T : Any> FixtureMonkey.giveMeOne(): T = this.giveMeOne(T::class.java)

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMeOne(customizer: KArbitraryCustomizer<T>)")
)
inline fun <reified T : Any> FixtureMonkey.giveMeOne(
    type: KClass<T>,
    customizer: KArbitraryCustomizer<T>
): T = this.giveMeOne(type.java, customizer.toArbitraryCustomizer())

inline fun <reified T : Any> FixtureMonkey.giveMeOne(
    customizer: KArbitraryCustomizer<T>
): T = this.giveMeOne(T::class.java, customizer.toArbitraryCustomizer())

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMeArbitrary()")
)
inline fun <reified T : Any> FixtureMonkey.giveMeArbitrary(
    type: KClass<T>
): Arbitrary<T> = this.giveMeArbitrary(type.java)

inline fun <reified T : Any> FixtureMonkey.giveMeArbitrary(): Arbitrary<T> = this.giveMeArbitrary(T::class.java)

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMeBuilder()")
)
inline fun <reified T : Any> FixtureMonkey.giveMeArbitraryBuilder(
    type: KClass<T>
): ArbitraryBuilder<T> = this.giveMeBuilder(type.java)

inline fun <reified T : Any> FixtureMonkey.giveMeBuilder(): ArbitraryBuilder<T> = this.giveMeBuilder(T::class.java)

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith(expression = "giveMeBuilder(options: ArbitraryOption)")
)
inline fun <reified T : Any> FixtureMonkey.giveMeArbitraryBuilder(
    type: KClass<T>,
    options: ArbitraryOption
): ArbitraryBuilder<T> = this.giveMeBuilder(type.java, options)

inline fun <reified T : Any> FixtureMonkey.giveMeBuilder(
    options: ArbitraryOption
): ArbitraryBuilder<T> = this.giveMeBuilder(T::class.java, options)

@Deprecated(
    message = "Will be removed in 1.0.0",
    replaceWith = ReplaceWith("giveMeBuilder(value: T)")
)
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
