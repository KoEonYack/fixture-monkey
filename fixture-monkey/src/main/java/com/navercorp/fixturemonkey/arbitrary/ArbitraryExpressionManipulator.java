package com.navercorp.fixturemonkey.arbitrary;

public interface ArbitraryExpressionManipulator {
	ArbitraryExpression getArbitraryExpression();

	void addPrefix(String expression);
}
