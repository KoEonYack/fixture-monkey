package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.ALL_INDEX_STRING;
import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.Objects;

abstract class Cursor {
	private final String name;
	private final int index;

	public Cursor(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public boolean indexEquals(int index) {
		return this.index == index
			|| index == NO_OR_ALL_INDEX_INTEGER_VALUE
			|| this.index == NO_OR_ALL_INDEX_INTEGER_VALUE;
	}

	public boolean nameEquals(String name) {
		return this.name.equals(name)
			|| name.equals(ALL_INDEX_STRING)
			|| this.name.equals(ALL_INDEX_STRING);
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Cursor)) {
			return false;
		}
		Cursor cursor = (Cursor)obj;

		boolean indexEqual = indexEquals(cursor.getIndex());
		boolean nameEqual = nameEquals(cursor.getName());
		return nameEqual && indexEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
