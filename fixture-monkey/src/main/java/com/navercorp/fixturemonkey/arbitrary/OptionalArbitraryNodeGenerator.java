package com.navercorp.fixturemonkey.arbitrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class OptionalArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final OptionalArbitraryNodeGenerator INSTANCE = new OptionalArbitraryNodeGenerator();

	@Override
	public <T, U> List<ArbitraryNode<U>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
		List<ArbitraryNode<U>> generatedNodeList = new ArrayList<>();

		ArbitraryType<T> arbitraryType = nowNode.getType();
		ArbitraryType<U> elementType = arbitraryType.getGenericFixtureType(0);
		String fieldName = nowNode.getFieldName();

		LazyValue<U> nextLazyValue = getNextLazyValue(nowNode.getValue());

		if (nextLazyValue != null && nextLazyValue.isEmpty()) {
			// can not generate Optional empty by ArbitraryGenerator
			return generatedNodeList;
		}

		ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
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
