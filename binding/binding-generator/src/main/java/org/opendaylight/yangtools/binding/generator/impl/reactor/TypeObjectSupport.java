/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.TypeDefinitionCompat.WithQNameArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Support for {@link TypeObject} specializations.
 */
@NonNullByDefault
abstract sealed class TypeObjectSupport permits TypeObjectSupport.Base, TypeObjectSupport.Derived {

    abstract static sealed class Base extends TypeObjectSupport {
        Base(final TypeEffectiveStatement type) {
            super(type);
        }
    }

    static final class Derived extends TypeObjectSupport {
        Derived(final TypeEffectiveStatement type) {
            super(type);
        }
    }

    static final class Bits extends Base {
        private Bits(final TypeEffectiveStatement type) {
            super(type);
        }

        BitsTypeObjectArchetype toArchetype(final AbstractTypeObjectGenerator<?, ?> gen,
                final TypeBuilderFactory builderFactory) {
            final var statement = gen.statement();
            return createBits(statement, gen.typeName(), gen.currentModule(),
                (BitsTypeDefinition) statement.typeDefinition(), builderFactory,
                statement instanceof TypedefEffectiveStatement);
        }

        static BitsTypeObjectArchetype createBits(final WithQNameArgument<?> definingStatement,
                final JavaTypeName typeName, final ModuleGenerator module, final BitsTypeDefinition typedef,
                final TypeBuilderFactory builderFactory,
                // FIXME: why do we need this boolean?
                final boolean isTypedef) {
            final var builder = builderFactory.newBitsTypeObjectBuilder(typeName);
            builder.setTypedef(isTypedef);
            builder.addImplementsType(BindingTypes.BITS_TYPE_OBJECT);
            builder.setBaseType(typedef);
            YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);

            for (var bit : typedef.getBits()) {
                final String name = bit.getName();
                var genPropertyBuilder = builder.addProperty(Naming.getPropertyName(name));
                genPropertyBuilder.setReadOnly(true);
                genPropertyBuilder.setReturnType(Types.primitiveBooleanType());
            }
            builder.addConstant(Types.immutableSetTypeFor(Types.STRING), TypeConstants.VALID_NAMES_NAME, typedef);

