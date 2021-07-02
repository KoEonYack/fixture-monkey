package com.navercorp.fixturemonkey.customizer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.AbstractArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryFilter;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPrefix;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetSuffix;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;
import com.navercorp.fixturemonkey.arbitrary.PreArbitraryManipulator;

public final class ExpressionSpec {
	private final List<BuilderManipulator> builderManipulators;

	public ExpressionSpec() {
		this(new ArrayList<>());
	}

	private ExpressionSpec(List<BuilderManipulator> builderManipulators) {
		this.builderManipulators = builderManipulators;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, Object value) {
		if (value == null) {
			return this.setNull(expression);
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
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySet(fixtureExpression, value, limit));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> ExpressionSpec set(String expression, Arbitrary<T> arbitrary) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary(fixtureExpression, arbitrary));
		return this;
	}

	public ExpressionSpec set(String expression, ExpressionSpec spec) {
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

	public ExpressionSpec size(String expression, int min, int max) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, min, max));
		return this;
	}

	public <T> ExpressionSpec filter(String name, Predicate<T> predicate, long count) {
		filterSpec(Object.class, name, predicate, count);
		return this;
	}

	public <T> ExpressionSpec filter(String name, Predicate<T> predicate) {
		filterSpec(Object.class, name, predicate);
		return this;
	}

	public ExpressionSpec filterByte(String name, Predicate<Byte> bytePredicate) {
		filterSpec(Byte.class, name, bytePredicate);
		return this;
	}

	public ExpressionSpec filterInteger(String name, Predicate<Integer> integerPredicate) {
		filterSpec(Integer.class, name, integerPredicate);
		return this;
	}

	public ExpressionSpec filterLong(String name, Predicate<Long> longPredicate) {
		filterSpec(Long.class, name, longPredicate);
		return this;
	}

	public ExpressionSpec filterFloat(String name, Predicate<Float> floatPredicate) {
		filterSpec(Float.class, name, floatPredicate);
		return this;
	}

	public ExpressionSpec filterDouble(String name, Predicate<Double> doublePredicate) {
		filterSpec(Double.class, name, doublePredicate);
		return this;
	}

	public ExpressionSpec filterCharacter(String name, Predicate<Character> characterPredicate) {
		filterSpec(Character.class, name, characterPredicate);
		return this;
	}

	public ExpressionSpec filterString(String name, Predicate<String> stringPredicate) {
		filterSpec(String.class, name, stringPredicate);
		return this;
	}

	public <T> ExpressionSpec filterList(String name, Predicate<List<T>> predicate) {
		filterSpec(List.class, name, predicate);
		return this;
	}

	public <T> ExpressionSpec filterSet(String name, Predicate<Set<T>> predicate) {
		filterSpec(Set.class, name, predicate);
		return this;
	}

	public <K, V> ExpressionSpec filterMap(String name, Predicate<Map<K, V>> predicate) {
		filterSpec(Map.class, name, predicate);
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
		List<PreArbitraryManipulator> filteredPreArbitraryManipulators = fixtureSpec.builderManipulators.stream()
			.filter(AbstractArbitrarySet.class::isInstance)
			.map(AbstractArbitrarySet.class::cast)
			.filter(it -> overwrite || !this.hasSet(it.getArbitraryExpression().toString()))
			.collect(toList());

		this.builderManipulators.addAll(filteredPreArbitraryManipulators);

		List<PostArbitraryManipulator> postArbitraryManipulators = fixtureSpec.builderManipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.collect(toList());

		if (overwrite) {
			this.builderManipulators.removeIf(
				it -> it instanceof ArbitraryFilter
					&& fixtureSpec.hasFilter(((ArbitraryFilter<?>)it).getArbitraryExpression().toString())
			); // remove redundant fixtureExpression
			this.builderManipulators.addAll(postArbitraryManipulators);
		} else {
			List<PostArbitraryManipulator> filteredPostArbitraryManipulators = postArbitraryManipulators.stream()
				.filter(it -> !this.hasFilter(it.getArbitraryExpression().toString()))
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

	public boolean hasSet(String expression) {
		return this.builderManipulators.stream()
			.filter(AbstractArbitrarySet.class::isInstance)
			.map(AbstractArbitrarySet.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasFilter(String expression) {
		return this.builderManipulators.stream()
			.filter(ArbitraryFilter.class::isInstance)
			.map(ArbitraryFilter.class::cast)
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> void filterSpec(Class<T> clazz, String expression, Predicate predicate, long count) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitraryFilter<T>(clazz, fixtureExpression, predicate, count));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> void filterSpec(Class<T> clazz, String expression, Predicate predicate) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitraryFilter<T>(clazz, fixtureExpression, predicate));
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
