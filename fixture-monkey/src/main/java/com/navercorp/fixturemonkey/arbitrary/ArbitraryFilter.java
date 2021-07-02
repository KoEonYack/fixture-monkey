package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;
import java.util.function.Predicate;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.TypeSupports;

public final class ArbitraryFilter<T> extends AbstractArbitraryExpressionManipulator
	implements PostArbitraryManipulator<T> {
	private final Class<T> clazz;
	private final Predicate<T> filter;
	private long limit;

	public ArbitraryFilter(Class<T> clazz, ArbitraryExpression arbitraryExpression, Predicate<T> filter, long limit) {
		super(arbitraryExpression);
		this.clazz = clazz;
		this.filter = filter;
		this.limit = limit;
	}

	public ArbitraryFilter(Class<T> clazz, ArbitraryExpression arbitraryExpression, Predicate<T> filter) {
		this(clazz, arbitraryExpression, filter, Long.MAX_VALUE);
	}

	public Class<T> getClazz() {
		return clazz;
	}

	@Override
	public Arbitrary<T> apply(Arbitrary<T> from) {
		if (this.limit > 0) {
			limit--;
			return from.filter(filter);
		} else {
			return from;
		}
	}

	@Override
	public boolean isMappableTo(ArbitraryNode<T> arbitraryNode) {
		Class<?> nodeClazz = arbitraryNode.getType().getType();
		return TypeSupports.isSameType(this.clazz, nodeClazz)
			|| this.clazz.isAssignableFrom(nodeClazz);
	}

	@Override
	public ArbitraryFilter<T> copy() {
		return new ArbitraryFilter<>(this.clazz, this.getArbitraryExpression(), this.filter, this.limit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryFilter<T> that = (ArbitraryFilter<T>)obj;
		return clazz.equals(that.clazz)
			&& getArbitraryExpression().equals(that.getArbitraryExpression())
			&& filter.equals(that.filter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clazz, getArbitraryExpression(), filter);
	}
}
