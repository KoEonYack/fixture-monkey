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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.JqwikException;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.SourceOfRandomness;
import net.jqwik.engine.execution.lifecycle.CurrentTestDescriptor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

public final class ArbitrarySpecAny implements BuilderManipulator {
	private final List<ExpressionSpec> specs;

	public ArbitrarySpecAny(List<ExpressionSpec> specs) {
		this.specs = specs;
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void accept(ArbitraryBuilder arbitraryBuilder) {
		ExpressionSpec spec = CurrentTestDescriptor.isEmpty()
			? CurrentTestDescriptor.runWithDescriptor(SAMPLE_DESCRIPTOR, () ->
			Arbitraries.of(specs)
				.generator(1000, false)
				.stream(SourceOfRandomness.current())
				.map(Shrinkable::value)
				.map(Optional::ofNullable)
				.findFirst()
				.orElseThrow(() -> new JqwikException("Cannot generate a value"))
				.orElse(null)
		)
			: Arbitraries.of(specs)
			.generator(1000, false)
			.stream(SourceOfRandomness.current())
			.map(Shrinkable::value)
			.map(Optional::ofNullable)
			.findFirst()
			.orElseThrow(() -> new JqwikException("Cannot generate a value"))
			.orElse(null);
		List<BuilderManipulator> specArbitraryManipulators = spec.getBuilderManipulators();
		arbitraryBuilder.apply(specArbitraryManipulators);
	}

	@Override
	public BuilderManipulator copy() {
		List<ExpressionSpec> copiedSpecs = specs.stream()
			.map(ExpressionSpec::copy)
			.collect(toList());

		return new ArbitrarySpecAny(copiedSpecs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitrarySpecAny that = (ArbitrarySpecAny)obj;
		return specs.equals(that.specs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(specs);
	}
}
