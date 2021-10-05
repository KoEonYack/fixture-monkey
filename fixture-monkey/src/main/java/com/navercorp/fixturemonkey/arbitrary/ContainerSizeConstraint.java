/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;

import java.util.Optional;

import javax.annotation.Nullable;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.JqwikException;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.SourceOfRandomness;
import net.jqwik.engine.execution.lifecycle.CurrentTestDescriptor;

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
		return CurrentTestDescriptor.isEmpty()
			? CurrentTestDescriptor.runWithDescriptor(
			SAMPLE_DESCRIPTOR,
			() -> Arbitraries.integers().between(getMinSize(), getMaxSize())
				.generator(1000, false)
				.stream(SourceOfRandomness.current())
				.map(Shrinkable::value)
				.map(Optional::ofNullable)
				.findFirst()
				.orElseThrow(() -> new JqwikException("Cannot generate a value"))
				.orElse(null)
		)
			: Arbitraries.integers().between(getMinSize(), getMaxSize())
			.generator(1000, false)
			.stream(SourceOfRandomness.current())
			.map(Shrinkable::value)
			.map(Optional::ofNullable)
			.findFirst()
			.orElseThrow(() -> new JqwikException("Cannot generate a value"))
			.orElse(null);
	}

	private static final TestDescriptor SAMPLE_DESCRIPTOR = new AbstractTestDescriptor(
		UniqueId.root("fixture-monkey", "samples"),
		"sample descriptor for fixture-monkey"
	) {
		@Override
		public Type getType() {
			return Type.TEST;
		}
	};
}
