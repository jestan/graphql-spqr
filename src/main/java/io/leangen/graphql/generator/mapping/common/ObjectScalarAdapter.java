package io.leangen.graphql.generator.mapping.common;

import graphql.schema.GraphQLScalarType;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.annotations.GraphQLScalar;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.BuildContext;
import io.leangen.graphql.generator.OperationMapper;
import io.leangen.graphql.generator.mapping.OutputConverter;
import io.leangen.graphql.metadata.strategy.value.ScalarDeserializationStrategy;
import io.leangen.graphql.util.Scalars;

import java.lang.reflect.AnnotatedType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Bojan Tomic (kaqqao)
 */
public class ObjectScalarAdapter extends CachingMapper<GraphQLScalarType, GraphQLScalarType> implements OutputConverter<Object, Object> {

    private final ScalarDeserializationStrategy scalarStrategy;

    private static final AnnotatedType MAP = GenericTypeReflector.annotate(ScalarMap.class);

    public ObjectScalarAdapter(ScalarDeserializationStrategy scalarStrategy) {
        this.scalarStrategy = Objects.requireNonNull(scalarStrategy);
    }

    @Override
    public GraphQLScalarType toGraphQLType(String typeName, AnnotatedType javaType, OperationMapper operationMapper, BuildContext buildContext) {
        return Scalars.graphQLObjectScalar(typeName);
    }

    @Override
    public GraphQLScalarType toGraphQLInputType(String typeName, AnnotatedType javaType, OperationMapper operationMapper, BuildContext buildContext) {
        return toGraphQLType(typeName, javaType, operationMapper, buildContext);
    }

    @Override
    public Object convertOutput(Object original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        if (original == null || original instanceof String || original instanceof Number || original instanceof Boolean) {
            return original;
        } else {
            return resolutionEnvironment.valueMapper.fromInput(original, type.getType(), MAP);
        }
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return type.isAnnotationPresent(GraphQLScalar.class)
                || Object.class.equals(type.getType())
                || GenericTypeReflector.isSuperType(Map.class, type.getType())
                || scalarStrategy.isDirectlyDeserializable(type);
    }

    public static class ScalarMap extends LinkedHashMap {}
}
