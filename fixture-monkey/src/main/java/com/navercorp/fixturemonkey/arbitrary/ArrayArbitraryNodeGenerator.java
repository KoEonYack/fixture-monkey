package com.navercorp.fixturemonkey.arbitrary;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class ArrayArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final ArrayArbitraryNodeGenerator INSTANCE = new ArrayArbitraryNodeGenerator();

	@SuppressWarnings("unchecked")
	@Override
	public <T, U> List<ArbitraryNode<U>> generate(
		ArbitraryNode<T> nowNode,
		FieldNameResolver fieldNameResolver
	) {
		int elementSize = Integer.MAX_VALUE;
		int currentIndex = 0;

		ArbitraryType<T> clazz = nowNode.getType();
		ArbitraryType<U> childType = clazz.getArrayFixtureType();
		String fieldName = nowNode.getFieldName();
		LazyValue<T> lazyValue = nowNode.getValue();

		List<ArbitraryNode<U>> generatedNodeList = new ArrayList<>();

		if (lazyValue != null) {
			T value = lazyValue.get();
			ContainerSizeConstraint containerSizeConstraint = nowNode.getContainerSizeConstraint();

			if (value == null) {
				nowNode.setArbitrary(Arbitraries.just(null));
				return generatedNodeList;
			}
			int length = Array.getLength(value);

			if (containerSizeConstraint != null) {
				// container size is set.
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
				generatedNodeList.add(nextNode);
			}

			if (containerSizeConstraint == null) {
				// value exists, container size size is same as value size.
				nowNode.setContainerSizeConstraint(new ContainerSizeConstraint(length, length));
				return generatedNodeList;
			}
		}

		nowNode.initializeElementSize();
		if (isNotInitialized(elementSize)) {
			// value does not exist.
			elementSize = nowNode.getContainerSizeConstraint().getArbitraryElementSize();
		}

		for (int i = currentIndex; i < elementSize; i++) {
			ArbitraryNode<U> genericFrame = ArbitraryNode.<U>builder()
				.type(childType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(genericFrame);
		}
		return generatedNodeList;
	}

	private boolean isNotInitialized(int elementSize) {
		return elementSize == Integer.MAX_VALUE;
	}
}
