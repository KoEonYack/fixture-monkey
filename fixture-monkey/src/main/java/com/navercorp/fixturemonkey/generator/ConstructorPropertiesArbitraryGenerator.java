package com.navercorp.fixturemonkey.generator;

import static java.util.stream.Collectors.toList;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;

public final class ConstructorPropertiesArbitraryGenerator extends AbstractArbitraryGenerator
	implements WithFixtureCustomizer {
	public static final ConstructorPropertiesArbitraryGenerator INSTANCE =
		new ConstructorPropertiesArbitraryGenerator();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ArbitraryCustomizers arbitraryCustomizers;

	public ConstructorPropertiesArbitraryGenerator() {
		this(new ArbitraryCustomizers());
	}

	private ConstructorPropertiesArbitraryGenerator(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		Class<T> clazz = type.getType();

		List<Constructor<?>> constructors = Arrays.stream(clazz.getConstructors())
			.filter(c -> c.isAnnotationPresent(ConstructorProperties.class))
			.collect(toList());

		if (constructors.isEmpty()) {
			throw new IllegalArgumentException(
				clazz + " doesn't have constructor with 'ConstructorProperties' annotation.");
		}

		if (constructors.size() > 1) {
			throw new IllegalArgumentException(
				clazz + " has more then one constructor with 'ConstructorProperties' annotation.");
		}

		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, ArbitraryNode::getFieldName, (node, arbitrary) -> arbitrary)
		);

		this.arbitraryCustomizers.customizeFields(clazz, fieldArbitraries);

		Constructor<T> constructor = (Constructor<T>)constructors.get(0);

		ConstructorProperties constructorProperties = constructor.getAnnotation(ConstructorProperties.class);
		String[] providedParameterNames = constructorProperties.value();
		Parameter[] actualParameters = constructor.getParameters();

		Combinators.BuilderCombinator<List<Object>> builderCombinator = Combinators.withBuilder(
			() -> new ArrayList(providedParameterNames.length));
		for (int i = 0; i < providedParameterNames.length; ++i) {
			String fieldName = providedParameterNames[i];
			Arbitrary<?> arbitrary = fieldArbitraries.getArbitrary(fieldName);
			if (arbitrary == null) {
				Parameter actualParameter = actualParameters[i];
				Class<?> actualParameterType = actualParameter.getType();
				arbitrary = Arbitraries.defaultFor(actualParameterType);
			}

			builderCombinator = builderCombinator.use(arbitrary).in((list, value) -> {
				list.add(value);
				return list;
			});
		}

		return builderCombinator.build(list -> {
			T fixture = ReflectionUtils.newInstance(constructor, list.toArray());
			return this.arbitraryCustomizers.customizeFixture(clazz, fixture);
		});
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}
		return new ConstructorPropertiesArbitraryGenerator(arbitraryCustomizers);
	}
}
