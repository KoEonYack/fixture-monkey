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
		if (maxSize == null
			&& minSize != null && minSize > DEFAULT_ELEMENT_MAX_SIZE
		) {
			return minSize + DEFAULT_ELEMENT_MAX_SIZE;
		}
		return maxSize == null ? DEFAULT_ELEMENT_MAX_SIZE : maxSize;
	}

	public ContainerSizeConstraint copy() {
		return new ContainerSizeConstraint(this.minSize, this.maxSize);
	}

	public int getArbitraryElementSize() {
		return Arbitraries.integers().between(getMinSize(), getMaxSize()).sample();
	}

	public ContainerSizeConstraint withMinSize(@Nullable Integer minSize) {
		if (minSize == null && this.minSize != null) {
			return new ContainerSizeConstraint(this.minSize, this.maxSize);
		}
		return new ContainerSizeConstraint(minSize, this.maxSize);
	}

	public ContainerSizeConstraint withMaxSize(@Nullable Integer maxSize) {
		if (maxSize == null && this.maxSize != null) {
			return new ContainerSizeConstraint(this.minSize, this.maxSize);
		}
		return new ContainerSizeConstraint(this.minSize, maxSize);

	}
}
