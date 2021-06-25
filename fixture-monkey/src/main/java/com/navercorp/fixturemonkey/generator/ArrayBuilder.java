package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Combinators.BuilderCombinator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class ArrayBuilder {
	public static ArrayBuilder INSTANCE = new ArrayBuilder();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> Arbitrary<T> build(Class<T> clazz, List<ArbitraryNode> nodes) {
		BuilderCombinator<ArrayBuilderFrame> builder =
			Combinators.withBuilder(() -> new ArrayBuilderFrame(clazz, nodes.size()));

		if (nodes.isEmpty()) {
			return (Arbitrary<T>)builder.build(ArrayBuilderFrame::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			builder = builder.use(node.getArbitrary()).in(ArrayBuilderFrame::add);
		}

		return (Arbitrary<T>)builder.build(ArrayBuilderFrame::build);
	}

	private static final class ArrayBuilderFrame {
		private final List<Object> array;
		private final Class<?> componentType;
		private final int size;

		public ArrayBuilderFrame(Class<?> componentType, int size) {
			this.array = new ArrayList<>();
			this.componentType = componentType;
			this.size = size;
		}

		ArrayBuilderFrame add(Object value) {
			if (array.size() >= size) {
				return this;
			}

			array.add(value);
			return this;
		}

		// primitive 타입일 때, ClassCastException 이 발생하기 때문에 Object 로 반환한다.
		Object build() {
			Object array = Array.newInstance(componentType, size);
			for (int i = 0; i < this.array.size(); i++) {
				Array.set(array, i, this.array.get(i));
			}

			return array;
		}
	}
}
