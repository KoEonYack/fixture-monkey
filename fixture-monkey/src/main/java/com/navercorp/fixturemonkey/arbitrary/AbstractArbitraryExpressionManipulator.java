package com.navercorp.fixturemonkey.arbitrary;

public abstract class AbstractArbitraryExpressionManipulator implements ArbitraryExpressionManipulator {
	private ArbitraryExpression arbitraryExpression;

	public AbstractArbitraryExpressionManipulator(ArbitraryExpression arbitraryExpression) {
		this.arbitraryExpression = arbitraryExpression;
	}

	@Override
	public final ArbitraryExpression getArbitraryExpression() {
		return arbitraryExpression;
	}

	@Override
	public final void addPrefix(String expression) {
		arbitraryExpression = arbitraryExpression.appendLeft(expression);
	}
}
