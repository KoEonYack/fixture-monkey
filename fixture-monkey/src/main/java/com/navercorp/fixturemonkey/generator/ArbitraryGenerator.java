package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.Field;
import java.util.List;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;

@FunctionalInterface
public interface ArbitraryGenerator extends FieldNameResolver {
	@SuppressWarnings("rawtypes")
	<T> Arbitrary<T> generate(ArbitraryType type, List<ArbitraryNode> nodes);

	default String resolveFieldName(Field field) {
		return field.getName();
	}
}
