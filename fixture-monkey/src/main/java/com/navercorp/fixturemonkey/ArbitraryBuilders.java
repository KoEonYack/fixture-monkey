package com.navercorp.fixturemonkey;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

public final class ArbitraryBuilders {
	public static <T, U, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		BiFunction<T, U, R> combinator
	) {
		return a1.zipWith(a2, combinator);
	}

	public static <T, U, V, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		ArbitraryBuilder<V> a3,
		F3<T, U, V, R> combinator
	) {
		return a1.zipWith(a2, a3, combinator);
	}

	public static <T, U, V, W, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		ArbitraryBuilder<V> a3,
		ArbitraryBuilder<W> a4,
		F4<T, U, V, W, R> combinator
	) {
		return a1.zipWith(a2, a3, a4, combinator);
	}

	public static <T> ArbitraryBuilder<T> zip(
		List<ArbitraryBuilder<?>> list,
		Function<List<?>, T> combinator
	) {
		if (list.size() < 2) {
			throw new IllegalArgumentException(
				"zip should be used in more than two ArbitraryBuilders, given size : " + list.size()
			);
		}
		ArbitraryBuilder<?> first = list.get(0);
		return first.zipWith(list.subList(1, list.size()), combinator);
	}
}
