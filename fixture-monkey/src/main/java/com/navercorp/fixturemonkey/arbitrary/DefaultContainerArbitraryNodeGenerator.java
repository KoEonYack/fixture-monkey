package com.navercorp.fixturemonkey.arbitrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class DefaultContainerArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final DefaultContainerArbitraryNodeGenerator INSTANCE = new DefaultContainerArbitraryNodeGenerator();

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<ArbitraryNode<?>> generate(
		ArbitraryNode<T> nowNode,
		FieldNameResolver fieldNameResolver
	) {
		int currentIndex = 0;
		int elementSize = Integer.MAX_VALUE;

		ArbitraryType<T> clazz = nowNode.getType();
		LazyValue<T> lazyValue = nowNode.getValue();
		String fieldName = nowNode.getFieldName();
		ArbitraryType<?> elementType = clazz.getGenericFixtureType(0);

		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		if (lazyValue != null) {
			if (lazyValue.isEmpty()) {
				nowNode.setArbitrary(Arbitraries.just(null));
				return generatedNodeList;
			}
			T value = lazyValue.get();

			if (!(value instanceof Collection || value instanceof Iterator || value instanceof Stream)) {
				throw new IllegalArgumentException(
					"Unsupported container type is given. " + value.getClass().getName());
			}
			Iterator<?> iterator = getIterator(value);

			ContainerSizeConstraint containerSizeConstraint = nowNode.getContainerSizeConstraint();
			if (containerSizeConstraint != null) {
				// container size is set.
				elementSize = containerSizeConstraint.getArbitraryElementSize();
			}

			while (currentIndex < elementSize && iterator.hasNext()) {
				Object nextObject = iterator.next();
				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(elementType)
					.value(nextObject)
					.fieldName(fieldName)
					.indexOfIterable(currentIndex)
					.build();
				generatedNodeList.add(nextNode);
				currentIndex++;
			}

			if (containerSizeConstraint == null) {
				// value exists, container size size is same as value size.
				nowNode.setContainerSizeConstraint(new ContainerSizeConstraint(currentIndex, currentIndex));
				return generatedNodeList;
			}
		}

		nowNode.initializeElementSize();
		if (isNotInitialized(elementSize)) {
			// value does not exist.
			elementSize = nowNode.getContainerSizeConstraint().getArbitraryElementSize();
		}

		for (int i = currentIndex; i < elementSize; i++) {
			ArbitraryNode<?> nextNode = ArbitraryNode.builder()
				.type(elementType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();
			generatedNodeList.add(nextNode);
		}

		return generatedNodeList;
	}

	@SuppressWarnings("unchecked")
	private <T, U> Iterator<U> getIterator(T value) {
		if (value instanceof Collection) {
			return ((Collection<U>)value).iterator();
		} else if (value instanceof Stream) {
			return ((Stream<U>)value).iterator();
		} else {
			return (Iterator<U>)value;
		}
	}

	private boolean isNotInitialized(int elementSize) {
		return elementSize == Integer.MAX_VALUE;
	}
}
