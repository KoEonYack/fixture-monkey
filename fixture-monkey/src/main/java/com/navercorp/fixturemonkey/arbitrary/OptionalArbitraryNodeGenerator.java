package com.navercorp.fixturemonkey.arbitrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class OptionalArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final OptionalArbitraryNodeGenerator INSTANCE = new OptionalArbitraryNodeGenerator();

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		ArbitraryType<T> arbitraryType = nowNode.getType();
		ArbitraryType<?> elementType = arbitraryType.getGenericFixtureType(0);
		String fieldName = nowNode.getFieldName();

		LazyValue<?> nextLazyValue = getNextLazyValue(nowNode.getValue());

		if (nextLazyValue != null && nextLazyValue.isEmpty()) {
			// can not generate Optional empty by ArbitraryGenerator
			return generatedNodeList;
		}

		ArbitraryNode<?> nextNode = ArbitraryNode.builder()
			.type(elementType)
			.value(nextLazyValue)
			.fieldName(fieldName)
			.indexOfIterable(0)
			.build();
		generatedNodeList.add(nextNode);
		return generatedNodeList;
	}

	@SuppressWarnings("unchecked")
	private <T, U> LazyValue<U> getNextLazyValue(LazyValue<T> lazyValue) {
		if (lazyValue == null) {
			return null;
		}

		T value = lazyValue.get();

		Optional<U> optional = ((Optional<U>)value);
		U nextObject = optional.orElse(null);
		return new LazyValue<>(() -> nextObject);
	}
}
