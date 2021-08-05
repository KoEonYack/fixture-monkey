package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import lombok.Builder;
import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.arbitrary.ContainerArbitraryNodeGenerator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;
import com.navercorp.fixturemonkey.generator.FieldReflectionArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.NullArbitraryGenerator;

public class ArbitraryGeneratorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.putGenerator(BuilderIntegerClass.class, BuilderArbitraryGenerator.INSTANCE)
		.putGenerator(FieldReflectionIntegerClass.class, FieldReflectionArbitraryGenerator.INSTANCE)
		.putGenerator(NullIntegerClass.class, NullArbitraryGenerator.INSTANCE)
		.putGenerator(BeanIntegerClass.class, BeanArbitraryGenerator.INSTANCE)
		.build();

	@Property
	void giveMeWhenPutBuilderArbitraryGenerator() {
		// when
		BuilderIntegerClass actual = this.sut.giveMeOne(BuilderIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BuilderIntegerClass actual = sut.giveMeBuilder(BuilderIntegerClass.class).sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsFieldReflectionArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.build();

		// when
		FieldReflectionIntegerClass actual = sut.giveMeBuilder(FieldReflectionIntegerClass.class).sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutFieldReflectionArbitraryGenerator() {
		// when
		FieldReflectionIntegerClass actual = this.sut.giveMeOne(FieldReflectionIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsNullArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(NullArbitraryGenerator.INSTANCE)
			.build();

		// when
		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenPutNullArbitraryGenerator() {
		// when
		NullIntegerClass actual = this.sut.giveMeOne(NullIntegerClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBeanArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		// when
		BeanIntegerClass actual = sut.giveMeOne(BeanIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutBeanArbitraryGenerator() {
		// when
		BeanIntegerClass actual = this.sut.giveMeOne(BeanIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BuilderIntegerClass actual = sut.giveMeBuilder(BuilderIntegerClass.class)
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
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void generatorMapBeanGeneratorWithBuilderGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.putGenerator(BuilderIntegerClass.class, BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BeanInnerBuilderClass actual = sut.giveMeOne(BeanInnerBuilderClass.class);

		then(actual).isNotNull();
	}

	@Property
	void generatorMapFieldReflectionGeneratorWithBuilderGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.putGenerator(BuilderIntegerClass.class, BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		FieldReflectionInnerBuilderClass actual = sut.giveMeOne(FieldReflectionInnerBuilderClass.class);

		then(actual).isNotNull();
	}

	@Property
	void generatorMapFieldReflectionGeneratorWithBuilderGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.putGenerator(BuilderIntegerClass.class, BuilderArbitraryGenerator.INSTANCE)
			.addCustomizer(BuilderIntegerClass.class, new ArbitraryCustomizer<BuilderIntegerClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-1));
				}

				@Override
				public BuilderIntegerClass customizeFixture(BuilderIntegerClass object) {
					return object;
				}
			})
			.build();

		// when
		FieldReflectionInnerBuilderClass actual = sut.giveMeBuilder(FieldReflectionInnerBuilderClass.class)
			.setNotNull("value")
			.sample();

		then(actual.value.value).isEqualTo(-1);
	}

	@Property
	void overwriteCustomize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addCustomizer(BuilderIntegerClass.class, new ArbitraryCustomizer<BuilderIntegerClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-1));
				}

				@Override
				public BuilderIntegerClass customizeFixture(BuilderIntegerClass object) {
					return object;
				}
			})
			.addCustomizer(BuilderIntegerClass.class, new ArbitraryCustomizer<BuilderIntegerClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-2));
				}

				@Override
				public BuilderIntegerClass customizeFixture(BuilderIntegerClass object) {
					return object;
				}
			})
			.build();

		// when
		BuilderIntegerClass actual = sut.giveMeBuilder(BuilderIntegerClass.class)
			.generator(BuilderArbitraryGenerator.INSTANCE)
			.sample();

		then(actual.value).isEqualTo(-2);
	}

	@Property
	void giveMeWithCustomAnnotatedArbitraryGenerator() {
		// given
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

		// when
		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual.value).isEqualTo(1);
	}

	@Property
	void customContainerArbitraryNode() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.putContainerArbitraryNodeGenerator(CustomTriple.class, CustomTripleArbitraryNodeGenerator.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		CustomTripleWrapper actual = sut.giveMeOne(CustomTripleWrapper.class);

		then(actual.value.value1).isNotNull();
		then(actual.value.value2).isNotNull();
		then(actual.value.value3).isNotNull();
	}

	@Data
	public static class CustomTripleWrapper {
		CustomTriple<String, Integer, Float> value;
	}

	@Data
	public static class CustomTriple<T, U, V> {
		T value1;
		U value2;
		V value3;
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
	public static class BeanInnerBuilderClass {
		BuilderIntegerClass value;
	}

	public static class FieldReflectionInnerBuilderClass {
		BuilderIntegerClass value;
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	public static class CustomTripleArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
		public static final CustomTripleArbitraryNodeGenerator INSTANCE = new CustomTripleArbitraryNodeGenerator();

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
			List<ArbitraryNode<?>> generatedArbitraryNodes = new ArrayList<>();
			ArbitraryType<T> type = nowNode.getType();
			ArbitraryType<?> firstChildType = type.getGenericFixtureType(0);
			ArbitraryType<?> secondChildType = type.getGenericFixtureType(1);
			ArbitraryType<?> thirdChildType = type.getGenericFixtureType(2);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(firstChildType)
					.fieldName("value1")
					.build()
			);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(secondChildType)
					.fieldName("value2")
					.build()
			);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(thirdChildType)
					.fieldName("value3")
					.build()
			);
			return generatedArbitraryNodes;
		}
	}
}
