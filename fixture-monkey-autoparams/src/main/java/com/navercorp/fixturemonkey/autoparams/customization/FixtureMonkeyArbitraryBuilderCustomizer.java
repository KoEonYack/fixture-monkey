package com.navercorp.fixturemonkey.autoparams.customization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.javaunit.autoparams.customization.Customizer;
import org.javaunit.autoparams.generator.ObjectContainer;
import org.javaunit.autoparams.generator.ObjectGenerator;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;

final class FixtureMonkeyArbitraryBuilderCustomizer implements Customizer {
	private final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyArbitraryBuilderCustomizer(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public ObjectGenerator customize(ObjectGenerator generator) {
		return (query, context) ->
			generate(query.getType()).yieldIfEmpty(() -> generator.generate(query, context));
	}

	private ObjectContainer generate(Type type) {
		return type instanceof ParameterizedType
			? generate((ParameterizedType)type)
			: ObjectContainer.EMPTY;
	}

	private ObjectContainer generate(ParameterizedType parameterizedType) {
		Class<?> type = (Class<?>)parameterizedType.getRawType();
		return type == ArbitraryBuilder.class
			? this.generate((Class<?>)parameterizedType.getActualTypeArguments()[0])
			: ObjectContainer.EMPTY;
	}

	private ObjectContainer generate(Class<?> type) {
		return new ObjectContainer(this.fixtureMonkey.giveMeBuilder(type));
	}
}
