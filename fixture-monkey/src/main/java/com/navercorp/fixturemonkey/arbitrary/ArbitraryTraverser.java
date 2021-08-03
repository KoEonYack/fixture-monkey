package com.navercorp.fixturemonkey.arbitrary;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
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
		doTraverse(node, keyOfMapStructure, true, fieldNameResolver);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> void doTraverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure,
		boolean active,
		FieldNameResolver fieldNameResolver
	) {
		node.getChildren().clear();
		initializeDefaultArbitrary(node);
		LazyValue<T> nowValue = node.getValue();
		ArbitraryType<T> nowNodeType = node.getType();
		Class<?> clazz = nowNodeType.getType();

		if (isTraversable(nowNodeType)) {
			List<Field> fields = getFields(clazz);
			for (Field field : fields) {
				ArbitraryType arbitraryType = getArbitraryType(field);
				double nullInject = arbitraryOption.getNullInject();
				boolean defaultNotNull = arbitraryOption.isDefaultNotNull();
				boolean nullable = isNullableField(field, defaultNotNull);
				LazyValue<?> nextValue = getNextValue(nowValue, field);
				nullable = nextValue == null && nullable;
				boolean nextActive = (nextValue == null || !nextValue.isEmpty()) && active;
				if (nowValue != null && nowValue.isEmpty()) {
					node.setArbitrary(Arbitraries.just(null));
				}

				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(arbitraryType)
					.fieldName(fieldNameResolver.resolveFieldName(field))
					.nullable(nullable)
					.nullInject(nullInject)
					.keyOfMapStructure(keyOfMapStructure)
					.value(nextValue)
					.active(nextActive)
					.build();

				node.addChildNode(nextNode);
				doTraverse(nextNode, false, active, fieldNameResolver);
			}
		} else if (nowNodeType.isContainer()) {
			if (nowNodeType.isMap() || nowNodeType.isMapEntry()) {
				mapArbitrary(node, fieldNameResolver);
			} else if (nowNodeType.isArray()) {
				traverseContainer(node, active, fieldNameResolver, ArrayArbitraryNodeGenerator.INSTANCE);
			} else if (nowNodeType.isOptional()) {
				traverseContainer(node, active, fieldNameResolver, OptionalArbitraryNodeGenerator.INSTANCE);
			} else {
				traverseContainer(node, active, fieldNameResolver, DefaultContainerArbitraryNodeGenerator.INSTANCE);
			}
			// TODO: noGeneric
		} else {
			if (nowValue != null) {
				node.setArbitrary(Arbitraries.just(nowValue.get()));
			} else if (arbitraryOption.isDefaultArbitraryType(nowNodeType.getType())) {
				Arbitrary<T> registeredArbitrary = registeredArbitrary(node);
				node.setArbitrary(registeredArbitrary);
			} else if (nowNodeType.isEnum()) {
				Arbitrary<T> arbitrary = (Arbitrary<T>)Arbitraries.of((Class<Enum>)clazz);
				node.setArbitrary(arbitrary);
			} else if (nowNodeType.isInterface() || nowNodeType.isAbstract()) {
				InterfaceSupplier interfaceSupplier =
					arbitraryOption.getInterfaceSupplierOrDefault(nowNodeType.getType());
				node.setArbitrary(Arbitraries.just((T)interfaceSupplier.get(nowNodeType.getType())));
			} else {
				node.setArbitrary(Arbitraries.just(null));
			}
		}
	}

	@Nullable
	private <T> LazyValue<?> getNextValue(LazyValue<T> currentValue, Field field) {
		if (currentValue == null) {
			return null;
		}
		return currentValue.isEmpty() ? null : new LazyValue<>(extractValue(currentValue.get(), field));
	}

	@SuppressWarnings("unchecked")
	private <T> void initializeDefaultArbitrary(ArbitraryNode<T> node) {
		Class<?> clazz = node.getType().getType();
		ArbitraryBuilder<?> defaultArbitraryBuilder = arbitraryOption.getDefaultArbitraryBuilder(clazz);

		if (defaultArbitraryBuilder != null && !node.isHead()) {
			node.setValue(() -> (T)defaultArbitraryBuilder.sample());
			node.setManipulated(true); // TODO: decompose 정책 수정 후 제거
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
		ArbitraryType arbitraryType = getArbitraryType(field);
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
	private ArbitraryType getArbitraryType(Field field) {
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
	private <T, K, V> void mapArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		LazyValue<T> lazyValue = currentNode.getValue();
		if (lazyValue != null) {
			currentNode.setArbitrary(Arbitraries.just(lazyValue.get()));
			return;
		}
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
			ArbitraryNode<K> keyFrame = ArbitraryNode.builder()
				.type(keyType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.keyOfMapStructure(true)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(keyFrame);
			doTraverse(keyFrame, true, true, fieldNameResolver);

			ArbitraryNode<V> valueFrame = ArbitraryNode.builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(valueFrame);
			doTraverse(valueFrame, false, true, fieldNameResolver);
		}
	}

	private <T> void traverseContainer(
		ArbitraryNode<T> currentNode,
		boolean active,
		FieldNameResolver fieldNameResolver,
		ContainerArbitraryNodeGenerator containerArbitraryNodeGenerator
	) {
		List<ArbitraryNode<?>> nodes = containerArbitraryNodeGenerator.generate(currentNode, fieldNameResolver);
		for (ArbitraryNode<?> node : nodes) {
			currentNode.addChildNode(node);
			// TODO: map일 때 key 처리
			doTraverse(node, false, active, fieldNameResolver);
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
