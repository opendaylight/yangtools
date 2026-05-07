/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import java.io.Serializable;
import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;

/**
 * By type member {@link Comparator} which provides sorting by type for {@link GetterShape}s in a generated class.
 */
final class ByTypeMemberComparator implements Comparator<GetterShape>, Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Fixed-size comparison. These are all numeric types, boolean, empty, identityref.
     */
    private static final int RANK_FIXED_SIZE          = 0;
    /**
     * Variable-sized comparison across simple components. These are string, binary and bits type.
     */
    private static final int RANK_VARIABLE_ARRAY      = 1;
    /**
     * Variable-size comparison across complex components.
     */
    private static final int RANK_INSTANCE_IDENTIFIER = 2;
    /**
     * Composite structure. DataObject, OpaqueObject and similar.
     */
    private static final int RANK_COMPOSITE           = 3;

    /**
     * Singleton instance.
     */
    static final @NonNull ByTypeMemberComparator INSTANCE = new ByTypeMemberComparator();

    private ByTypeMemberComparator() {
        // Hidden on purpose
    }

    @Override
    public int compare(final GetterShape member1, final GetterShape member2) {
        final var type1 = getConcreteType(member1.type());
        final var type2 = getConcreteType(member2.type());
        if (!type1.name().equals(type2.name())) {
            final int cmp = rankOf(type1) - rankOf(type2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return member1.name().compareTo(member2.name());
    }

    private static Type getConcreteType(final Type type) {
        return switch (type) {
            case ConcreteType concrete -> concrete;
            case ParameterizedType generated -> generated.getRawType();
            case ScalarTypeObjectArchetype scalar -> scalar.valueType();
            default -> type;
        };
    }

    @NonNullByDefault
    private static int rankOf(final Type type) {
        return switch (type) {
            case BitsTypeObjectArchetype bits -> RANK_VARIABLE_ARRAY;
            case Decimal64Type decimal64 -> RANK_FIXED_SIZE;
            case IdentityArchetype identity -> RANK_FIXED_SIZE;
            default -> {
                final var typeName = type.name();
                yield switch (typeName.packageName().toString()) {
                    case "" -> switch (typeName.simpleName()) {
                        case "byte[]" -> RANK_VARIABLE_ARRAY;
                        default -> throw unhandled(typeName);
                    };
                    case "java.lang" -> switch (typeName.simpleName()) {
                        case "Boolean", "Byte", "Short", "Integer", "Long" -> RANK_FIXED_SIZE;
                        case "Object" -> RANK_COMPOSITE;
                        case "String" -> RANK_VARIABLE_ARRAY;
                        default -> throw unhandled(typeName);
                    };
                    case "org.opendaylight.yangtools.binding" -> switch (typeName.simpleName()) {
                        case "BindingInstanceIdentifier" -> RANK_INSTANCE_IDENTIFIER;
                        default -> throw unhandled(typeName);
                    };
                    case "org.opendaylight.yangtools.yang.common" -> switch (typeName.simpleName()) {
                        case "Empty", "Uint8", "Uint16", "Uint32", "Uint64" -> RANK_FIXED_SIZE;
                        default -> throw unhandled(typeName);
                    };
                    default -> RANK_COMPOSITE;
                };
            }
        };
    }

    @NonNullByDefault
    private static VerifyException unhandled(final TypeName typeName) {
        return new VerifyException("Unhandled " + typeName);
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private Object readResolve() {
        return INSTANCE;
    }
}
