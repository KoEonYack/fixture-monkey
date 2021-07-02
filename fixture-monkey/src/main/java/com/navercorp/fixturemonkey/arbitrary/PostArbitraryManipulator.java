package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public interface PostArbitraryManipulator<T> extends BuilderManipulator, ArbitraryExpressionManipulator {
	Arbitrary<T> apply(Arbitrary<T> from);

	boolean isMappableTo(ArbitraryNode<T> node);

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	default void accept(ArbitraryBuilder arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}
}
