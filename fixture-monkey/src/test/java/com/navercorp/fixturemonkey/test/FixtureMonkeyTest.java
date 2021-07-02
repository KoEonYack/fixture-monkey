package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryBuilders;
import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;
import com.navercorp.fixturemonkey.generator.FieldReflectionArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.NullArbitraryGenerator;

class FixtureMonkeyTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
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
		.putGenerator(BuilderIntegerClass.class, BuilderArbitraryGenerator.INSTANCE)
		.putGenerator(FieldReflectionIntegerClass.class, FieldReflectionArbitraryGenerator.INSTANCE)
		.putGenerator(NullIntegerClass.class, NullArbitraryGenerator.INSTANCE)
		.putGenerator(BeanIntegerClass.class, BeanArbitraryGenerator.INSTANCE)
		.build();

	@Property
	void giveMeRegisteredReference() {
		Integer actual = this.sut.giveMeOne(Integer.class);

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeRegisteredPrimitive() {
		ArbitraryBuilder<Integer> builder = this.sut.giveMeBuilder(Integer.class);

		int actual = builder.sample();

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWithAnnotation() {
		IntegerWrapperClassWithAnnotation actual = this.sut.giveMeOne(IntegerWrapperClassWithAnnotation.class);

		then(actual.getValue()).isPositive();
	}

	@Property
	void giveMeWithCustomAnnotatedArbitraryGenerator() {
		IntegerWrapperClass value = new IntegerWrapperClass();
		value.setValue(1);

		ArbitraryOption customOption = ArbitraryOption.builder()
			.addAnnotatedArbitraryGenerator(
				IntegerWrapperClass.class,
				annotationSource -> Arbitraries.just(value)
			)
			.build();
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.options(customOption)
			.build();

		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual.value).isEqualTo(1);
	}

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
	void giveMeWhenPutBuilderArbitraryGenerator() {
		BuilderIntegerClass actual = this.sut.giveMeOne(BuilderIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<BuilderIntegerClass> whenDefaultGeneratorIsBuilderArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		return sut.giveMeBuilder(BuilderIntegerClass.class).build();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGenerator(
		@ForAll("whenDefaultGeneratorIsBuilderArbitraryGenerator") BuilderIntegerClass actual
	) {
		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<FieldReflectionIntegerClass> whenDefaultGeneratorIsFieldReflectionArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.build();
		return sut.giveMeBuilder(FieldReflectionIntegerClass.class).build();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsFieldReflectionArbitraryGenerator(
		@ForAll("whenDefaultGeneratorIsFieldReflectionArbitraryGenerator") FieldReflectionIntegerClass actual
	) {
		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutFieldReflectionArbitraryGenerator() {
		FieldReflectionIntegerClass actual = this.sut.giveMeOne(FieldReflectionIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsNullArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(NullArbitraryGenerator.INSTANCE)
			.build();

		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenPutNullArbitraryGenerator() {
		NullIntegerClass actual = this.sut.giveMeOne(NullIntegerClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBeanArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		BeanIntegerClass actual = sut.giveMeOne(BeanIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutBeanArbitraryGenerator() {
		BeanIntegerClass actual = this.sut.giveMeOne(BeanIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<BuilderIntegerClass> whenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		return sut.giveMeBuilder(BuilderIntegerClass.class)
			.customize(BuilderIntegerClass.class, new ArbitraryCustomizer<BuilderIntegerClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.putArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public BuilderIntegerClass customizeFixture(@Nullable BuilderIntegerClass fixture) {
					return fixture;
				}
			})
			.build();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer(
		@ForAll("whenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer") BuilderIntegerClass actual
	) {
		then(actual.value).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specSet() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", -1))
			.build();
	}

	@Property
	void giveMeSpecSet(@ForAll("specSet") IntegerWrapperClass actual) {
		then(actual.getValue()).isEqualTo(-1);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specSetArbitrary() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.build();
	}

	@Property
	void giveMeSpecSetArbitrary(@ForAll("specSetArbitrary") IntegerWrapperClass actual) {
		then(actual.value).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> listSize() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.build();
	}

	@Property
	void giveMeListSize(@ForAll("listSize") IntegerListClass actual) {
		then(actual.values).hasSize(1);
	}

	@Provide
	Arbitrary<IntegerListClass> setNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().setNull("values"))
			.build();
	}

	@Property
	void giveMeSetNull(@ForAll("setNull") IntegerListClass actual) {
		then(actual.values).isNull();
	}

	@Provide
	Arbitrary<IntegerListClass> sizeAfterSetNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
			)
			.build();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull(@ForAll("sizeAfterSetNull") IntegerListClass actual) {
		then(actual.values).isNull();
	}

	@Provide
	Arbitrary<IntegerListClass> setAfterSetNullReturnsNotNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.build();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull(@ForAll("setAfterSetNullReturnsNotNull") IntegerListClass actual) {
		then(actual.values).isNotNull();
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(0);
	}

	@Provide
	Arbitrary<IntegerListClass> setNotNullAfterSetNullReturnsNotNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.build();
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull(
		@ForAll("setNotNullAfterSetNullReturnsNotNull") IntegerListClass actual
	) {
		then(actual.values).isNotNull();
	}

	@Provide
	Arbitrary<IntegerListClass> setNullAfterSetNotNullReturnsNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.build();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull(@ForAll("setNullAfterSetNotNullReturnsNull") IntegerListClass actual) {
		then(actual.values).isNull();
	}

	@Provide
	Arbitrary<IntegerListClass> setNullAfterSetReturnsNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.build();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull(@ForAll("setNullAfterSetReturnsNull") IntegerListClass actual) {
		then(actual.values).isNull();
	}

	@Provide
	Arbitrary<StringWrapperClass> specSetPrefix() {
		return this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setPrefix("value", "prefix"))
			.build();
	}

	@Property
	void giveMeSpecSetPrefix(@ForAll("specSetPrefix") StringWrapperClass actual) {
		then(actual.value).startsWith("prefix");
	}

	@Provide
	Arbitrary<StringWrapperClass> specSetSuffix() {
		return this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setSuffix("value", "suffix"))
			.build();
	}

	@Property
	void giveMeSpecSetSuffix(@ForAll("specSetSuffix") StringWrapperClass actual) {
		then(actual.value).endsWith("suffix");
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specFilter() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().<Integer>filter(
				"value",
				value -> value >= 0 && value <= 100
			))
			.build();
	}

	@Property
	void giveMeSpecFilter(@ForAll("specFilter") IntegerWrapperClass actual) {
		then(actual.value).isBetween(0, 100);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specFilterType() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().filterInteger(
				"value",
				value -> value >= 0 && value <= 100
			))
			.build();
	}

	@Property
	void giveMeSpecFilterType(@ForAll("specFilterType") IntegerWrapperClass actual) {
		then(actual.value).isBetween(0, 100);
	}

	@Provide
	Arbitrary<IntegerListClass> filterIndex() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.filterInteger("values[0]", value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.build();
	}

	@Property
	void giveMeFilterIndex(@ForAll("filterIndex") IntegerListClass actual) {
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isBetween(0, 100);
	}

	// @Provide
	// Arbitrary<IntegerWrapperClass> objectToBuilder() {
	// 	IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);
	//
	// 	return this.sut.giveMeBuilder(expected).build();
	// }
	//
	// @Property
	// void giveMeObjectToBuilder(@ForAll("objectToBuilder") IntegerWrapperClass actual) {
	// }

	@Provide
	Arbitrary<IntegerWrapperClass> objectToBuilderSet() {
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		ArbitraryBuilder<IntegerWrapperClass> actual = this.sut.giveMeBuilder(expected)
			.set("value", 1);

		return actual.build();
	}

	@Property
	void giveMeObjectToBuilderSet(@ForAll("objectToBuilderSet") IntegerWrapperClass actual) {
		then(actual.value).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> objectToBuilderSetIndex() {
		IntegerListClass expected = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		return this.sut.giveMeBuilder(expected)
			.set("values[1]", 1)
			.build();
	}

	@Property
	void giveMeObjectToBuilderSetIndex(@ForAll("objectToBuilderSetIndex") IntegerListClass actual) {
		then(actual.values.get(1)).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerArrayClass> arrayToBuilder() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeArrayToBuilder(@ForAll("arrayToBuilder") IntegerArrayClass actual) {
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<IntArrayClass> primitiveArrayToBuilder() {
		IntArrayClass expected = new IntArrayClass(new int[] {1, 2, 3});

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMePrimitiveArrayToBuilder(@ForAll("primitiveArrayToBuilder") IntArrayClass actual) {
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<MapKeyIntegerValueIntegerClass> sameKeyValueMapToBuilder() {
		Map<Integer, Integer> values = new HashMap<>();
		values.put(1, 1);
		MapKeyIntegerValueIntegerClass expected = new MapKeyIntegerValueIntegerClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeSameKeyValueMapToBuilder(
		@ForAll("sameKeyValueMapToBuilder") MapKeyIntegerValueIntegerClass actual
	) {
		then(actual.values.get(1)).isEqualTo(1);
	}

	@Provide
	Arbitrary<MapKeyIntegerValueStringClass> diffKeyValueMapToBuilder() {
		Map<Integer, String> values = new HashMap<>();
		values.put(1, "1");
		MapKeyIntegerValueStringClass expected = new MapKeyIntegerValueStringClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeDiffKeyValueMapToBuilder(@ForAll("diffKeyValueMapToBuilder") MapKeyIntegerValueStringClass actual) {
		then(actual.values.get(1)).isEqualTo("1");
	}

	@Provide
	Arbitrary<MapEntryKeyIntegerValueStringClass> mapEntryToBuilder() {
		Map.Entry<Integer, String> value = new SimpleEntry<>(1, "1");
		MapEntryKeyIntegerValueStringClass expected = new MapEntryKeyIntegerValueStringClass(value);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeMapEntryToBuilder(@ForAll("mapEntryToBuilder") MapEntryKeyIntegerValueStringClass actual) {
		then(actual.value.getValue()).isEqualTo("1");
	}

	@Provide
	Arbitrary<IntegerSetClass> setToBuilder() {
		Set<Integer> values = new HashSet<>();
		values.add(1);
		IntegerSetClass expected = new IntegerSetClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeSetToBuilder(@ForAll("setToBuilder") IntegerSetClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerIterableClass> iterableToBuilder() {
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIterableClass expected = new IntegerIterableClass(values);

		return sut.giveMeBuilder(expected)
			.build();
	}

	@Property
	void giveMeIterableToBuilder(@ForAll("iterableToBuilder") IntegerIterableClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerIteratorClass> iteratorToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIteratorClass expected = new IntegerIteratorClass(values.iterator());

		return sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeIteratorToBuilder(@ForAll("iteratorToBuilder") IntegerIteratorClass actual) {
		then(actual.values.next()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerStreamClass> streamToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerStreamClass expected = new IntegerStreamClass(values.stream());

		return sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeStreamToBuilder(@ForAll("streamToBuilder") IntegerStreamClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerOptionalClass> optionalToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		return sut.giveMeBuilder(new IntegerOptionalClass(Optional.of(1))).build();
	}

	@Property
	void giveMeOptionalToBuilder(@ForAll("optionalToBuilder") IntegerOptionalClass actual) {
		//noinspection OptionalGetWithoutIsPresent
		then(actual.value.get()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerOptionalClass> optionalEmptyToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		Optional<Integer> value = Optional.empty();
		IntegerOptionalClass expected = new IntegerOptionalClass(value);

		return sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeOptionalEmptyToBuilder(@ForAll("optionalEmptyToBuilder") IntegerOptionalClass actual) {
		then(actual.value).isEqualTo(Optional.empty());
	}

	@Provide
	Arbitrary<IntegerWrapperClass> setLimit() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", 1, 0)
			.build();
	}

	@Property
	void giveMeSetLimitReturnsNotSet(@ForAll("setLimit") IntegerWrapperClass actual) {
		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specSetWithLimit() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.build();
	}

	@Property
	void giveMeSpecSetWithLimit(@ForAll("specSetWithLimit") IntegerWrapperClass actual) {
		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<StringListClass> specSetIndexWithLimit() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.build();
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns(@ForAll("specSetIndexWithLimit") StringListClass actual) {
		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Provide
	Arbitrary<StringListClass> setIndexWithLimit() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.build();
	}

	@Property
	void giveMeSetIndexWithLimitReturns(@ForAll("setIndexWithLimit") StringListClass actual) {
		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Provide
	Arbitrary<IntegerArrayClass> copyArbitrary() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		ArbitraryBuilder<IntegerArrayClass> builder = this.sut.giveMeBuilder(expected);
		ArbitraryBuilder<IntegerArrayClass> copiedBuilder = builder.copy()
			.set("value[1]", 3);

		return copiedBuilder.build();
	}

	@Property
	void copy(@ForAll("copyArbitrary") IntegerArrayClass actual) {
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isNotEqualTo(2);
		then(actual.value[1]).isEqualTo(3);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<IntegerArrayClass> copyArbitaryWithManipulator() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});
		ArbitraryBuilder<IntegerArrayClass> builder = this.sut.giveMeBuilder(expected)
			.set("value[0]", -1);

		return builder.copy().build();
	}

	@Property
	void copyWithManipulator(@ForAll("copyArbitaryWithManipulator") IntegerArrayClass actual) {
		then(actual.value[0]).isEqualTo(-1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<StringListClass> filterLimitIndex() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.filter("values[*]", String.class, it -> it.length() > 0)
			.filter("values[*]", String.class, it -> it.length() > 5, 1)
			.build();
	}

	@Property
	void giveMeFilterLimitIndex(@ForAll("filterLimitIndex") StringListClass actual) {
		then(actual.values).anyMatch(it -> it.length() > 5);
	}

	@Provide
	Arbitrary<StringListClass> filterLimitIndexNotOverwriteIfLimitIsZero() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.filter("values[*]", String.class, it -> it.length() > 5)
			.filter("values[*]", String.class, it -> it.length() == 0, 0)
			.build();
	}

	@Property
	void giveMefilterLimitIndexNotOverwriteIfLimitIsZeroReturnsNotFilter(
		@ForAll("filterLimitIndexNotOverwriteIfLimitIsZero") StringListClass actual
	) {
		then(actual.values).allMatch(it -> it.length() > 5);
	}

	@Provide
	Arbitrary<IntegerListClass> specListSetSize() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.build();
	}

	@Property
	void giveMeSpecListSetSize(@ForAll("specListSetSize") IntegerListClass actual) {
		then(actual.values).hasSize(1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListSetElement() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListSetElement(@ForAll("specListSetElement") IntegerListClass actual) {
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListAnySet() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListAnySet(@ForAll("specListAnySet") IntegerListClass actual) {
		then(actual.values).anyMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListAllSet() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListAllSet(@ForAll("specListAllSet") IntegerListClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListFilterElement() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.<Integer>filterElement(0, filtered -> filtered > 1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListFilterElement(@ForAll("specListFilterElement") IntegerListClass actual) {
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isGreaterThan(1);
	}

	@Property
	void giveMeSpecListAnyFilter() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.<Integer>any(filtered -> filtered > 1);
			}))
			.sample();

		then(actual.values).anyMatch(it -> it > 1);
	}

	@Property
	void giveMeSpecListAllFilter() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.<Integer>all(filtered -> filtered > 1);
			}))
			.sample();

		then(actual.values).allMatch(it -> it > 1);
	}

	@Property
	void giveMeSpecListFilterElementField() {
		NestedStringList actual = this.sut.giveMeBuilder(NestedStringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.<String>filterElementField(0, "value", filtered -> filtered.length() > 5);
			}))
			.sample();

		then(actual.values).allMatch(it -> it.value.length() > 5);
	}

	@Property
	void giveMeSpecListListElementSet() {
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).hasSize(1);
		then(actual.values.get(0).get(0)).isEqualTo("set");
	}

	@Property
	void giveMeSpecListListElementFilter() {
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.<String>filterElement(0, filtered -> filtered.length() > 5);
				});
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).hasSize(1);
		then(actual.values.get(0).get(0).length()).isGreaterThan(5);
	}

	@Property
	void giveMeMap() {
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.build()
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeMapAndSet() {
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.build()
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void giveMeMapAndSetAndMap() {
		String actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.map(StringWrapperClass::getValue)
			.build()
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void giveMeList() {
		List<IntegerWrapperClass> actual = this.sut.giveMe(IntegerWrapperClass.class, 5);

		then(actual).hasSize(5);
		then(actual).allMatch(Objects::nonNull);
	}

	@Property
	void giveMeOptional() {
		OptionalClass actual = this.sut.giveMeOne(OptionalClass.class);

		then(actual).isNotNull();
	}

	@Provide
	Arbitrary<MapKeyIntegerValueIntegerClass> sizeMap() {
		return this.sut.giveMeBuilder(MapKeyIntegerValueIntegerClass.class)
			.size("values", 2, 2)
			.build();
	}

	@Property
	void giveMeSizeMap(@ForAll("sizeMap") MapKeyIntegerValueIntegerClass actual) {
		then(actual.values).hasSize(2);
	}

	@Provide
	Arbitrary<StringListClass> setRightOrder() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSize(3)
						.setElement(0, "field1")
						.setElement(1, "field2")
						.setElement(2, "field3")
				)
			)
			.build();
	}

	@Property
	void giveMeSetRightOrder(@ForAll("setRightOrder") StringListClass actual) {
		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(3);
		then(values.get(0)).isEqualTo("field1");
		then(values.get(1)).isEqualTo("field2");
		then(values.get(2)).isEqualTo("field3");
	}

	@Provide
	Arbitrary<StringListClass> filterRightOrder() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					(it) -> it.ofSize(2)
						.filterElement(0, (Predicate<String>)s -> s.length() > 5)
						.filterElement(1, (Predicate<String>)s -> s.length() > 10)
				))
			.build();
	}

	@Property
	void giveMeFilterRightOrder(@ForAll("filterRightOrder") StringListClass actual) {
		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(2);
		then(values.get(0).length()).isGreaterThan(5);
		then(values.get(1).length()).isGreaterThan(10);
	}

	@Provide
	Arbitrary<StringListClass> listSpecMinSize() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			)
			.build();
	}

	@Property
	void giveMeListSpecMinSize(@ForAll("listSpecMinSize") StringListClass actual) {
		then(actual.values.size()).isGreaterThanOrEqualTo(1);
	}

	@Provide
	Arbitrary<StringListClass> listSpecMaxSize() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.build();
	}

	@Property
	void giveMeListSpecMaxSize(@ForAll("listSpecMaxSize") StringListClass actual) {
		then(actual.values.size()).isLessThanOrEqualTo(2);
	}

	@Provide
	Arbitrary<StringListClass> listSpecSizeBetween() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.build();
	}

	@Property
	void giveMeListSpecSizeBetween(@ForAll("listSpecSizeBetween") StringListClass actual) {
		then(actual.values.size()).isBetween(1, 3);
	}

	@Provide
	Arbitrary<TwoStringClass> setAllName() {
		return this.sut.giveMeBuilder(TwoStringClass.class)
			.set("*", "set")
			.build();
	}

	@Property
	void giveMeSetAllName(@ForAll("setAllName") TwoStringClass actual) {
		then(actual.value1).isEqualTo("set");
		then(actual.value2).isEqualTo("set");
	}

	@Provide
	Arbitrary<StringListClass> listExactSize() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 3)
			.build();
	}

	@Property
	void giveMeListExactSize(@ForAll("listExactSize") StringListClass actual) {
		then(actual.values.size()).isEqualTo(3);
	}

	@Provide
	Arbitrary<String> zipWith() {
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		return this.sut.giveMeBuilder(Integer.class)
			.zipWith(stringArbitraryBuilder, (integer, string) -> integer + "" + string)
			.build();
	}

	@Property
	void giveMeZipWith(@ForAll("zipWith") String actual) {
		then(actual).isNotNull();
	}

	@Provide
	Arbitrary<String> zipTwoElement() {
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);
		return ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).build();
	}

	@Property
	void giveMeZipTwoElement(@ForAll("zipTwoElement") String actual) {
		then(actual).isNotNull();
	}

	@Property
	void setAfterBuildNotAffected() {
		ArbitraryBuilder<StringWrapperClass> builder = this.sut.giveMeBuilder(StringWrapperClass.class);
		Arbitrary<StringWrapperClass> build = builder.build();
		ArbitraryBuilder<StringWrapperClass> actual = builder.set("value", "set");

		StringWrapperClass actualSample = actual.sample();
		StringWrapperClass buildSample = build.sample();
		then(actualSample).isNotEqualTo(buildSample);
		then(actualSample.value).isEqualTo("set");
	}

	@Property
	void giveMeZipReturnsNew() {
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);

		Arbitrary<String> zippedArbitraryBuilder = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).build();

		String result1 = zippedArbitraryBuilder.sample();
		String result2 = zippedArbitraryBuilder.sample();
		then(result1).isNotEqualTo(result2);
	}

	@Provide
	Arbitrary<StringWrapperClass> builderSetNull() {
		return this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", null)
			.build();
	}

	@Property
	void giveMeBuilderSetNull(@ForAll("builderSetNull") StringWrapperClass actual) {
		then(actual.value).isNull();
	}

	@Provide
	Arbitrary<IntegerListClass> minSize() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.minSize("values", 2)
			.build();
	}

	@Property
	void giveMeMinSize(@ForAll("minSize") IntegerListClass actual) {
		then(actual.values.size()).isGreaterThanOrEqualTo(2);
	}

	@Provide
	Arbitrary<IntegerListClass> maxSize() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.maxSize("values", 10)
			.build();
	}

	@Property
	void giveMeMaxSize(@ForAll("maxSize") IntegerListClass actual) {
		then(actual.values.size()).isLessThanOrEqualTo(10);
	}

	@Provide
	Arbitrary<IntegerListClass> sizeMinMaxBiggerThanDefault() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.size("values", 100, 150)
			.build();
	}

	@Property(tries = 10)
	void giveMeSizeMinMaxBiggerThanDefault(@ForAll("sizeMinMaxBiggerThanDefault") IntegerListClass actual) {
		then(actual.values.size()).isBetween(100, 150);
	}

	@Provide
	Arbitrary<IntegerListClass> sizeMinBiggerThanDefaultMax() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.minSize("values", 100)
			.build();
	}

	@Property(tries = 10)
	void giveMeSizeMinBiggerThanDefaultMax(@ForAll("sizeMinBiggerThanDefaultMax") IntegerListClass actual) {
		then(actual.values.size()).isBetween(100, 110);
	}

	@Provide
	Arbitrary<IntegerListClass> sizeMaxSizeIsZero() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.maxSize("values", 0)
			.build();
	}

	@Property
	void giveMeSizeMaxSizeIsZero(@ForAll("sizeMaxSizeIsZero") IntegerListClass actual) {
		then(actual.values).isEmpty();
	}

	@Provide
	Arbitrary<IntegerListClass> sizeMaxSizeBeforeMinSizeIsZero() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.maxSize("values", 15)
			.minSize("values", 14)
			.build();
	}

	@Property
	void giveMeSizeMaxSizeBeforeMinSizeIsZero(@ForAll("sizeMaxSizeBeforeMinSizeIsZero") IntegerListClass actual) {
		then(actual.values.size()).isBetween(14, 15);
	}

	@Provide
	Arbitrary<IntegerListClass> sizeMinSizeBeforeMaxSizeIsZero() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.minSize("values", 14)
			.maxSize("values", 15)
			.build();
	}

	@Property
	void giveMeSizeMinSizeBeforeMaxSizeIsZero(@ForAll("sizeMinSizeBeforeMaxSizeIsZero") IntegerListClass actual) {
		then(actual.values.size()).isBetween(14, 15);
	}

	@Provide
	Arbitrary<StringWrapperClassWithPredicate> acceptIf() {
		return this.sut.giveMeBuilder(StringWrapperClassWithPredicate.class)
			.acceptIf(StringWrapperClassWithPredicate::isEmpty, it -> it.set("value", "test"))
			.build();
	}

	@Property
	void giveMeAcceptIf(
		@ForAll("acceptIf") StringWrapperClassWithPredicate actual1,
		@ForAll("acceptIf") StringWrapperClassWithPredicate actual2
	) {
		then(actual1.value).satisfiesAnyOf(
			it -> then(it).isNotEqualTo(actual2),
			it -> then(it).isEqualTo("test")
		);
	}

	@Provide
	Arbitrary<IntegerListClass> specAny() {
		ExpressionSpec specOne = new ExpressionSpec()
			.list("values", it -> it
				.ofSize(1)
				.setElement(0, 1)
			);
		ExpressionSpec specTwo = new ExpressionSpec()
			.list("values", it -> it
				.ofSize(2)
				.setElement(0, 1)
				.setElement(1, 2)
			);
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.specAny(specOne, specTwo)
			.build();
	}

	@Property
	void giveMeSpecAny(@ForAll("specAny") IntegerListClass actual) {
		IntegerListClass expectedOne = new IntegerListClass();
		expectedOne.values = new ArrayList<>();
		expectedOne.values.add(1);

		IntegerListClass expectedTwo = new IntegerListClass();
		expectedTwo.values = new ArrayList<>();
		expectedTwo.values.add(1);
		expectedTwo.values.add(2);

		then(actual).isIn(expectedOne, expectedTwo);
	}

	@Provide
	Arbitrary<IntegerListClassNotEmpty> notEmpty() {
		return this.sut.giveMeBuilder(IntegerListClassNotEmpty.class)
			.build();
	}

	@Property
	void giveMeNotEmpty(@ForAll("notEmpty") IntegerListClassNotEmpty actual) {
		then(actual.values).isNotEmpty();
	}

	@Property
	void addExceptGenerate() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("com.navercorp.fixturemonkey.test")
			.build();

		ExceptGenerateClass actual = sut.giveMeOne(ExceptGenerateClass.class);

		then(actual).isNull();
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	@Data
	public static class IntegerWrapperClassWithAnnotation {
		@Positive
		int value;
	}

	@Data
	public static class IntegerListClass {
		List<Integer> values;
	}

	@Data
	public static class CustomizerIntegerClass {
		Integer value;
	}

	@Builder
	public static class BuilderIntegerClass {
		int value;
	}

	public static class FieldReflectionIntegerClass {
		private int value;
	}

	public static class NullIntegerClass {
		int value;
	}

	@Data
	public static class BeanIntegerClass {
		private int value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringWrapperClass {
		private String value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerArrayClass {
		private Integer[] value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntArrayClass {
		private int[] value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueIntegerClass {
		private Map<Integer, Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueStringClass {
		private Map<Integer, String> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapEntryKeyIntegerValueStringClass {
		private Map.Entry<Integer, String> value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerSetClass {
		private Set<Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerIterableClass {
		private Iterable<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerIteratorClass {
		private Iterator<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerStreamClass {
		private Stream<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerOptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Data
	public static class StringListClass {
		private List<String> values;
	}

	@Data
	public static class NestedStringList {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class ListListString {
		private List<List<String>> values;
	}

	@Data
	public static class OptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Data
	public static class TwoStringClass {
		private String value1;
		private String value2;
	}

	@Data
	public static class StringWrapperClassWithPredicate {
		private String value;

		public boolean isEmpty() {
			return value == null;
		}
	}

	@Data
	public static class IntegerListClassNotEmpty {
		@NotEmpty
		private List<Integer> values;
	}

	@Data
	public static class ExceptGenerateClass {
		String value;
	}
}
