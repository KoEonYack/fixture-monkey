package com.navercorp.fixturemonkey.arbitrary;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class NullArbitraryType extends ArbitraryType {
	public static final NullArbitraryType INSTANCE = new NullArbitraryType();

	private NullArbitraryType() {
		super(null);
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public Annotation getAnnotation(Class annotationType) {
		return null;
	}

	@Override
	public ArbitraryType getGenericFixtureType(int index) {
		throw new IllegalArgumentException("Null ArbitraryType can not have generics");
	}

	@Override
	public ArbitraryType getArrayFixtureType() {
		throw new IllegalArgumentException("Null ArbitraryType can not be array");
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isMapEntry() {
		return false;
	}

	@Override
	public boolean isMap() {
		return false;
	}

	@Override
	public boolean isGenericType() {
		return false;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isStream() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public Class<?> getType() {
		return null;
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return null;
	}
}
