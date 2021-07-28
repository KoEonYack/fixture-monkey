package com.navercorp.fixturemonkey.arbitrary;

import java.util.function.Supplier;

public class LazyValue<T> {
	private T value;
	private final Supplier<T> supplier;

	public LazyValue(T value) {
		this.value = value;
		this.supplier = () -> value;
	}

	public LazyValue(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@SuppressWarnings("unchecked")
	public ArbitraryType<T> getArbitraryType() {
		if (get() == null) {
			return NullArbitraryType.INSTANCE;
		}
		return new ArbitraryType<>((Class<T>)get().getClass());
	}

	public T get() {
		if (value == null) {
			value = supplier.get();
		}
		return value;
	}

	public void clear() {
		this.value = null;
	}
}
