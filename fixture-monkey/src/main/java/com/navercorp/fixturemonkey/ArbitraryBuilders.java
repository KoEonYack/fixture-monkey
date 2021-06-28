package com.navercorp.fixturemonkey;

import java.util.function.BiFunction;

public final class ArbitraryBuilders {
	public static <T, U, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		BiFunction<T, U, R> combinator
	) {
		return a1.zipWith(a2, combinator);
	}
}
