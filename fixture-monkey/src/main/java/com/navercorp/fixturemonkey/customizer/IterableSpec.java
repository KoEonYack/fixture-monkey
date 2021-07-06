package com.navercorp.fixturemonkey.customizer;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IterableSpec {
	IterableSpec ofMinSize(int size);

	IterableSpec ofMaxSize(int size);

	IterableSpec ofSize(int size);

	IterableSpec ofSizeBetween(int min, int max);

	IterableSpec ofNotNull();

	IterableSpec setElement(long fieldIndex, Object object);

	<T> IterableSpec setElementPostCondition(long fieldIndex, Class<T> clazz, Predicate<T> postCondition);

	<T> IterableSpec setElementPostCondition(long fieldIndex, Class<T> clazz, Predicate<T> postCondition, long limit);

	IterableSpec listElement(long fieldIndex, Consumer<IterableSpec> spec);

	IterableSpec listFieldElement(long fieldIndex, String fieldName, Consumer<IterableSpec> spec);

	IterableSpec setElementField(long fieldIndex, String fieldName, Object object);

	<T> IterableSpec setElementFieldPostCondition(
		long fieldIndex,
		String fieldName,
		Class<T> clazz,
		Predicate<T> postCondition
	);

	<T> IterableSpec setElementFieldPostCondition(
		long fieldIndex,
		String fieldName,
		Class<T> clazz,
		Predicate<T> postCondition,
		long limit
	);

	IterableSpec any(Object object);

	IterableSpec all(Object object);
}
