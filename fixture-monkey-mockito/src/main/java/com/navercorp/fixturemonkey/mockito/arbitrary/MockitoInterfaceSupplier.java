package com.navercorp.fixturemonkey.mockito.arbitrary;

import org.mockito.Mockito;

import com.navercorp.fixturemonkey.arbitrary.InterfaceSupplier;

public final class MockitoInterfaceSupplier<T> implements InterfaceSupplier<T> {
	public static final MockitoInterfaceSupplier<?> INSTANCE = new MockitoInterfaceSupplier<>();

	private MockitoInterfaceSupplier() {
	}

	@Override
	public T get(Class<T> type) {
		return Mockito.mock(type);
	}
}
