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

package com.navercorp.fixturemonkey.customizer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.arbitrary.AbstractArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPostCondition;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPrefix;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetSuffix;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;

public final class ExpressionSpec {
	private final List<BuilderManipulator> builderManipulators;

	public ExpressionSpec() {
		this(new ArrayList<>());
	}

	public ExpressionSpec(List<BuilderManipulator> builderManipulators) {
		this.builderManipulators = builderManipulators;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, @Nullable Object value) {
		if (value == null) {
			return this.setNull(expression);
		}
		if (value instanceof Arbitrary) {
			return this.set(expression, (Arbitrary<?>)value);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySet(fixtureExpression, value));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, Object value, long limit) {
		if (value == null) {
			return this.setNull(expression);
		}
		if (value instanceof Arbitrary) {
			return this.set(expression, (Arbitrary<?>)value);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySet(fixtureExpression, value, limit));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> ExpressionSpec set(String expression, Arbitrary<T> arbitrary) {
		if (arbitrary == null) {
			return this.setNull(null);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary(fixtureExpression, arbitrary));
		return this;
	}

	public <T> ExpressionSpec setBuilder(String expression, @Nullable ArbitraryBuilder<T> builder, long limit) {
		if (builder == null) {
			return this.setNull(null);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary<>(fixtureExpression, builder.build(), limit));
		return this;
	}

	public <T> ExpressionSpec setBuilder(String expression, @Nullable ArbitraryBuilder<T> builder) {
		if (builder == null) {
			return this.setNull(null);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary<>(fixtureExpression, builder.build()));
		return this;
	}

	public ExpressionSpec set(String expression, ExpressionSpec spec) {
		if (spec == null) {
			return this.setNull(expression);
		}

		ExpressionSpec copied = spec.copy();
		for (BuilderManipulator arbitraryManipulator : copied.builderManipulators) {
			if (arbitraryManipulator instanceof AbstractArbitraryExpressionManipulator) {
				((AbstractArbitraryExpressionManipulator)arbitraryManipulator).addPrefix(expression);
			}
		}
		this.merge(copied);
		return this;
	}

	public ExpressionSpec setPrefix(String expression, String value) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetPrefix(fixtureExpression, Arbitraries.just(value)));
		return this;
	}

	public ExpressionSpec setSuffix(String expression, String value) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetSuffix(fixtureExpression, Arbitraries.just(value)));
		return this;
	}

	public ExpressionSpec setNull(String expression) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitraryNullity(fixtureExpression, true));
		return this;
	}

	public ExpressionSpec setNotNull(String expression) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitraryNullity(fixtureExpression, false));
		return this;
	}

	public ExpressionSpec size(String expression, int size) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, size, size));
		return this;
	}

	public ExpressionSpec size(String expression, int min, int max) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, min, max));
		return this;
	}

	public ExpressionSpec minSize(String expression, int min) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, min, null));
		return this;
	}

	public ExpressionSpec maxSize(String expression, int max) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, null, max));
		return this;
	}

	public <T> ExpressionSpec setPostCondition(String expression, Class<T> clazz, Predicate<T> predicate, long count) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, fixtureExpression, predicate, count));
		return this;
	}

	public <T> ExpressionSpec setPostCondition(String expression, Class<T> clazz, Predicate<T> predicate) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, fixtureExpression, predicate));
		return this;
	}

	public ExpressionSpec list(String iterableName, Consumer<IterableSpec> iterableSpecSupplier) {
		DefaultIterableSpec iterableSpec = new DefaultIterableSpec(iterableName);
		iterableSpecSupplier.accept(iterableSpec);
		iterableSpec.visit(this);
		return this;
	}

	public ExpressionSpec copy() {
		List<BuilderManipulator> copiedArbitraryManipulators = this.builderManipulators.stream()
			.map(BuilderManipulator::copy)
			.collect(toList());

		return new ExpressionSpec(copiedArbitraryManipulators);
	}

	@SuppressWarnings("rawtypes")
	public ExpressionSpec merge(ExpressionSpec fixtureSpec, boolean overwrite) {
		List<BuilderManipulator> filteredOrderedArbitraryManipulators = fixtureSpec.builderManipulators.stream()
			.filter(it -> !(it instanceof PostArbitraryManipulator) && !(it instanceof MetadataManipulator))
			.map(ArbitraryExpressionManipulator.class::cast)
			.filter(it -> overwrite || !hasOrderedManipulators(it.getArbitraryExpression().toString()))
			.map(BuilderManipulator.class::cast)
			.collect(toList());

		this.builderManipulators.addAll(filteredOrderedArbitraryManipulators);

		List<PostArbitraryManipulator> postArbitraryManipulators = fixtureSpec.builderManipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.collect(toList());

		if (overwrite) {
			this.builderManipulators.removeIf(
				it -> it instanceof PostArbitraryManipulator
					&& fixtureSpec.hasPostArbitraryManipulators(
					((PostArbitraryManipulator<?>)it).getArbitraryExpression().toString()
				)
			); // remove redundant fixtureExpression
			this.builderManipulators.addAll(postArbitraryManipulators);
		} else {
			List<PostArbitraryManipulator> filteredPostArbitraryManipulators = postArbitraryManipulators.stream()
				.filter(it -> !this.hasPostArbitraryManipulators(it.getArbitraryExpression().toString()))
				.collect(toList());
			this.builderManipulators.addAll(filteredPostArbitraryManipulators);
		}

		List<MetadataManipulator> filteredMetadataManipulators = fixtureSpec.builderManipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.filter(it -> overwrite || this.hasMetadata(it.getArbitraryExpression().toString()))
			.collect(toList());

		this.builderManipulators.addAll(filteredMetadataManipulators);

		return this;
	}

	public ExpressionSpec merge(ExpressionSpec fixtureSpec) {
		return this.merge(fixtureSpec, true);
	}

	public ExpressionSpec exclude(String... excludeExpressions) {
		List<ArbitraryExpression> excludeArbitraryExpression = Arrays.stream(excludeExpressions)
			.map(ArbitraryExpression::from)
			.collect(toList());

		this.builderManipulators.removeIf(
			it -> it instanceof AbstractArbitraryExpressionManipulator
				&& excludeArbitraryExpression.contains(
				((AbstractArbitraryExpressionManipulator)it).getArbitraryExpression()
			)
		);
		return this;
	}

	public boolean hasOrderedManipulators(String expression) {
		return this.builderManipulators.stream()
			.filter(it -> !(it instanceof PostArbitraryManipulator) && !(it instanceof MetadataManipulator))
			.map(ArbitraryExpressionManipulator.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasPostArbitraryManipulators(String expression) {
		return this.builderManipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasSet(String expression) {
		return this.builderManipulators.stream()
			.filter(AbstractArbitrarySet.class::isInstance)
			.map(AbstractArbitrarySet.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasPostCondition(String expression) {
		return this.builderManipulators.stream()
			.filter(ArbitrarySetPostCondition.class::isInstance)
			.map(ArbitrarySetPostCondition.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasMetadata(String expression) {
		return this.builderManipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public Optional<Object> findSetValue(String expression) {
		return this.builderManipulators.stream()
			.filter(AbstractArbitrarySet.class::isInstance)
			.map(AbstractArbitrarySet.class::cast)
			.filter(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)))
			.map(AbstractArbitrarySet::getValue)
			.findAny();
	}

	public List<BuilderManipulator> getBuilderManipulators() {
		return builderManipulators;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ExpressionSpec that = (ExpressionSpec)obj;
		return builderManipulators.equals(that.builderManipulators);
	}

	@Override
	public int hashCode() {
		return Objects.hash(builderManipulators);
	}
}
