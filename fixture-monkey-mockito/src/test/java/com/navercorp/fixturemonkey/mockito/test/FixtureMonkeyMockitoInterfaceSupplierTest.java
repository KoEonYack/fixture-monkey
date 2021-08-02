package com.navercorp.fixturemonkey.mockito.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.annotation.Nonnull;

import net.jqwik.api.Example;

import lombok.Getter;
import lombok.Setter;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.mockito.arbitrary.MockitoInterfaceSupplier;

class FixtureMonkeyMockitoInterfaceSupplierTest {
	private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.defaultInterfaceSupplier(MockitoInterfaceSupplier.INSTANCE)
		.build();

	@Example
	void interfaceTypeIsMock() {
		HasInterfaceType value = fixtureMonkey.giveMeOne(HasInterfaceType.class);
		assertThat(value.getInterfaceType()).isNotNull();

		when(value.getInterfaceType().getName()).thenReturn("test-value");
		assertThat(value.getInterfaceType().getName()).isEqualTo("test-value");
	}

	@Example
	void abstractTypeIsMock() {
		HasAbstractType value = fixtureMonkey.giveMeOne(HasAbstractType.class);
		assertThat(value.getAbstractType()).isNotNull();

		when(value.getAbstractType().getName()).thenReturn("test-value");
		assertThat(value.getAbstractType().getName()).isEqualTo("test-value");
	}

	@Getter
	@Setter
	public static class HasInterfaceType {
		private String name;

		@Nonnull
		private InterfaceType interfaceType;
	}

	public interface InterfaceType {
		String getName();
	}

	@Getter
	@Setter
	public static class HasAbstractType {
		private String name;

		@Nonnull
		private AbstractType abstractType;
	}

	public abstract static class AbstractType {
		public abstract String getName();
	}
}
