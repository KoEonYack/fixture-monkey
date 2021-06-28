package com.navercorp.fixturemonkey.arbitrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public final class ArbitraryTree<T> {
	private final Supplier<ArbitraryNode<T>> headSupplier;
	private ArbitraryNode<T> head;

	public ArbitraryTree(Supplier<ArbitraryNode<T>> headSupplier) {
		this.headSupplier = headSupplier;
	}

	@SuppressWarnings("rawtypes")
	public Collection<ArbitraryNode> findAll(ArbitraryExpression arbitraryExpression) {
		Queue<ArbitraryNode> selectNodes = new LinkedList<>();
		selectNodes.add(getHead());

		List<ArbitraryNode> nextNodes = new ArrayList<>();

		CursorHolder cursorHolder = new CursorHolder(arbitraryExpression);
		for (Cursor cursor : cursorHolder.getCursors()) {
			while (!selectNodes.isEmpty()) {
				ArbitraryNode<?> selectNode = selectNodes.poll();

				nextNodes.addAll(selectNode.findChildrenByCursor(cursor));
			}
			selectNodes.addAll(nextNodes);
			nextNodes.clear();
		}
		return selectNodes;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <U> ArbitraryNode<U> findFirst(ArbitraryExpression arbitraryExpression) {
		ArbitraryNode<?> selectNode = getHead();

		CursorHolder cursorHolder = new CursorHolder(arbitraryExpression);
		List<Cursor> cursors = cursorHolder.getCursors();
		for (Cursor cursor : cursors) {
			selectNode = (ArbitraryNode<?>)selectNode.findChild(cursor).orElse(null);
			if (selectNode == null) { // 상위 필드가 생성 안됐을 경우
				break;
			}
		}

		return (ArbitraryNode<U>)selectNode;
	}

	public void update(ArbitraryGenerator generator) {
		getHead().update(getHead(), generator);
	}

	public ArbitraryNode<T> getHead() {
		if (head == null) {
			head = headSupplier.get();
		}
		return head;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Arbitrary<T> result(
		Supplier<Arbitrary<T>> generateArbitrary,
		ArbitraryValidator<T> validator,
		boolean validOnly
	) {
		return new ArbitraryValue(generateArbitrary, validator, validOnly);
	}

	public ArbitraryTree<T> copy() {
		return new ArbitraryTree<>(() -> this.getHead().copy());
	}
}
