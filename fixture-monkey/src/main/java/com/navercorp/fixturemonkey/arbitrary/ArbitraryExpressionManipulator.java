package com.navercorp.fixturemonkey.arbitrary;

interface ArbitraryExpressionManipulator {
	ArbitraryExpression getArbitraryExpression();

	void addPrefix(String expression);
}
