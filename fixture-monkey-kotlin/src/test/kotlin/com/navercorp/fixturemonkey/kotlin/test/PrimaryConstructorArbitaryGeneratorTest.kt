package com.navercorp.fixturemonkey.kotlin.test

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KFixtureMonkeyBuilder
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then

class PrimaryConstructorArbitaryGeneratorTest {
    private val sut: FixtureMonkey = KFixtureMonkeyBuilder()
        .build()

    @Property
    fun giveMeClassWithPrimaryConstructor() {
        val actual = this.sut.giveMe(ClassWithPrimaryConstructor::class.java, 10)
        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithNestedOne() {
        val actual = this.sut.giveMe(ClassWithNestedOne::class.java, 10)
        then(actual).hasSize(10)
    }

    @Property
    fun giveMeDataClass() {
        val actual = this.sut.giveMe(DataClass::class.java, 10)
        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithVarValue() {
        val actual = this.sut.giveMe(ClassWithVarValue::class.java, 10)
        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithNullable() {
        val actual = this.sut.giveMe(ClassWithNullable::class.java, 10)
        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithDefaultValue() {
        val actual = this.sut.giveMe(ClassWithDefaultValue::class.java, 10)
        then(actual).hasSize(10).allSatisfy {
            with(it) {
                then(stringValue).isNotEqualTo("default_value")
            }
        }
    }

    @Property
    fun giveMeClassWithSecondaryConstructor() {
        val actual = this.sut.giveMe(ClassWithSecondaryConstructor::class.java, 10)
        then(actual).hasSize(10).allSatisfy {
            with(it) {
                then(stringValue).isNotEqualTo("default_value")
            }
        }
    }

    @Property
    fun giveMeInterfaceClass() {
        val actual = this.sut.giveMeOne(InterfaceClass::class.java)
        then(actual).isNull()
    }

    class ClassWithPrimaryConstructor(
        val intValue: Int,
        val stringValue: String
    )

    class NestedClass(
        val intValue: Int
    )

    class ClassWithNestedOne(
        val nestedClass: NestedClass
    )

    data class DataClass(
        val intValue: Int,
        val stringValue: String
    )

    class ClassWithVarValue(
        var intValue: Int,
        var stringValue: String
    )

    class ClassWithNullable(
        val intValue: Int,
        val stringValue: String?
    )

    class ClassWithDefaultValue(
        val intValue: Int,
        val stringValue: String = "default_value"
    )

    class ClassWithSecondaryConstructor(
        val intValue: Int,
        val stringValue: String
    ) {
        constructor(another: String) : this(0, "default_value")
    }

    interface InterfaceClass {
        fun test()
    }
}
