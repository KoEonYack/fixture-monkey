package com.navercorp.fixturemonkey.arbitrary;

import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

@SuppressWarnings("unchecked")
public class MapArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final MapArbitraryNodeGenerator INSTANCE = new MapArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		LazyValue<T> lazyValue = nowNode.getValue();
		if (lazyValue != null) {
			nowNode.setArbitrary(Arbitraries.just(lazyValue.get()));
			return generatedNodeList;
		}

		ArbitraryType<T> clazz = nowNode.getType();
		String fieldName = nowNode.getFieldName();

		ArbitraryType<?> keyType = clazz.getGenericFixtureType(0);
		ArbitraryType<?> valueType = clazz.getGenericFixtureType(1);

		nowNode.initializeElementSize();

		int elementSize = nowNode.getContainerSizeConstraint().getArbitraryElementSize();

		if (clazz.isMapEntry()) {
			elementSize = 1;
		}

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<?> keyNode = ArbitraryNode.builder()
				.type(keyType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.keyOfMapStructure(true)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(keyNode);

			ArbitraryNode<?> valueNode = ArbitraryNode.builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(valueNode);
		}
		return generatedNodeList;
	}
}
