package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryFilter;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTraverser;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTree;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;
import com.navercorp.fixturemonkey.arbitrary.PreArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public final class ArbitraryBuilder<T> {
	private final ArbitraryTree<T> tree;
	private final ArbitraryTraverser traverser;
	@SuppressWarnings("rawtypes")
	private final List<PreArbitraryManipulator> preArbitraryManipulators = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private final List<PostArbitraryManipulator> postArbitraryManipulators = new ArrayList<>();
	private final List<MetadataManipulator> metadataManipulators = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private final ArbitraryValidator validator;
	private final ArbitraryCustomizers arbitraryCustomizers;

	private ArbitraryGenerator generator;
	private boolean validOnly = true;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder(
		Class<T> clazz,
		ArbitraryOption options,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this(ArbitraryNode.builder()
				.type(new ArbitraryType(clazz))
				.fieldName("HEAD_NAME")
				.build(),
			new ArbitraryTraverser(options),
			generator,
			validator,
			arbitraryCustomizers
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder(
		T value,
		ArbitraryTraverser fixtureTraverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this.tree = new ArbitraryTree<>(
			() -> ArbitraryNode.builder()
				.type(new ArbitraryType(value.getClass()))
				.valueSupplier(() -> value)
				.fieldName("HEAD_NAME")
				.build()
		);
		this.generator = generator;
		this.traverser = fixtureTraverser;
		this.validator = validator;
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings({"rawtypes"})
	private ArbitraryBuilder(
		Supplier<T> valueSupplier,
		ArbitraryTraverser fixtureTraverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this.tree = new ArbitraryTree<>(() ->
			ArbitraryNode.<T>builder()
				.valueSupplier(valueSupplier)
				.fieldName("HEAD_NAME")
				.build()
		);
		this.generator = generator;
		this.traverser = fixtureTraverser;
		this.validator = validator;
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings("rawtypes")
	private ArbitraryBuilder(
		ArbitraryTree<T> tree,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		List<PreArbitraryManipulator> preArbitraryManipulators,
		List<PostArbitraryManipulator> postArbitraryManipulators,
		List<MetadataManipulator> metadataManipulators
	) {
		this.tree = tree;
		this.traverser = traverser;
		this.generator = generator;
		this.validator = validator;
		this.arbitraryCustomizers = arbitraryCustomizers;
		this.preArbitraryManipulators.addAll(preArbitraryManipulators);
		this.postArbitraryManipulators.addAll(postArbitraryManipulators);
		this.metadataManipulators.addAll(metadataManipulators);
	}

	@SuppressWarnings("rawtypes")
	private ArbitraryBuilder(
		ArbitraryNode<T> node,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this.traverser = traverser;
		this.tree = new ArbitraryTree<>(() -> node);
		this.generator = generator;
		this.validator = validator;
		this.arbitraryCustomizers = arbitraryCustomizers;

	}

	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.validOnly = validOnly;
		return this;
	}

	public ArbitraryBuilder<T> generator(ArbitraryGenerator generator) {
		this.generator = generator;
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Arbitrary<T> build() {
		ArbitraryBuilder<T> buildArbitraryBuilder = this.copy();

		return buildArbitraryBuilder.tree.result(() -> {
			ArbitraryTree<T> buildTree = buildArbitraryBuilder.tree;
			List<MetadataManipulator> metadataManipulators = buildArbitraryBuilder.metadataManipulators;
			List<PreArbitraryManipulator> preArbitraryManipulators = buildArbitraryBuilder.preArbitraryManipulators;
			List<PostArbitraryManipulator> postArbitraryManipulators = buildArbitraryBuilder.postArbitraryManipulators;

			buildArbitraryBuilder.traverser.traverse(buildTree.getHead(), false, buildArbitraryBuilder.generator);
			metadataManipulators.stream().sorted().forEachOrdered(it -> it.accept(buildArbitraryBuilder));
			preArbitraryManipulators.forEach(it -> it.accept(buildArbitraryBuilder));
			postArbitraryManipulators.forEach(it -> it.accept(buildArbitraryBuilder));
			buildTree.update(buildArbitraryBuilder.generator);
			return buildTree.getHead().getArbitrary();
		}, this.validator, this.validOnly);
	}

	public T sample() {
		return this.build().sample();
	}

	public List<T> sampleList(int size) {
		return this.build().sampleStream().limit(size).collect(toList());
	}

	public ArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> self) {
		return new ArbitraryBuilder<>(() -> {
			T sample = this.sample();
			if (predicate.test(sample)) {
				ArbitraryBuilder<T> newArbitraryBuilder = new ArbitraryBuilder<>(
					sample,
					this.traverser,
					this.generator,
					this.validator,
					this.arbitraryCustomizers
				);
				self.accept(newArbitraryBuilder);
				return newArbitraryBuilder.sample();
			}
			return sample;
		},
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers
		);
	}

	public ArbitraryBuilder<T> spec(ExpressionSpec expressionSpec) {
		metadataManipulators.addAll(expressionSpec.getMetadataManipulators());
		preArbitraryManipulators.addAll(expressionSpec.getPreArbitraryManipulators());
		postArbitraryManipulators.addAll(expressionSpec.getPostArbitraryManipulators());
		return this;
	}

	@SuppressWarnings("unchecked")
	public ArbitraryBuilder<T> set(String expression, @Nullable Object value) {
		if (value == null) {
			return this.setNull(expression);
		}
		if (value instanceof Arbitrary) {
			return this.set(expression, (Arbitrary<T>)value);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.preArbitraryManipulators.add(new ArbitrarySet<>(arbitraryExpression, value));
		return this;
	}

	public ArbitraryBuilder<T> set(String expression, @Nullable Object value, long limit) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.preArbitraryManipulators.add(new ArbitrarySet<>(arbitraryExpression, value, limit));
		return this;
	}

	public ArbitraryBuilder<T> setNull(String expression) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.preArbitraryManipulators.add(new ArbitrarySetNullity<>(arbitraryExpression, true));
		return this;
	}

	public ArbitraryBuilder<T> setNotNull(String expression) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.preArbitraryManipulators.add(new ArbitrarySetNullity<>(arbitraryExpression, false));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryBuilder<T> setNullity(ArbitrarySetNullity<T> arbitrarySetNullity) {
		ArbitraryExpression arbitraryExpression = arbitrarySetNullity.getArbitraryExpression();
		Collection<ArbitraryNode> foundNodes = tree.findAll(arbitraryExpression);
		for (ArbitraryNode foundNode : foundNodes) {
			foundNode.apply(arbitrarySetNullity);
		}
		return this;
	}

	public ArbitraryBuilder<T> set(String expression, @Nullable Arbitrary<T> value) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.preArbitraryManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, value));
		return this;
	}

	public <U> ArbitraryBuilder<T> filter(String expression, Class<U> clazz, Predicate<U> filter, long limit) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.postArbitraryManipulators.add(new ArbitraryFilter<>(clazz, arbitraryExpression, filter, limit));
		return this;
	}

	public <U> ArbitraryBuilder<T> filter(String expression, Class<U> clazz, Predicate<U> filter) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.postArbitraryManipulators.add(new ArbitraryFilter<>(clazz, arbitraryExpression, filter));
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder<T> addPostArbitraryManipulator(PostArbitraryManipulator<T> postArbitraryManipulator) {
		Collection<ArbitraryNode> foundNodes = tree.findAll(postArbitraryManipulator.getArbitraryExpression());
		if (!foundNodes.isEmpty()) {
			for (ArbitraryNode<T> foundNode : foundNodes) {
				if (postArbitraryManipulator.isMappableTo(foundNode)) {
					foundNode.addArbitraryOperation(postArbitraryManipulator);
				}
			}
		}
		return this;
	}

	public ArbitraryBuilder<T> size(String expression, int size) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		metadataManipulators.add(new ContainerSizeManipulator(arbitraryExpression, size, size));
		return this;
	}

	public ArbitraryBuilder<T> size(String expression, int min, int max) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		metadataManipulators.add(new ContainerSizeManipulator(arbitraryExpression, min, max));
		return this;
	}

	public ArbitraryBuilder<T> minSize(String expression, int min) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		metadataManipulators.add(
			new ContainerSizeManipulator(arbitraryExpression, min, null)
		);
		return this;
	}

	public ArbitraryBuilder<T> maxSize(String expression, int max) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		metadataManipulators.add(new ContainerSizeManipulator(arbitraryExpression, null, max));
		return this;
	}

	public ArbitraryBuilder<T> customize(Class<T> type, ArbitraryCustomizer<T> customizer) {
		ArbitraryCustomizers newFixtureCustomizer = this.arbitraryCustomizers.mergeWith(
			Collections.singletonMap(type, customizer)
		);
		if (this.generator instanceof WithFixtureCustomizer) {
			this.generator = ((WithFixtureCustomizer)this.generator).withFixtureCustomizers(newFixtureCustomizer);
		}
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryBuilder<T> apply(ContainerSizeManipulator manipulator) {
		ArbitraryExpression arbitraryExpression = manipulator.getArbitraryExpression();
		Integer min = manipulator.getMin();
		Integer max = manipulator.getMax();

		Collection<ArbitraryNode> foundNodes = tree.findAll(arbitraryExpression);
		for (ArbitraryNode foundNode : foundNodes) {
			if (!foundNode.getType().isContainer()) {
				throw new IllegalArgumentException("Only Container can set size");
			}
			foundNode.setContainerMinSize(min);
			foundNode.setContainerMaxSize(max);
			traverser.traverse(foundNode, false, generator); // regenerate subtree
		}
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void apply(AbstractArbitrarySet<T> fixtureSet) {
		Collection<ArbitraryNode> foundNodes = tree.findAll(fixtureSet.getArbitraryExpression());

		if (!foundNodes.isEmpty()) {
			for (ArbitraryNode<T> foundNode : foundNodes) {
				foundNode.apply(fixtureSet);
			}
		}
	}

	public <U> ArbitraryBuilder<U> map(Function<T, U> mapper) {
		ArbitraryBuilder<T> copiedBuilder = this.copy();
		Supplier<U> mappedResult = () -> {
			T sample = copiedBuilder.sample();
			return mapper.apply(sample);
		};
		return new ArbitraryBuilder<>(
			mappedResult,
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers
		);
	}

	public <U, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		BiFunction<T, U, R> combinator
	) {
		ArbitraryBuilder<T> copied = this.copy();
		ArbitraryBuilder<U> copiedOther = other.copy();

		Supplier<R> result = () -> {
			Arbitrary<T> sample = copied.build();
			Arbitrary<U> otherSample = copiedOther.build();
			return combinator.apply(sample.sample(), otherSample.sample());
		};

		return new ArbitraryBuilder<>(
			result,
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers
		);
	}

	public ArbitraryBuilder<T> copy() {
		return new ArbitraryBuilder<>(
			this.tree.copy(),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.preArbitraryManipulators.stream().map(PreArbitraryManipulator::copy).collect(toList()),
			this.postArbitraryManipulators.stream().map(PostArbitraryManipulator::copy).collect(toList()),
			this.metadataManipulators.stream().map(MetadataManipulator::copy).collect(toList())
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryBuilder<?> that = (ArbitraryBuilder<?>)obj;
		Class<?> generateClazz = tree.getHead().getType().getType();
		Class<?> thatGenerateClazz = that.tree.getHead().getType().getType();

		return generateClazz.equals(thatGenerateClazz)
			&& metadataManipulators.equals(that.metadataManipulators)
			&& preArbitraryManipulators.equals(that.preArbitraryManipulators)
			&& postArbitraryManipulators.equals(that.postArbitraryManipulators);
	}

	@Override
	public int hashCode() {
		Class<?> generateClazz = tree.getHead().getType().getType();
		return Objects.hash(generateClazz, metadataManipulators, preArbitraryManipulators, postArbitraryManipulators);
	}
}
