package com.navercorp.fixturemonkey.arbitrary;

@FunctionalInterface
public interface InterfaceSupplier<T> {
	T get(Class<T> type);
}
