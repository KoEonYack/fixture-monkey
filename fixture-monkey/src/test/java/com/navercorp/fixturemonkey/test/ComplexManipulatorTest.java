package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryBuilders;
import com.navercorp.fixturemonkey.FixtureMonkey;

public class ComplexManipulatorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.build();

	@Property
	void giveMeAcceptIf() {
		StringWrapperClassWithPredicate actual1 = this.sut.giveMeBuilder(StringWrapperClassWithPredicate.class)
			.acceptIf(StringWrapperClassWithPredicate::isEmpty, it -> it.set("value", "test"))
			.sample();

		StringWrapperClassWithPredicate actual2 = this.sut.giveMeBuilder(StringWrapperClassWithPredicate.class)
			.acceptIf(StringWrapperClassWithPredicate::isEmpty, it -> it.set("value", "test"))
			.sample();

		then(actual1.value).satisfiesAnyOf(
			it -> then(it).isNotEqualTo(actual2),
			it -> then(it).isEqualTo("test")
		);
	}

	@Property
	void giveMeAcceptIfWithNull() {
		// when
		StringIntegerListClass actual = this.sut.giveMeBuilder(StringIntegerListClass.class)
			.set("value", "test")
			.acceptIf(it -> it.value.equals("test"), builder -> builder.setNull("values"))
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void decomposedNullCollectionReturnsNull() {
		// when
		List<Integer> values = this.sut.giveMeBuilder(IntegerListClass.class)
			.setNull("values")
			.map(it -> it.values)
			.sample();

		then(values).isNull();
	}

	@Property
	void giveMeComplexApply() {
		// when
		StringIntegerClass actual = this.sut.giveMeBuilder(StringIntegerClass.class)
			.setNotNull("value2")
			.apply((it, builder) -> builder.set("value1.value", String.valueOf(it.value2.value)))
			.sample();

		then(actual.value1.value).isEqualTo(String.valueOf(actual.value2.value));
	}

	@Property
	void acceptIfSetNull() {
		// given
		ArbitraryBuilder<NestedStringWrapper> decomposedBuilder = this.sut.giveMeBuilder(NestedStringWrapper.class)
			.set("value.value", Arbitraries.strings())
			.acceptIf(
				s -> true,
				it -> it.setNull("value.value")
			);

		// when
		NestedStringWrapper actual = decomposedBuilder.sample();

		then(actual.value.value).isNull();
	}

	@Property
	void applySetNull() {
		// given
		ArbitraryBuilder<NestedStringWrapper> decomposedBuilder = this.sut.giveMeBuilder(NestedStringWrapper.class)
			.set("value.value", Arbitraries.strings())
			.apply((value, it) -> it.setNull("value.value"));

		// when
		NestedStringWrapper actual = decomposedBuilder.sample();

		then(actual.value.value).isNull();
	}

	@Property
	void applySetAfterSetNull() {
		// given
		ArbitraryBuilder<NestedStringWrapper> decomposedBuilder = this.sut.giveMeBuilder(NestedStringWrapper.class)
			.set("value.value", Arbitraries.strings())
			.apply((value, it) -> it.setNull("value.value"))
			.set("value.value", "test");

		// when
		NestedStringWrapper actual = decomposedBuilder.sample();

		then(actual.value.value).isEqualTo("test");
	}

	@Property
	void acceptIfSetAfterSetNull() {
		// given
		ArbitraryBuilder<NestedStringWrapper> decomposedBuilder = this.sut.giveMeBuilder(NestedStringWrapper.class)
			.set("value.value", Arbitraries.strings())
			.acceptIf(
				s -> true,
				it -> it.setNull("value.value")
			)
			.set("value.value", "test");

		// when
		NestedStringWrapper actual = decomposedBuilder.sample();

		then(actual.value.value).isEqualTo("test");
	}

	@Property
	void giveMeZipList() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();
		list.add(this.sut.giveMeBuilder(StringWrapperClass.class));
		list.add(this.sut.giveMeBuilder(IntegerWrapperClass.class));

		// when
		StringIntegerClass actual = ArbitraryBuilders.zip(
			list,
			(l) -> {
				StringIntegerClass result = new StringIntegerClass();
				result.setValue1((StringWrapperClass)l.get(0));
				result.setValue2((IntegerWrapperClass)l.get(1));
				return result;
			}
		).sample();

		then(actual.value1).isNotNull();
		then(actual.value2).isNotNull();
	}

	@Property
	void giveMeZipEmptyListThrows() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();

		thenThrownBy(
			() -> ArbitraryBuilders.zip(
				list,
				(l) -> new StringIntegerClass()
			).sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("zip should be used in more than two ArbitraryBuilders, given size");
	}

	@Property
	void zipThree() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");

		// when
		NestedStringList actual = ArbitraryBuilders.zip(s1, s2, s3, (a1, a2, a3) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(3);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
	}

	@Property
	void giveMeZipWithThree() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");

		// when
		NestedStringList actual = s1.zipWith(s2, s3, (a1, a2, a3) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(3);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
	}

	@Property
	void zipFour() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");
		ArbitraryBuilder<StringWrapperClass> s4 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s4");

		// when
		NestedStringList actual = ArbitraryBuilders.zip(s1, s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(4);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
		then(actual.values.get(3).value).isEqualTo("s4");
	}

	@Property
	void giveMeZipWithFour() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");
		ArbitraryBuilder<StringWrapperClass> s4 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s4");

		// when
		NestedStringList actual = s1.zipWith(s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(4);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
		then(actual.values.get(3).value).isEqualTo("s4");
	}

	@Property
	void giveMeZipWith() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);

		// when
		String actual = this.sut.giveMeBuilder(Integer.class)
			.zipWith(stringArbitraryBuilder, (integer, string) -> integer + "" + string)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeZipTwoElement() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);

		// when
		String actual = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeZipReturnsNew() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);

		// when
		Arbitrary<String> zippedArbitraryBuilder = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).build();

		// then
		String result1 = zippedArbitraryBuilder.sample();
		String result2 = zippedArbitraryBuilder.sample();
		then(result1).isNotEqualTo(result2);
	}

	@Property
	void giveMeMap() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeMapAndSet() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void giveMeMapAndSetAndMap() {
		// when
		String actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.map(StringWrapperClass::getValue)
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void applySetWithDefault() {
		// given
		ArbitraryBuilder<StringIntegerListClass> defaultBuilder = this.sut.giveMeBuilder(StringIntegerListClass.class)
			.set("value", Arbitraries.integers().map(String::valueOf))
			.minSize("values", 1);

		// when
		StringIntegerListClass actual = defaultBuilder.apply(
			(value, builder) -> builder.set("values[" + (value.values.size() - 1) + "]", Integer.parseInt(value.value))
		).sample();

		then(actual.values.get(actual.values.size() - 1)).isEqualTo(Integer.parseInt(actual.value));
	}

	@Data
	public static class IntegerListClass {
		List<Integer> values;
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

	@Data
	public static class NestedStringWrapper {
		StringWrapperClass value;
	}

	@Data
	public static class StringIntegerListClass {
		String value;
		List<Integer> values;
	}

	@Data
	public static class NestedStringList {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class StringWrapperClassWithPredicate {
		private String value;

		public boolean isEmpty() {
			return value == null;
		}
	}
}
