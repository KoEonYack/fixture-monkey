package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;

public class ArbitraryCustomizerTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.addCustomizer(CustomizerIntegerClass.class, new ArbitraryCustomizer<CustomizerIntegerClass>() {
			@Override
			public void customizeFields(Class<CustomizerIntegerClass> type, FieldArbitraries fieldArbitraries) {
				fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
			}

			@Override
			public CustomizerIntegerClass customizeFixture(CustomizerIntegerClass object) {
				return object;
			}
		})
		.build();

	@Provide
	Arbitrary<CustomizerIntegerClass> withCustomizer() {
		return this.sut.giveMeBuilder(CustomizerIntegerClass.class).build();
	}

	@Property
	void giveMeWithCustomizer(@ForAll("withCustomizer") CustomizerIntegerClass actual) {
		then(actual.value).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> customize() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.customize(IntegerWrapperClass.class, new ArbitraryCustomizer<IntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<IntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public IntegerWrapperClass customizeFixture(@Nullable IntegerWrapperClass object) {
					return object;
				}
			}).build();
	}

	@Property
	void giveMeCustomize(@ForAll("customize") IntegerWrapperClass actual) {
		then(actual.value).isEqualTo(1);
	}

	@Property
	void nestedCustomize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addCustomizer(StringWrapperClass.class, it -> new StringWrapperClass("test"))
			.addCustomizer(IntegerWrapperClass.class, it -> {
				IntegerWrapperClass integerWrapperClass = new IntegerWrapperClass();
				integerWrapperClass.value = -1;
				return integerWrapperClass;
			})
			.build();

		StringIntegerClass actual = sut.giveMeBuilder(StringIntegerClass.class)
			.setNotNull("value1")
			.setNotNull("value2")
			.sample();

		then(actual.value1.value).isEqualTo("test");
		then(actual.value2.value).isEqualTo(-1);
	}

	@Data
	public static class CustomizerIntegerClass {
		Integer value;
	}

	@Data
	public static class StringIntegerClass {
		StringWrapperClass value1;
		IntegerWrapperClass value2;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringWrapperClass {
		private String value;
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}
}
