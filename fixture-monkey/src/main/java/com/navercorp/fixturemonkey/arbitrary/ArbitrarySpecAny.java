package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

public final class ArbitrarySpecAny implements BuilderManipulator {
	private final List<ExpressionSpec> specs;

	public ArbitrarySpecAny(List<ExpressionSpec> specs) {
		this.specs = specs;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void accept(ArbitraryBuilder arbitraryBuilder) {
		ExpressionSpec spec = Arbitraries.of(specs).sample();
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
