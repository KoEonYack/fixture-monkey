package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public abstract class AbstractArbitrarySet<T> extends AbstractArbitraryExpressionManipulator
	implements PreArbitraryManipulator<T> {

	public AbstractArbitrarySet(ArbitraryExpression arbitraryExpression) {
		super(arbitraryExpression);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public final void accept(ArbitraryBuilder arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	public abstract Object getValue();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		AbstractArbitrarySet<?> that = (AbstractArbitrarySet<?>)obj;
		return getArbitraryExpression().equals(that.getArbitraryExpression());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getArbitraryExpression());
	}
}
