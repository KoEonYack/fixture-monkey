package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ContainerSizeManipulator implements MetadataManipulator {
	private ArbitraryExpression arbitraryExpression;
	private final Integer min;
	private final Integer max;

	public ContainerSizeManipulator(
		ArbitraryExpression arbitraryExpression,
		@Nullable Integer min,
		@Nullable Integer max
	) {
		this.arbitraryExpression = arbitraryExpression;
		this.min = min;
		this.max = max;
	}

	@Nullable
	public Integer getMin() {
		return min;
	}

	@Nullable
	public Integer getMax() {
		return max;
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
	public ContainerSizeManipulator copy() {
		return new ContainerSizeManipulator(this.arbitraryExpression, this.min, this.max);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ContainerSizeManipulator that = (ContainerSizeManipulator)obj;
		return arbitraryExpression.equals(that.arbitraryExpression)
			&& Objects.equals(min, that.min)
			&& Objects.equals(max, that.max);
	}

	@Override
	public int hashCode() {
		return Objects.hash(arbitraryExpression, min, max);
	}
}
