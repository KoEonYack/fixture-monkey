package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryTraverser;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public class FixtureMonkey {
	private final ArbitraryOption options;
	private final ArbitraryGenerator defaultGenerator;
	@SuppressWarnings("rawtypes")
	private final ArbitraryValidator validator;
	private final Map<Class<?>, ArbitraryGenerator> generatorMap;
	private final ArbitraryCustomizers arbitraryCustomizers;

	@SuppressWarnings("rawtypes")
	public FixtureMonkey(
		ArbitraryOption options,
		ArbitraryGenerator defaultGenerator,
		ArbitraryValidator validator,
		Map<Class<?>, ArbitraryGenerator> generatorMap,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this.options = options;
		this.defaultGenerator = defaultGenerator;
		this.validator = validator;
		this.generatorMap = generatorMap;
		this.arbitraryCustomizers = arbitraryCustomizers;
		options.applyArbitraryBuilders(this);
	}

	public static FixtureMonkeyBuilder builder() {
		return new FixtureMonkeyBuilder();
	}

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMeBuilder(type, options).build().sampleStream();
	}

	public <T> Stream<T> giveMe(Class<T> type, ArbitraryCustomizer<T> customizer) {
		return this.giveMeBuilder(type, options, customizer).build().sampleStream();
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type).limit(size).collect(toList());
	}

	public <T> List<T> giveMe(Class<T> type, int size, ArbitraryCustomizer<T> customizer) {
		return this.giveMe(type, customizer).limit(size).collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMe(type, 1).get(0);
	}

	public <T> T giveMeOne(Class<T> type, ArbitraryCustomizer<T> customizer) {
		return this.giveMe(type, 1, customizer).get(0);
	}

	public <T> Arbitrary<T> giveMeArbitrary(Class<T> type) {
		return this.giveMeBuilder(type, options).build();
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> clazz) {
		return this.giveMeBuilder(clazz, options);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> clazz, ArbitraryOption options) {
		return this.giveMeBuilder(clazz, options, this.arbitraryCustomizers);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
		return new ArbitraryBuilder<>(
			value,
			new ArbitraryTraverser(options),
			defaultGenerator,
			validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	private <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		ArbitraryOption options,
		ArbitraryCustomizer<T> customizer
	) {
		ArbitraryCustomizers newArbitraryCustomizers =
			this.arbitraryCustomizers.mergeWith(Collections.singletonMap(clazz, customizer));

		return this.giveMeBuilder(clazz, options, newArbitraryCustomizers);
	}

	private <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		ArbitraryOption option,
		ArbitraryCustomizers customizers
	) {
		ArbitraryBuilder<T> defaultArbitraryBuilder = option.getDefaultArbitraryBuilder(clazz);
		if (defaultArbitraryBuilder != null) {
			return defaultArbitraryBuilder.copy();
		}

		return new ArbitraryBuilder<>(
			clazz,
			option,
			defaultGenerator,
			this.validator,
			customizers,
			this.generatorMap
		);
	}
}
