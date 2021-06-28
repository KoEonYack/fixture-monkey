package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ContainerMaxSizeManipulator implements MetadataManipulator {
	private ArbitraryExpression arbitraryExpression;
	private final int size;

	public ContainerMaxSizeManipulator(ArbitraryExpression arbitraryExpression, int size) {
		this.arbitraryExpression = arbitraryExpression;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	@Override
	public ArbitraryExpression getArbitraryExpression() {
		return arbitraryExpression;
	}

	@Override
	public void addPrefix(String expression) {
		arbitraryExpression = arbitraryExpression.appendLeft(expression);
	}

	@Override
	public void accept(ArbitraryBuilder<?> arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	@Override
	public Priority getPriority() {
		return Priority.HIGH;
	}

	@Override
	public ContainerMaxSizeManipulator copy() {
		return new ContainerMaxSizeManipulator(this.arbitraryExpression, this.size);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ContainerMaxSizeManipulator that = (ContainerMaxSizeManipulator)obj;
		return size == that.size
			&& Objects.equals(arbitraryExpression, that.arbitraryExpression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(arbitraryExpression, size);
	}
}
