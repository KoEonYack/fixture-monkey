package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldNameResolver {
	String resolveFieldName(Field field);
}
