package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ArbitraryNullity extends AbstractArbitraryExpressionManipulator
	implements BuilderManipulator {
	private final boolean toNull;

	public ArbitraryNullity(ArbitraryExpression fixtureExpression, boolean toNull) {
		super(fixtureExpression);
		this.toNull = toNull;
	}

	public Boolean toNull() {
		return toNull;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void accept(ArbitraryBuilder fixtureBuilder) {
		fixtureBuilder.setNullity(this);
	}

	@Override
	public ArbitraryNullity copy() {
		return new ArbitraryNullity(this.getArbitraryExpression(), this.toNull);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryNullity that = (ArbitraryNullity)obj;
		return toNull == that.toNull && getArbitraryExpression().equals(that.getArbitraryExpression());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getArbitraryExpression(), toNull);
	}
}
