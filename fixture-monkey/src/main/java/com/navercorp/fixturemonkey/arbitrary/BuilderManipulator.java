package com.navercorp.fixturemonkey.arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public interface BuilderManipulator {
	@SuppressWarnings("rawtypes")
	void accept(ArbitraryBuilder arbitraryBuilder);

	BuilderManipulator copy();
}
