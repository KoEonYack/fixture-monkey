package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;

public final class ContainerSizeConstraint {
	@Nullable
	private final Integer minSize;

	@Nullable
	private final Integer maxSize;

	public ContainerSizeConstraint(@Nullable Integer minSize, @Nullable Integer maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	public int getMinSize() {
		return minSize == null ? DEFAULT_ELEMENT_MIN_SIZE : minSize;
	}

	public int getMaxSize() {
		return maxSize == null ? DEFAULT_ELEMENT_MAX_SIZE : maxSize;
	}

	public ContainerSizeConstraint copy() {
		return new ContainerSizeConstraint(this.minSize, this.maxSize);
	}

	public int getArbitraryElementSize() {
		return Arbitraries.integers().between(getMinSize(), getMaxSize()).sample();
	}
}
