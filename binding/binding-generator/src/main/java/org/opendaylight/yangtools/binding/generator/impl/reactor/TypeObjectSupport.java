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
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

/**
 * Support for {@link TypeObject} specializations.
 */
abstract sealed class TypeObjectSupport permits TypeObjectSupport.Base, TypeObjectSupport.Derived {

    static abstract sealed class Base extends TypeObjectSupport {
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

    static final class Union extends Base {
        private Union(final TypeEffectiveStatement type) {
            super(type);
        }
    }

    static final class BuiltIn extends Base {
        final @NonNull ConcreteType javaType;

        private BuiltIn(final TypeEffectiveStatement type, final ConcreteType javaType) {
            super(type);
            this.javaType = requireNonNull(javaType);
        }
    }

    final @NonNull TypeEffectiveStatement type;

    TypeObjectSupport(final TypeEffectiveStatement type) {
        this.type = requireNonNull(type);
    }

    static @NonNull TypeObjectSupport of(final @NonNull TypeEffectiveStatement type) {
        final var typeName = type.argument();
        if (!YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule())) {
            return new Derived(type);
        }

        final var simpleName = typeName.getLocalName();
        return switch (simpleName) {
            case "binary" -> new BuiltIn(type, BaseYangTypes.BINARY_TYPE);
            case "bits" -> new Bits(type);
            case "boolean" -> new BuiltIn(type, BaseYangTypes.BOOLEAN_TYPE);
            case "decimal64" -> new BuiltIn(type,
                Decimal64Type.ofFractionDigits(((DecimalTypeDefinition) type.typeDefinition()).getFractionDigits()));
            case "empty" -> new BuiltIn(type, BaseYangTypes.EMPTY_TYPE);
            case "enumeration" -> new Enumeration(type);
            case "identityref" -> new Identityref(type);
            case "int8" -> new BuiltIn(type, BaseYangTypes.INT8_TYPE);
            case "int16" -> new BuiltIn(type, BaseYangTypes.INT16_TYPE);
            case "int32" -> new BuiltIn(type, BaseYangTypes.INT32_TYPE);
            case "int64" -> new BuiltIn(type, BaseYangTypes.INT64_TYPE);
            case "string" -> new BuiltIn(type, BaseYangTypes.STRING_TYPE);
            case "union" -> new Union(type);
            case "leafref" -> new Leafref(type);
            case "instance-identifier" -> new BuiltIn(type, BaseYangTypes.INSTANCE_IDENTIFIER);
            case "uint8" -> new BuiltIn(type, BaseYangTypes.UINT8_TYPE);
            case "uint16" -> new BuiltIn(type, BaseYangTypes.UINT16_TYPE);
            case "uint32" -> new BuiltIn(type, BaseYangTypes.UINT32_TYPE);
            case "uint64" -> new BuiltIn(type, BaseYangTypes.UINT64_TYPE);
            default -> throw new VerifyException("Unhandled type " + typeName);
        };
    }
}
