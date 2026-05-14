/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

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
    }

    static final class Enumeration extends Base {
        private Enumeration(final TypeEffectiveStatement type) {
            super(type);
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
            final var stmt = gen.statement();
            return new ScalarTypeObjectArchetype(gen.typeName(), stmt, stmt.typeDefinition(), javaType, null);
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

            TypedefGenerator basetypeOf(final QName typeName) {
                return baseTypes.get(requireNonNull(typeName));
            }

            TypeReference identityrefOf(final EffectiveStatement<?, ?> stmt) {
                return identityTypes.get(requireNonNull(stmt));
            }

            TypeReference leafrefOf(final EffectiveStatement<?, ?> stmt) {
                return leafTypes.get(requireNonNull(stmt));
            }
        }

        private Union(final TypeEffectiveStatement type) {
            super(type);
        }

        Map.Entry<UnionTypeObjectArchetype, List<GeneratedType>> toArchetype(
                final AbstractTypeObjectGenerator<?, ?> gen, final Dependencies dependencies,
                final TypeBuilderFactory builderFactory) {
            final var stmt = gen.statement();
            return TypeObjectCreator.createUnionTypeObjectArchetype(gen.typeName(), stmt,
                (UnionTypeDefinition) stmt.typeDefinition(), type, dependencies, builderFactory,
                gen.currentModule().statement());
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
