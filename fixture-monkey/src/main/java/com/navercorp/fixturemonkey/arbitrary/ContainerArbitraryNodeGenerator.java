package com.navercorp.fixturemonkey.arbitrary;

import java.util.List;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public interface ContainerArbitraryNodeGenerator {
	<T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver);
}
