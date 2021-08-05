package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPostCondition;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySpecAny;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTraverser;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTree;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.LazyValue;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public final class ArbitraryBuilder<T> {
	private final ArbitraryTree<T> tree;
	private final ArbitraryTraverser traverser;
	private final List<BuilderManipulator> builderManipulators = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private final ArbitraryValidator validator;
	private final Map<Class<?>, ArbitraryGenerator> generatorMap;

	private ArbitraryGenerator generator;
	private ArbitraryCustomizers arbitraryCustomizers;
	private boolean validOnly = true;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder(
		Class<T> clazz,
		ArbitraryOption options,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this(ArbitraryNode.builder()
				.type(new ArbitraryType(clazz))
				.fieldName("HEAD_NAME")
				.build(),
			new ArbitraryTraverser(options),
			generator,
			validator,
			arbitraryCustomizers,
			generatorMap
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder(
		T value,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this(
			ArbitraryNode.builder()
				.type(new ArbitraryType(value.getClass()))
				.value(() -> value)
				.fieldName("HEAD_NAME")
				.build(),
			traverser,
			generator,
			validator,
			arbitraryCustomizers,
			generatorMap
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ArbitraryBuilder(
		Supplier<T> valueSupplier,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this(
			ArbitraryNode.builder()
				.value(valueSupplier)
				.fieldName("HEAD_NAME")
				.build(),
			traverser,
			generator,
			validator,
			arbitraryCustomizers,
			generatorMap
		);
	}

	@SuppressWarnings("rawtypes")
	private ArbitraryBuilder(
		ArbitraryNode<T> node,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this(
			new ArbitraryTree<>(node),
			traverser,
			generator,
			validator,
			arbitraryCustomizers,
			new ArrayList<>(),
			generatorMap
		);
	}

	@SuppressWarnings("rawtypes")
	private ArbitraryBuilder(
		ArbitraryTree<T> tree,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		List<BuilderManipulator> builderManipulators,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this.tree = tree;
		this.traverser = traverser;
		this.generator = getGenerator(generator, arbitraryCustomizers);
		this.validator = validator;
		this.arbitraryCustomizers = arbitraryCustomizers;
		this.builderManipulators.addAll(builderManipulators);
		this.generatorMap = generatorMap.entrySet().stream()
			.map(it -> new SimpleEntry<Class<?>, ArbitraryGenerator>(
				it.getKey(),
				getGenerator(it.getValue(), arbitraryCustomizers))
			)
			.collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));
	}

	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.validOnly = validOnly;
		return this;
	}

	public ArbitraryBuilder<T> generator(ArbitraryGenerator generator) {
		this.generator = getGenerator(generator, arbitraryCustomizers);
		return this;
	}

	@SuppressWarnings("unchecked")
	public Arbitrary<T> build() {
		ArbitraryBuilder<T> buildArbitraryBuilder = this.copy();

		return buildArbitraryBuilder.tree.result(() -> {
			ArbitraryTree<T> buildTree = buildArbitraryBuilder.tree;

			buildArbitraryBuilder.traverser.traverse(
				buildTree.getHead(),
				false,
				buildArbitraryBuilder.generator
			);
			buildArbitraryBuilder.apply(buildArbitraryBuilder.builderManipulators);
			buildTree.update(buildArbitraryBuilder.generator, generatorMap);
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
		applyToRootValue(builder -> {
			T sample = builder.sample();
			builder.tree.getHead().setValue(() -> sample); // fix builder value
			if (predicate.test(sample)) {
				self.accept(builder);
				return builder.sample();
			}
			return sample;
		});

		return this;
	}

	public ArbitraryBuilder<T> spec(ExpressionSpec expressionSpec) {
		this.builderManipulators.addAll(expressionSpec.getBuilderManipulators());
		return this;
	}

	public ArbitraryBuilder<T> specAny(ExpressionSpec... expressionSpecs) {
		this.builderManipulators.add(new ArbitrarySpecAny(Arrays.asList(expressionSpecs)));
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
		this.builderManipulators.add(new ArbitrarySet<>(arbitraryExpression, value));
		return this;
	}

	public ArbitraryBuilder<T> set(String expression, @Nullable Object value, long limit) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySet<>(arbitraryExpression, value, limit));
		return this;
	}

	public ArbitraryBuilder<T> setNull(String expression) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitraryNullity(arbitraryExpression, true));
		return this;
	}

	public ArbitraryBuilder<T> setNotNull(String expression) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitraryNullity(arbitraryExpression, false));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryBuilder<T> setNullity(ArbitraryNullity arbitraryNullity) {
		ArbitraryExpression arbitraryExpression = arbitraryNullity.getArbitraryExpression();
		Collection<ArbitraryNode> foundNodes = tree.findAll(arbitraryExpression);
		for (ArbitraryNode foundNode : foundNodes) {
			LazyValue<T> value = foundNode.getValue();
			if (value != null && value.isEmpty()) { // decompose null value
				foundNode.clearValue();
				traverser.traverse(foundNode, foundNode.isKeyOfMapStructure(), generator);
			} else {
				foundNode.apply(arbitraryNullity);
			}
		}
		return this;
	}

	public ArbitraryBuilder<T> set(String expression, @Nullable Arbitrary<T> value) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, value));
		return this;
	}

	public ArbitraryBuilder<T> setBuilder(String expression, @Nullable ArbitraryBuilder<?> builder, long limit) {
		if (builder == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, builder.build(), limit));
		return this;
	}

	public ArbitraryBuilder<T> setBuilder(String expression, @Nullable ArbitraryBuilder<?> builder) {
		if (builder == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, builder.build()));
		return this;
	}

	public <U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> clazz, Predicate<U> filter,
		long limit) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, arbitraryExpression, filter, limit));
		return this;
	}

	public <U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> clazz, Predicate<U> filter) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, arbitraryExpression, filter));
		return this;
	}

	public ArbitraryBuilder<T> size(String expression, int size) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(arbitraryExpression, size, size));
		return this;
	}

	public ArbitraryBuilder<T> size(String expression, int min, int max) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(arbitraryExpression, min, max));
		return this;
	}

	public ArbitraryBuilder<T> minSize(String expression, int min) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(
			new ContainerSizeManipulator(arbitraryExpression, min, null)
		);
		return this;
	}

	public ArbitraryBuilder<T> maxSize(String expression, int max) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(arbitraryExpression, null, max));
		return this;
	}

	public ArbitraryBuilder<T> customize(Class<T> type, ArbitraryCustomizer<T> customizer) {
		this.arbitraryCustomizers = this.arbitraryCustomizers.mergeWith(
			Collections.singletonMap(type, customizer)
		);

		if (this.generator instanceof WithFixtureCustomizer) {
			this.generator = ((WithFixtureCustomizer)this.generator).withFixtureCustomizers(arbitraryCustomizers);
		}
		return this;
	}

	public ArbitraryBuilder<T> apply(BiConsumer<T, ArbitraryBuilder<T>> mapper) {
		applyToRootValue(builder -> {
			T sample = builder.sample();
			builder.tree.getHead().setValue(() -> sample); // fix builder value
			mapper.accept(sample, builder);
			return builder.sample();
		});

		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryBuilder<T> apply(MetadataManipulator manipulator) {
		ArbitraryExpression arbitraryExpression = manipulator.getArbitraryExpression();

		if (manipulator instanceof ContainerSizeManipulator) {
			ContainerSizeManipulator containerSizeManipulator = ((ContainerSizeManipulator)manipulator);
			Integer min = containerSizeManipulator.getMin();
			Integer max = containerSizeManipulator.getMax();

			Collection<ArbitraryNode> foundNodes = tree.findAll(arbitraryExpression);
			for (ArbitraryNode foundNode : foundNodes) {
				if (!foundNode.getType().isContainer()) {
					throw new IllegalArgumentException("Only Container can set size");
				}
				foundNode.setContainerMinSize(min);
				foundNode.setContainerMaxSize(max);
				traverser.traverse(foundNode, false, generator); // regenerate subtree
			}
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder<T> apply(PostArbitraryManipulator<T> postArbitraryManipulator) {
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

	@SuppressWarnings("rawtypes")
	public void apply(List<BuilderManipulator> arbitraryManipulators) {
		List<MetadataManipulator> metadataManipulators = this.extractMetadataManipulatorsFrom(arbitraryManipulators);
		List<BuilderManipulator> orderedArbitraryManipulators =
			this.extractOrderedManipulatorsFrom(arbitraryManipulators);
		List<PostArbitraryManipulator> postArbitraryManipulators =
			this.extractPostArbitraryManipulatorsFrom(arbitraryManipulators);

		metadataManipulators.stream().sorted().forEachOrdered(it -> it.accept(this));
		orderedArbitraryManipulators.forEach(it -> it.accept(this));
		postArbitraryManipulators.forEach(it -> it.accept(this));
	}

	public <U> ArbitraryBuilder<U> map(Function<T, U> mapper) {
		return new ArbitraryBuilder<>(() -> mapper.apply(this.sample()),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	public <U, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		BiFunction<T, U, R> combinator
	) {
		return new ArbitraryBuilder<>(() -> combinator.apply(this.sample(), other.sample()),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	public <U, V, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	) {
		return new ArbitraryBuilder<>(() -> combinator.apply(
			this.sample(),
			other.sample(),
			another.sample()
		),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap);
	}

	public <U, V, W, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	) {
		return new ArbitraryBuilder<>(() -> combinator.apply(
			this.sample(),
			other.sample(),
			another.sample(),
			theOther.sample()
		),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <U> ArbitraryBuilder<U> zipWith(
		List<ArbitraryBuilder<?>> others,
		Function<List<?>, U> combinator
	) {
		return new ArbitraryBuilder<>(() -> {
			List combinedList = new ArrayList<>();
			combinedList.add(this.sample());
			for (ArbitraryBuilder<?> other : others) {
				combinedList.add(other.sample());
			}
			return combinator.apply(combinedList);
		},
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	private void applyToRootValue(Function<ArbitraryBuilder<T>, T> valueSupplier) {
		ArbitraryBuilder<T> copied = this.copy();
		this.tree.getHead().setValue(() -> valueSupplier.apply(copied));
		this.builderManipulators.clear();
	}

	public ArbitraryBuilder<T> copy() {
		return new ArbitraryBuilder<>(
			this.tree.copy(),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.builderManipulators.stream().map(BuilderManipulator::copy).collect(toList()),
			this.generatorMap
		);
	}

	public ExpressionSpec toExpressionSpec() {
		return new ExpressionSpec(this.builderManipulators.stream()
			.map(BuilderManipulator::copy)
			.collect(toList()));
	}

	private List<BuilderManipulator> extractOrderedManipulatorsFrom(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(it -> !(it instanceof MetadataManipulator))
			.filter(it -> !(it instanceof PostArbitraryManipulator))
			.collect(toList());
	}

	private List<MetadataManipulator> extractMetadataManipulatorsFrom(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.sorted()
			.collect(toList());
	}

	@SuppressWarnings("rawtypes")
	private List<PostArbitraryManipulator> extractPostArbitraryManipulatorsFrom(
		List<BuilderManipulator> manipulators
	) {
		return manipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.collect(toList());
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
			&& builderManipulators.equals(that.builderManipulators);
	}

	@Override
	public int hashCode() {
		Class<?> generateClazz = tree.getHead().getType().getType();
		return Objects.hash(generateClazz, builderManipulators);
	}

	private ArbitraryGenerator getGenerator(ArbitraryGenerator generator, ArbitraryCustomizers customizers) {
		if (generator instanceof WithFixtureCustomizer) {
			generator = ((WithFixtureCustomizer)generator).withFixtureCustomizers(customizers);
		}
		return generator;
	}
}
