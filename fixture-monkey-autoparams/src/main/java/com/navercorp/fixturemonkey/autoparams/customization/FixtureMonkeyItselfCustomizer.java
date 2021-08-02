package com.navercorp.fixturemonkey.autoparams.customization;

import java.lang.reflect.Type;

import org.javaunit.autoparams.customization.Customizer;
import org.javaunit.autoparams.generator.ObjectContainer;
import org.javaunit.autoparams.generator.ObjectGenerator;

import com.navercorp.fixturemonkey.FixtureMonkey;

final class FixtureMonkeyItselfCustomizer implements Customizer {
	private final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyItselfCustomizer(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public ObjectGenerator customize(ObjectGenerator generator) {
		return (query, context) ->
			generate(query.getType()).yieldIfEmpty(() -> generator.generate(query, context));
	}

	private ObjectContainer generate(Type type) {
		return type.equals(FixtureMonkey.class)
			? new ObjectContainer(this.fixtureMonkey)
			: ObjectContainer.EMPTY;
	}
}
