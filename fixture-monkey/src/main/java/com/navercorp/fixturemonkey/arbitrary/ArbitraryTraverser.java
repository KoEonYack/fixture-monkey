package com.navercorp.fixturemonkey.arbitrary;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.generator.AnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.AnnotationSource;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public final class ArbitraryTraverser {
	public static final ArbitraryTraverser INSTANCE = new ArbitraryTraverser(ArbitraryOption.DEFAULT_FIXTURE_OPTIONS);
	private final ArbitraryOption arbitraryOption;

	public ArbitraryTraverser(ArbitraryOption arbitraryOption) {
		this.arbitraryOption = arbitraryOption;
	}

	public <T> void traverse(ArbitraryNode<T> node, boolean keyOfMapStructure, FieldNameResolver fieldNameResolver) {
		LazyValue<T> value = node.getValue();
		if (value != null) {
			value.clear();
		}
		doTraverse(node, keyOfMapStructure, fieldNameResolver);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> void doTraverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure,
		FieldNameResolver fieldNameResolver
	) {
		node.getChildren().clear();
		LazyValue<T> lazyValue = node.getValue();
		ArbitraryType<T> currentNodeType = node.getType();
		Class<?> clazz = currentNodeType.getType();

		List<Field> fields = getFields(clazz);
		if (isTraversable(currentNodeType)) {
			for (Field field : fields) {
				ArbitraryType arbitraryType = getFixtureType(field);
				double nullInject = arbitraryOption.getNullInject();
				boolean defaultNotNull = arbitraryOption.isDefaultNotNull();
				boolean nullable = isNullableField(field, defaultNotNull);
				LazyValue<?> nextValue;
				if (lazyValue != null) {
					T value = lazyValue.get();
					if (value == null) {
						nextValue = null;
					} else {
						nextValue = new LazyValue<>(extractValue(value, field));
						nullable = false;
					}
				} else {
					nextValue = null;
				}

				ArbitraryNode<?> nextFrame = ArbitraryNode.builder()
					.type(arbitraryType)
					.fieldName(fieldNameResolver.resolveFieldName(field))
					.nullable(nullable)
					.nullInject(nullInject)
					.keyOfMapStructure(keyOfMapStructure)
					.value(nextValue)
					.build();

				node.addChildNode(nextFrame);
				doTraverse(nextFrame, false, fieldNameResolver);
			}
		} else {
			if (!currentNodeType.isContainer() && lazyValue != null) {
				node.setArbitrary(Arbitraries.just(lazyValue.get()));
			} else if (arbitraryOption.isDefaultArbitraryType(currentNodeType.getType())) {
				Arbitrary<T> registeredArbitrary = registeredArbitrary(node);
				node.setArbitrary(registeredArbitrary);
			} else if (currentNodeType.isContainer()) {
				if (currentNodeType.isMap() || currentNodeType.isMapEntry()) {
					if (lazyValue != null) {
						node.setArbitrary(Arbitraries.just(lazyValue.get()));
						return;
					}
					mapArbitrary(node, fieldNameResolver);
				} else if (currentNodeType.isArray()) {
					arrayArbitrary(node, fieldNameResolver);
				} else {
					containerArbitrary((ArbitraryNode<? extends Collection>)node, fieldNameResolver);
				}
			} else if (currentNodeType.isEnum()) {
				Arbitrary<T> arbitrary = (Arbitrary<T>)Arbitraries.of((Class<Enum>)clazz);
				node.setArbitrary(arbitrary);
			} else if (currentNodeType.isInterface() || currentNodeType.isAbstract()) {
				InterfaceSupplier interfaceSupplier =
					arbitraryOption.getInterfaceSupplierOrDefault(currentNodeType.getType());
				node.setArbitrary(Arbitraries.just((T)interfaceSupplier.get()));
			} else {
				node.setArbitrary(Arbitraries.just(null));
			}
			// TODO: noGeneric
		}
	}

	private List<Field> getFields(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}

		return ReflectionUtils.findFields(
			clazz,
			this::availableField,
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
		);
	}

	private <T> Object extractValue(T value, Field field) {
		try {
			field.setAccessible(true);
			return field.get(value);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Can not extract value");
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean isNullableField(Field field, boolean defaultNotNull) {
		ArbitraryType arbitraryType = getFixtureType(field);
		boolean nullable = arbitraryOption.getNullableArbitraryEvaluator().isNullable(field);
		if (arbitraryType.isContainer()) {
			return nullable && arbitraryOption.isNullableContainer();
		} else if (arbitraryType.isPrimitive()) {
			return false;
		} else if (arbitraryType.getAnnotation(NotEmpty.class) != null
			|| (field.getType() == String.class && arbitraryType.getAnnotation(NotBlank.class) != null)
		) {
			return false;
		} else {
			if (arbitraryType.getAnnotation(Nullable.class) != null) {
				return true;
			}
			if (!nullable) {
				return false;
			}

			boolean hasNotNullAnnotations = arbitraryType.getAnnotations().stream()
				.noneMatch(it -> arbitraryOption.isNonNullAnnotation((Annotation)it));

			if (!hasNotNullAnnotations) {
				return false;
			}

			return !defaultNotNull;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ArbitraryType getFixtureType(Field field) {
		List<Annotation> annotations = Arrays.asList(field.getAnnotations());
		return new ArbitraryType(field.getType(), field.getAnnotatedType(), annotations);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> Arbitrary<T> registeredArbitrary(ArbitraryNode<T> currentNode) {
		ArbitraryType type = currentNode.getType();
		AnnotatedType argsType = type.getAnnotatedType();

		Class<T> clazz;
		if (argsType != null) {
			clazz = (Class<T>)argsType.getType();
		} else {
			clazz = type.getType();
		}

		AnnotationSource annotationSource = new AnnotationSource(argsType);

		Map<Class<?>, AnnotatedArbitraryGenerator<?>> annotatedArbitraryMap =
			arbitraryOption.getAnnotatedArbitraryMap();

		return (Arbitrary<T>)Optional.ofNullable(annotatedArbitraryMap.get(clazz))
			.map(arbitraryGenerator -> arbitraryGenerator.generate(annotationSource))
			.orElseThrow(() -> new IllegalArgumentException("Class is not registered " + clazz.getName()));
	}

	@SuppressWarnings("unchecked")
	private <T, U> void containerArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getGenericFixtureType(0);

		LazyValue<T> lazyValue = currentNode.getValue();

		int currentIndex = 0;
		int elementSize = Integer.MAX_VALUE;

		if (lazyValue != null) {
			T value = lazyValue.get();
			if (value == null) {
				currentNode.setArbitrary(Arbitraries.just(null));
				return;
			}
			Iterator<U> iterator;
			if (value instanceof Collection || value instanceof Iterator || value instanceof Stream) {
				if (value instanceof Collection) {
					iterator = ((Collection<U>)value).iterator();
				} else if (value instanceof Stream) {
					iterator = ((Stream<U>)value).iterator();
				} else {
					iterator = (Iterator<U>)value;
				}

				ContainerSizeConstraint containerSizeConstraint = currentNode.getContainerSizeConstraint();
				if (containerSizeConstraint != null) {
					elementSize = containerSizeConstraint.getArbitraryElementSize();
				}

				while (currentIndex < elementSize && iterator.hasNext()) {
					U nextObject = iterator.next();
					ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
						.type(childType)
						.value(nextObject)
						.fieldName(fieldName)
						.indexOfIterable(currentIndex)
						.build();
					currentNode.addChildNode(nextNode);
					doTraverse(nextNode, false, fieldNameResolver);
					currentIndex++;
				}

				if (containerSizeConstraint == null) {
					currentNode.setContainerSizeConstraint(
						new ContainerSizeConstraint(currentIndex, currentIndex)
					);
					return;
				}
			} else {
				if (clazz.isOptional()) {
					Optional<U> optional = ((Optional<U>)value);
					if (!optional.isPresent()) {
						return;
					}
					U nextObject = optional.get();
					ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
						.type(childType)
						.value(nextObject)
						.fieldName(fieldName)
						.indexOfIterable(0)
						.build();
					currentNode.addChildNode(nextNode);
					doTraverse(nextNode, false, fieldNameResolver);
					return;
				}
			}
		}

		currentNode.initializeElementSize();
		if (elementSize == Integer.MAX_VALUE) {
			elementSize = currentNode.getContainerSizeConstraint().getArbitraryElementSize();
		}

		for (int i = currentIndex; i < elementSize; i++) {
			ArbitraryNode<U> genericFrame = ArbitraryNode.<U>builder()
				.type(childType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(genericFrame);
			doTraverse(genericFrame, false, fieldNameResolver);
		}
	}

	@SuppressWarnings("unchecked")
	private <T, U> void arrayArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getArrayFixtureType();

		LazyValue<T> lazyValue = currentNode.getValue();
		ContainerSizeConstraint containerSizeConstraint = currentNode.getContainerSizeConstraint();
		int elementSize = Integer.MAX_VALUE;
		int currentIndex = 0;

		if (lazyValue != null) {
			T value = lazyValue.get();
			if (value == null) {
				currentNode.setArbitrary(Arbitraries.just(null));
				return;
			}
			int length = Array.getLength(value);

			if (containerSizeConstraint != null) {
				elementSize = containerSizeConstraint.getArbitraryElementSize();
			}

			for (currentIndex = 0; currentIndex < length && currentIndex < elementSize; currentIndex++) {
				U nextValue = (U)Array.get(value, currentIndex);
				ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
					.type(childType)
					.fieldName(fieldName)
					.indexOfIterable(currentIndex)
					.value(nextValue)
					.build();
				currentNode.addChildNode(nextNode);
				doTraverse(nextNode, false, fieldNameResolver);
			}

			if (containerSizeConstraint == null) {
				currentNode.setContainerSizeConstraint(new ContainerSizeConstraint(length, length));
				return;
			}
		}

		currentNode.initializeElementSize();
		if (elementSize == Integer.MAX_VALUE) {
			elementSize = containerSizeConstraint.getArbitraryElementSize();
		}

		for (int i = currentIndex; i < elementSize; i++) {
			ArbitraryNode<U> genericFrame = ArbitraryNode.<U>builder()
				.type(childType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(genericFrame);
			doTraverse(genericFrame, false, fieldNameResolver);
		}
	}

	private <T, K, V> void mapArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<K> keyType = clazz.getGenericFixtureType(0);
		ArbitraryType<V> valueType = clazz.getGenericFixtureType(1);

		currentNode.initializeElementSize();

		int elementSize = currentNode.getContainerSizeConstraint().getArbitraryElementSize();

		if (clazz.isMapEntry()) {
			elementSize = 1;
		}

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<K> keyFrame = ArbitraryNode.<K>builder()
				.type(keyType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.keyOfMapStructure(true)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(keyFrame);
			doTraverse(keyFrame, true, fieldNameResolver);

			ArbitraryNode<V> valueFrame = ArbitraryNode.<V>builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(valueFrame);
			doTraverse(valueFrame, false, fieldNameResolver);
		}
	}

	private boolean availableField(Field field) {
		return !Modifier.isStatic(field.getModifiers());
	}

	private boolean isTraversable(ArbitraryType<?> type) {
		Class<?> clazz = type.getType();
		if (clazz == null) {
			return false;
		}

		return arbitraryOption.isExceptGeneratePackage(clazz)
			&& !arbitraryOption.isDefaultArbitraryType(clazz)
			&& !type.isContainer()
			&& !type.isOptional()
			&& !type.isEnum()
			&& !type.isInterface()
			&& !type.isAbstract();
	}
}
