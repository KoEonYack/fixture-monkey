package com.navercorp.fixturemonkey.autoparams.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.javaunit.autoparams.AutoSource;
import org.javaunit.autoparams.customization.Customization;
import org.junit.jupiter.params.ParameterizedTest;

import net.jqwik.api.Arbitrary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.autoparams.customization.FixtureMonkeyCustomizer;

class FixtureMonkeyValueCustomizerTest {
	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerWrapper(IntegerWrapperClass value) {
		then(value).isNotNull();
		then(value.getValue()).isGreaterThan(0);
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerListWrapper(IntegerListClass value) {
		then(value).isNotNull();
		then(value.getValues()).isNotNull();
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void stringWrapper(StringWrapperClass value) {
		then(value).isNotNull();
		then(value.getValue()).isNotNull();
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerWrapperList(List<IntegerWrapperClass> values) {
		values.forEach(it -> then(it.getValue()).isGreaterThan(0));
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerListWrapperStream(Stream<IntegerListClass> values) {
		values.limit(5).forEach(it -> then(it.getValues()).isNotNull());
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void stringWrapperMap(Map<String, StringWrapperClass> map) {
		map.forEach((key, value) -> {
			then(key).isNotNull();
			then(value.getValue()).isNotNull();
		});
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerWrapperArbitraryBuilder(ArbitraryBuilder<IntegerWrapperClass> builder) {
		then(builder).isNotNull();
		then(builder.sample().getValue()).isGreaterThan(0);
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerListWrapperArbitraryBuilder(ArbitraryBuilder<IntegerListClass> builder) {
		then(builder).isNotNull();
		then(builder.sample().getValues()).isNotNull();
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void stringWrapperArbitraryBuilder(ArbitraryBuilder<StringWrapperClass> builder) {
		then(builder).isNotNull();
		then(builder.sample().getValue()).isNotNull();
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerWrapperArbitrary(Arbitrary<IntegerWrapperClass> arbitrary) {
		then(arbitrary).isNotNull();
		then(arbitrary.sample().getValue()).isGreaterThan(0);
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void integerListWrapperArbitrary(Arbitrary<IntegerListClass> arbitrary) {
		then(arbitrary).isNotNull();
		then(arbitrary.sample().getValues()).isNotNull();
	}

	@ParameterizedTest
	@AutoSource
	@Customization(FixtureMonkeyCustomizer.class)
	void stringWrapperArbitrary(Arbitrary<StringWrapperClass> arbitrary) {
		then(arbitrary).isNotNull();
		then(arbitrary.sample().getValue()).isNotNull();
	}

	@Data
	public static class IntegerWrapperClass {
		@Positive
		int value;
	}

	@Data
	public static class IntegerListClass {
		List<Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringWrapperClass {
		@NotNull
		private String value;
	}
}
