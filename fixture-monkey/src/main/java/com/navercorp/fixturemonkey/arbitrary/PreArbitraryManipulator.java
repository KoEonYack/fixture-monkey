package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

public interface PreArbitraryManipulator<T> extends BuilderManipulator {
	Arbitrary<T> apply(Arbitrary<T> from);
}
