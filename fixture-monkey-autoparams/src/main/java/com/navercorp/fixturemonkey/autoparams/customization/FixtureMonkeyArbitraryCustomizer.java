package com.navercorp.fixturemonkey.autoparams.customization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.javaunit.autoparams.customization.Customizer;
import org.javaunit.autoparams.generator.ObjectContainer;
import org.javaunit.autoparams.generator.ObjectGenerator;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.FixtureMonkey;

final class FixtureMonkeyArbitraryCustomizer implements Customizer {
	private final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyArbitraryCustomizer(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public ObjectGenerator customize(ObjectGenerator generator) {
		return (query, context) ->
			generate(query.getType()).yieldIfEmpty(() -> generator.generate(query, context));
	}

	private ObjectContainer generate(Type type) {
		return type instanceof ParameterizedType ? generate((ParameterizedType)type)
			: ObjectContainer.EMPTY;
	}

	private ObjectContainer generate(ParameterizedType parameterizedType) {
		Class<?> type = (Class<?>)parameterizedType.getRawType();
		return type == Arbitrary.class
			? this.generate((Class<?>)parameterizedType.getActualTypeArguments()[0])
			: ObjectContainer.EMPTY;
	}

	private ObjectContainer generate(Class<?> type) {
		return new ObjectContainer(this.fixtureMonkey.giveMeArbitrary(type));
	}
}
