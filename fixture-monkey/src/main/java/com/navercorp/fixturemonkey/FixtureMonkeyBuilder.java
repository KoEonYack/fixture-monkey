package com.navercorp.fixturemonkey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.navercorp.fixturemonkey.ArbitraryOption.FixtureOptionsBuilder;
import com.navercorp.fixturemonkey.arbitrary.NullableArbitraryEvaluator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.generator.AnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.validator.CompositeArbitraryValidator;

public class FixtureMonkeyBuilder {
	private ArbitraryGenerator defaultGenerator = new BeanArbitraryGenerator();
	private Map<Class<?>, ArbitraryGenerator> generatorMap = new HashMap<>();
	private Map<Class<?>, ArbitraryCustomizer<?>> customizerMap = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private ArbitraryValidator validator = new CompositeArbitraryValidator();
	private ArbitraryCustomizers arbitraryCustomizers = new ArbitraryCustomizers();
	private ArbitraryOption options;
	private final FixtureOptionsBuilder optionsBuilder = ArbitraryOption.builder();

	public FixtureMonkeyBuilder defaultGenerator(ArbitraryGenerator defaultCombiner) {
		this.defaultGenerator = defaultCombiner;
		return this;
	}

	public FixtureMonkeyBuilder generatorMap(Map<Class<?>, ArbitraryGenerator> combinerMap) {
		this.generatorMap = combinerMap;
		return this;
	}

	public FixtureMonkeyBuilder putGenerator(Class<?> type, ArbitraryGenerator generator) {
		this.generatorMap.put(type, generator);
		return this;
	}

	public FixtureMonkeyBuilder customizers(Map<Class<?>, ArbitraryCustomizer<?>> customizer) {
		this.customizerMap = customizer;
		return this;
	}

	public FixtureMonkeyBuilder customizers(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
		return this;
	}

	public <T> FixtureMonkeyBuilder addCustomizer(Class<T> type, ArbitraryCustomizer<T> customizer) {
		this.customizerMap.put(type, customizer);
		return this;
	}

	public FixtureMonkeyBuilder nullableArbitraryEvaluator(
		NullableArbitraryEvaluator nullableArbitraryEvaluator
	) {
		this.optionsBuilder.nullableArbitraryEvaluator(nullableArbitraryEvaluator);
		return this;
	}

	public FixtureMonkeyBuilder nullInject(double nullInject) {
		this.optionsBuilder.nullInject(nullInject);
		return this;
	}

	public FixtureMonkeyBuilder addExceptGeneratePackage(String exceptGeneratePackage) {
		this.optionsBuilder.addExceptGeneratePackage(exceptGeneratePackage);
		return this;
	}

	public FixtureMonkeyBuilder exceptGeneratePackages(Set<String> exceptGeneratePackages) {
		this.optionsBuilder.exceptGeneratePackages(exceptGeneratePackages);
		return this;
	}

	public FixtureMonkeyBuilder nullableContainer(boolean nullableContainer) {
		this.optionsBuilder.nullableContainer(nullableContainer);
		return this;
	}

	public FixtureMonkeyBuilder options(ArbitraryOption options) {
		this.options = options;
		return this;
	}

	public FixtureMonkeyBuilder addAnnotatedArbitraryGenerator(
		Class<?> clazz,
		AnnotatedArbitraryGenerator<?> generator
	) {
		this.optionsBuilder.addAnnotatedArbitraryGenerator(clazz, generator);
		return this;
	}

	@SuppressWarnings("rawtypes")
	public FixtureMonkeyBuilder validator(ArbitraryValidator validator) {
		this.validator = validator;
		return this;
	}

	public FixtureMonkey build() {
		if (options == null) {
			this.options = optionsBuilder.build();
		}

		return new FixtureMonkey(
			this.options,
			defaultGenerator,
			validator,
			generatorMap,
			this.arbitraryCustomizers.mergeWith(this.customizerMap)
		);
	}
}
