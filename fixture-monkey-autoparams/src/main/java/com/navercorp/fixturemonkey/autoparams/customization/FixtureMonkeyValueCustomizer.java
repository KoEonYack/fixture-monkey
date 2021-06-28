package com.navercorp.fixturemonkey.autoparams.customization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

import org.javaunit.autoparams.Builder;
import org.javaunit.autoparams.customization.Customizer;
import org.javaunit.autoparams.generator.ObjectContainer;
import org.javaunit.autoparams.generator.ObjectGenerator;

import com.navercorp.fixturemonkey.FixtureMonkey;

final class FixtureMonkeyValueCustomizer implements Customizer {
	private final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyValueCustomizer(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public ObjectGenerator customize(ObjectGenerator generator) {
		return (query, context) ->
			generate(query.getType()).yieldIfEmpty(() -> generator.generate(query, context));
	}

	private ObjectContainer generate(Type type) {
		if (type instanceof Class<?>) {
			return this.generateNonGeneric((Class<?>) type);
		} else if (type instanceof ParameterizedType) {
			return this.generateGeneric((ParameterizedType)type);
		} else {
			return ObjectContainer.EMPTY;
		}
	}

	private ObjectContainer generateNonGeneric(Class<?> type) {
		return new ObjectContainer(this.fixtureMonkey.giveMeOne(type));
	}

	private ObjectContainer generateGeneric(ParameterizedType parameterizedType) {
		Class<?> type = (Class<?>) parameterizedType.getRawType();
		if (this.isYieldType(type)) {
			return ObjectContainer.EMPTY;
		}

		return new ObjectContainer(this.fixtureMonkey.giveMeOne(type));
	}

	private boolean isYieldType(Class<?> type) {
		if (Builder.class.isAssignableFrom(type)) {
			return true;
		}

		if (Iterable.class.isAssignableFrom(type)) {
			return true;
		}

		if (Stream.class.isAssignableFrom(type)) {
			return true;
		}

		if (Map.class.isAssignableFrom(type)) {
			return true;
		}

		return false;
	}
}
