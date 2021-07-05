package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

public interface PreArbitraryManipulator<T> extends ArbitraryExpressionManipulator, BuilderManipulator {
	Arbitrary<T> apply(Arbitrary<T> from);
}
