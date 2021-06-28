package com.navercorp.fixturemonkey.autoparams.customization;

import org.javaunit.autoparams.customization.CompositeCustomizer;

import com.navercorp.fixturemonkey.FixtureMonkey;

public class FixtureMonkeyCustomizer extends CompositeCustomizer {
	public FixtureMonkeyCustomizer() {
		this(FixtureMonkey.builder().build());
	}

	public FixtureMonkeyCustomizer(FixtureMonkey fixtureMonkey) {
		super(
			new FixtureMonkeyValueCustomizer(fixtureMonkey),
			new FixtureMonkeyArbitraryCustomizer(fixtureMonkey),
			new FixtureMonkeyArbitraryBuilderCustomizer(fixtureMonkey)
		);
	}
}