            builder.setModuleName(module.statement().argument().getLocalName());
            builderFactory.addCodegenInformation(typedef, builder);
            AbstractTypeObjectGenerator.annotateDeprecatedIfNecessary(typedef, builder);
            AbstractTypeObjectGenerator.makeSerializable(builder);
            return builder.build();
        }
    }

    static final class Enumeration extends Base {
        private Enumeration(final TypeEffectiveStatement type) {
            super(type);
        }

        EnumTypeObjectArchetype toArchetype(final AbstractTypeObjectGenerator<?, ?> gen,
                final TypeBuilderFactory builderFactory) {
            final var statement = gen.statement();
            return createEnumeration(builderFactory, statement, gen.typeName(), gen.currentModule(),
                (EnumTypeDefinition) statement.typeDefinition());

        }

        static EnumTypeObjectArchetype createEnumeration(final TypeBuilderFactory builderFactory,
                final WithQNameArgument<?> definingStatement, final JavaTypeName typeName, final ModuleGenerator module,
                final EnumTypeDefinition typedef) {
            // TODO units for typedef enum
            final var builder = builderFactory.newEnumTypeObjectBuilder(typeName);
            YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);

            typedef.getDescription().map(DocUtils::encodeAngleBrackets).ifPresent(builder::setDescription);
            typedef.getReference().ifPresent(builder::setReference);

            builder.setModuleName(module.statement().argument().getLocalName());
            builder.updateEnumPairsFromEnumTypeDef(typedef);
            return builder.build();
        }
    }

    static final class Identityref extends Base {
        private Identityref(final TypeEffectiveStatement type) {
            super(type);
        }

        Stream<QName> baseIdentities() {
            return type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                .map(BaseEffectiveStatement::argument);
        }
    }

    static final class Leafref extends Base {
        private Leafref(final TypeEffectiveStatement type) {
            super(type);
        }

        PathExpression path() {
            return type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow();
        }
    }

    static final class Scalar extends Base {
        final ConcreteType javaType;

        private Scalar(final TypeEffectiveStatement type, final ConcreteType javaType) {
            super(type);
            this.javaType = requireNonNull(javaType);
        }

        private Scalar(final TypeEffectiveStatement type, final DecimalTypeDefinition def) {
            super(type);
            javaType = Decimal64Type.ofFractionDigits(def.getFractionDigits());
        }

        ScalarTypeObjectArchetype toArchetype(final AbstractTypeObjectGenerator<?, ?> gen,
                final TypeBuilderFactory builderFactory) {
            final var statement = gen.statement();

            return createScalar(builderFactory, statement, gen.typeName(), gen.currentModule(), javaType,
                statement.typeDefinition());
        }

        static ScalarTypeObjectArchetype createScalar(final TypeBuilderFactory builderFactory,
                final WithQNameArgument<?> definingStatement, final JavaTypeName typeName, final ModuleGenerator module,
                final Type javaType, final TypeDefinition<?> typedef) {
            final var builder = builderFactory.newScalarTypeObjectBuilder(typeName);
            builder.setTypedef(true);
            builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));
            YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);

            final var genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
            genPropBuilder.setReturnType(javaType);
            builder.setRestrictions(AbstractTypeObjectGenerator.getRestrictions(typedef));
            builder.setModuleName(module.statement().argument().getLocalName());
            builderFactory.addCodegenInformation(typedef, builder);

            AbstractTypeObjectGenerator.annotateDeprecatedIfNecessary(typedef, builder);

            if (javaType instanceof ConcreteType
                // FIXME: This looks very suspicious: we should by checking for Types.STRING
                && "String".equals(javaType.simpleName()) && typedef.getBaseType() != null) {
                AbstractTypeObjectGenerator.addStringRegExAsConstant(builder,
                    AbstractTypeObjectGenerator.resolveRegExpressions(typedef));
            }
            AbstractTypeObjectGenerator.addUnits(builder, typedef);

            AbstractTypeObjectGenerator.makeSerializable(builder);
            return builder.build();
        }
    }

    static final class Union extends Base {
        static final class Dependencies implements Immutable {
            private final HashMap<EffectiveStatement<?, ?>, TypeReference> identityTypes = new HashMap<>();
            private final HashMap<EffectiveStatement<?, ?>, TypeReference> leafTypes = new HashMap<>();
            private final HashMap<QName, TypedefGenerator> baseTypes = new HashMap<>();

            Dependencies(final TypeEffectiveStatement type, final GeneratorContext context) {
                resolveUnionDependencies(context, type);
            }

            private void resolveUnionDependencies(final GeneratorContext context, final TypeEffectiveStatement union) {
                for (var stmt : union.effectiveSubstatements()) {
                    if (stmt instanceof TypeEffectiveStatement type) {
                        final QName typeName = type.argument();
                        if (BuiltInType.IDENTITYREF.typeName().equals(typeName)) {
                            if (!identityTypes.containsKey(stmt)) {
                                identityTypes.put(stmt, TypeReference.identityRef(
                                    type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                                        .map(BaseEffectiveStatement::argument)
                                        .map(context::resolveIdentity)
                                        .collect(Collectors.toUnmodifiableList())));
                            }
                        } else if (BuiltInType.LEAFREF.typeName().equals(typeName)) {
                            if (!leafTypes.containsKey(stmt)) {
                                leafTypes.put(stmt, TypeReference.leafRef(context.resolveLeafref(
                                    type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class)
                                    .orElseThrow())));
                            }
                        } else if (BuiltInType.UNION.typeName().equals(typeName)) {
                            resolveUnionDependencies(context, type);
                        } else if (!AbstractTypeObjectGenerator.isBuiltinName(typeName)
                                   && !baseTypes.containsKey(typeName)) {
                            baseTypes.put(typeName, context.resolveTypedef(typeName));
                        }
                    }
                }
            }
        }

        // FIXME: remove this map
        private static final ImmutableMap<QName, ConcreteType> SIMPLE_TYPES = ImmutableMap.<QName, ConcreteType>builder()
            .put(BuiltInType.BINARY.typeName(), BaseYangTypes.BINARY_TYPE)
            .put(BuiltInType.BOOLEAN.typeName(), BaseYangTypes.BOOLEAN_TYPE)
            .put(BuiltInType.EMPTY.typeName(), BaseYangTypes.EMPTY_TYPE)
            .put(BuiltInType.INSTANCE_IDENTIFIER.typeName(), BaseYangTypes.INSTANCE_IDENTIFIER)
            .put(BuiltInType.INT8.typeName(), BaseYangTypes.INT8_TYPE)
            .put(BuiltInType.INT16.typeName(), BaseYangTypes.INT16_TYPE)
            .put(BuiltInType.INT32.typeName(), BaseYangTypes.INT32_TYPE)
            .put(BuiltInType.INT64.typeName(), BaseYangTypes.INT64_TYPE)
            .put(BuiltInType.STRING.typeName(), BaseYangTypes.STRING_TYPE)
            .put(BuiltInType.UINT8.typeName(), BaseYangTypes.UINT8_TYPE)
            .put(BuiltInType.UINT16.typeName(), BaseYangTypes.UINT16_TYPE)
            .put(BuiltInType.UINT32.typeName(), BaseYangTypes.UINT32_TYPE)
            .put(BuiltInType.UINT64.typeName(), BaseYangTypes.UINT64_TYPE)
            .build();

        private Union(final TypeEffectiveStatement type) {
            super(type);
        }

        Map.Entry<UnionTypeObjectArchetype, List<GeneratedType>> toArchetype(
                final AbstractTypeObjectGenerator<?, ?> gen, final Dependencies deoendencies,
                final TypeBuilderFactory builderFactory) {
            final var statement = gen.statement();
            final var tmp = new ArrayList<GeneratedType>(1);
            return Map.entry(createUnion(tmp, builderFactory, statement, deoendencies, gen.typeName(),
                gen.currentModule(), type, statement instanceof TypedefEffectiveStatement, statement.typeDefinition()),
                tmp);
        }

        static UnionTypeObjectArchetype createUnion(final List<GeneratedType> auxiliaryGeneratedTypes,
                final TypeBuilderFactory builderFactory, final WithQNameArgument<?> definingStatement,
                final Dependencies dependencies, final JavaTypeName typeName, final ModuleGenerator module,
                final TypeEffectiveStatement type,
                // FIXME: why do we need this boolean?
                final boolean isTypedef,
                final TypeDefinition<?> typedef) {
            final var builder = builderFactory.newUnionTypeObjectBuilder(typeName);
            YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);
            builder.addImplementsType(BindingTypes.UNION_TYPE_OBJECT);
            builder.setModuleName(module.statement().argument().getLocalName());
            builderFactory.addCodegenInformation(definingStatement, builder);

            AbstractTypeObjectGenerator.annotateDeprecatedIfNecessary(definingStatement, builder);

            // Pattern string is the key, XSD regex is the value. The reason for this choice is that the pattern carries
            // also negation information and hence guarantees uniqueness.
            final var expressions = new HashMap<String, String>();

            // Linear list of properties generated from subtypes. We need this information for runtime types, as it allows
            // direct mapping of type to corresponding property -- without having to resort to re-resolving the leafrefs
            // again.
            final var typeProperties = new ArrayList<String>();

            for (var stmt : type.effectiveSubstatements()) {
                if (stmt instanceof TypeEffectiveStatement subType) {
                    final QName subName = subType.argument();
                    final String localName = subName.getLocalName();

                    String propSource = localName;
                    final Type generatedType;
                    if (BuiltInType.UNION.typeName().equals(subName)) {
                        final var subUnionName = typeName.createEnclosed(
                            AbstractTypeObjectGenerator.provideAvailableNameForGenTOBuilder(typeName.simpleName()));
                        final var subUnion = createUnion(auxiliaryGeneratedTypes, builderFactory, definingStatement,
                            dependencies, subUnionName, module, subType, isTypedef, subType.typeDefinition());
                        builder.addEnclosingTransferObject(subUnion);
                        propSource = subUnionName.simpleName();
                        generatedType = subUnion;
                    } else if (BuiltInType.ENUMERATION.typeName().equals(subName)) {
                        final var subEnumeration = TypeObjectSupport.Enumeration.createEnumeration(builderFactory,
                            definingStatement, typeName.createEnclosed(Naming.getClassName(localName), "$"), module,
                            (EnumTypeDefinition) subType.typeDefinition());
                        builder.addEnumeration(subEnumeration);
                        generatedType = subEnumeration;
                    } else if (BuiltInType.BITS.typeName().equals(subName)) {
                        final var subBits = TypeObjectSupport.Bits.createBits(definingStatement,
                            typeName.createEnclosed(Naming.getClassName(localName), "$"), module,
                            (BitsTypeDefinition) subType.typeDefinition(), builderFactory, isTypedef);
                        builder.addEnclosingTransferObject(subBits);
                        generatedType = subBits;
                    } else if (BuiltInType.IDENTITYREF.typeName().equals(subName)) {
                        propSource = stmt.findFirstEffectiveSubstatement(BaseEffectiveStatement.class)
                            .orElseThrow(() -> new VerifyException(String.format("Invalid identityref "
                                + "definition %s in %s, missing BASE statement", stmt, definingStatement)))
                            .argument().getLocalName();
                        generatedType = verifyNotNull(dependencies.identityTypes.get(stmt),
                            "Cannot resolve identityref %s in %s", stmt, definingStatement)
                            .methodReturnType(builderFactory);
                    } else if (BuiltInType.LEAFREF.typeName().equals(subName)) {
                        generatedType = verifyNotNull(dependencies.leafTypes.get(stmt),
                            "Cannot resolve leafref %s in %s", stmt, definingStatement)
                            .methodReturnType(builderFactory);
                    } else {
                        final var subDef = subType.typeDefinition();

                        Type baseType = SIMPLE_TYPES.get(subName);
                        if (baseType == null) {
                            if (!BuiltInType.DECIMAL64.typeName().equals(subName)) {
                                // This has to be a reference to a typedef, let's lookup it up and pick up its type
                                final AbstractTypeObjectGenerator<?, ?> baseGen = verifyNotNull(
                                    dependencies.baseTypes.get(subName), "Cannot resolve base type %s in %s", subName,
                                    definingStatement);
                                baseType = baseGen.methodReturnType(builderFactory);

                                // FIXME: This is legacy behaviour for leafrefs:
                                if (baseGen.refType instanceof TypeReference.Leafref) {
                                    // if there already is a compatible property, do not generate a new one
                                    final Type search = baseType;

                                    final String matching = builder.getProperties().stream()
                                        .filter(prop -> search == ((GeneratedPropertyBuilderImpl) prop).getReturnType())
                                        .findFirst()
                                        .map(GeneratedPropertyBuilder::getName)
                                        .orElse(null);
                                    if (matching != null) {
                                        typeProperties.add(matching);
                                        continue;
                                    }

                                    // ... otherwise generate this weird property name
                                    propSource = getUnionLeafrefMemberName(builder.typeName().simpleName(),
                                        baseType.simpleName());
                                }
                            } else {
                                baseType = Decimal64Type.ofFractionDigits(
                                    ((DecimalTypeDefinition) subDef).getFractionDigits());
                            }
                        }

                        expressions.putAll(AbstractTypeObjectGenerator.resolveRegExpressions(subDef));

                        generatedType = AbstractTypeObjectGenerator.restrictType(baseType,
                            AbstractTypeObjectGenerator.getRestrictions(type.typeDefinition()), builderFactory);
                    }

                    final String propName = Naming.getPropertyName(propSource);
                    typeProperties.add(propName);

                    if (builder.containsProperty(propName)) {
                        /*
                         *  FIXME: this is not okay, as we are ignoring multiple base types. For example in the case of:
                         *
                         *    type union {
                         *      type string {
                         *        length 1..5;
                         *      }
                         *      type string {
                         *        length 8..10;
                         *      }
                         *    }
                         *
                         *  We are ending up losing the information about 8..10 being an alternative. This is also the case
                         *  for leafrefs -- we are performing property compression as well (see above). While it is alluring
                         *  to merge these into 'length 1..5|8..10', that may not be generally feasible.
                         *
                         *  We should resort to a counter of conflicting names, i.e. the second string would be mapped to
                         *  'string1' or similar.
                         */
                        continue;
                    }

                    builder.addProperty(propName).setReturnType(generatedType);
                }
            }

            // Record property names if needed
            builder.setTypePropertyNames(typeProperties);

            AbstractTypeObjectGenerator.addStringRegExAsConstant(builder, expressions);
            AbstractTypeObjectGenerator.addUnits(builder, typedef);

            AbstractTypeObjectGenerator.makeSerializable(builder);
            return builder.build();
        }

        // FIXME: this is legacy union/leafref property handling. The resulting value is *not* normalized for use as a
        //        property.
        private static String getUnionLeafrefMemberName(final String unionClassSimpleName,
                final String referencedClassSimpleName) {
            return requireNonNull(referencedClassSimpleName) + requireNonNull(unionClassSimpleName) + "Value";
        }
    }

    final TypeEffectiveStatement type;

    TypeObjectSupport(final TypeEffectiveStatement type) {
        this.type = requireNonNull(type);
    }

    static TypeObjectSupport of(final TypeEffectiveStatement type) {
        final var typeName = type.argument();
        if (!YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule())) {
            return new Derived(type);
        }

        final var simpleName = typeName.getLocalName();
        return switch (simpleName) {
            case "binary" -> new Scalar(type, BaseYangTypes.BINARY_TYPE);
            case "bits" -> new Bits(type);
            case "boolean" -> new Scalar(type, BaseYangTypes.BOOLEAN_TYPE);
            case "decimal64" -> new Scalar(type, (DecimalTypeDefinition) type.typeDefinition());
            case "empty" -> new Scalar(type, BaseYangTypes.EMPTY_TYPE);
            case "enumeration" -> new Enumeration(type);
            case "identityref" -> new Identityref(type);
            case "int8" -> new Scalar(type, BaseYangTypes.INT8_TYPE);
            case "int16" -> new Scalar(type, BaseYangTypes.INT16_TYPE);
            case "int32" -> new Scalar(type, BaseYangTypes.INT32_TYPE);
            case "int64" -> new Scalar(type, BaseYangTypes.INT64_TYPE);
            case "string" -> new Scalar(type, BaseYangTypes.STRING_TYPE);
            case "union" -> new Union(type);
            case "leafref" -> new Leafref(type);
            case "instance-identifier" -> new Scalar(type, BaseYangTypes.INSTANCE_IDENTIFIER);
            case "uint8" -> new Scalar(type, BaseYangTypes.UINT8_TYPE);
            case "uint16" -> new Scalar(type, BaseYangTypes.UINT16_TYPE);
            case "uint32" -> new Scalar(type, BaseYangTypes.UINT32_TYPE);
            case "uint64" -> new Scalar(type, BaseYangTypes.UINT64_TYPE);
            default -> throw new VerifyException("Unhandled type " + typeName);
        };
    }
}
