/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * By type member {@link Comparator} which provides sorting by type for members (variables)
 * in a generated class.
 *
 * @param <T> TypeMember type
 */
final class ByTypeMemberComparator implements Comparator<BuilderField> {
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
    @VisibleForTesting
    static final @NonNull ByTypeMemberComparator INSTANCE = new ByTypeMemberComparator();

    private ByTypeMemberComparator() {
        // Hidden on purpose
    }

    static Collection<BuilderField> sort(final Collection<BuilderField> input) {
        if (input.size() < 2) {
            return input;
        }

        final var ret = new ArrayList<>(input);
        ret.sort(INSTANCE);
        return ret;
    }

    @Override
    public int compare(final BuilderField member1, final BuilderField member2) {
        final Type type1 = getConcreteType(member1.method().getReturnType());
        final Type type2 = getConcreteType(member2.method().getReturnType());
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
                yield switch (typeName.packageName()) {
                    case "" -> switch (typeName.simpleName()) {
                        case "byte[]" -> RANK_VARIABLE_ARRAY;
                        default -> unhandled(typeName);
                    };
                    case "java.lang" -> switch (typeName.simpleName()) {
                        case "Boolean", "Byte", "Short", "Integer", "Long" -> RANK_FIXED_SIZE;
                        case "Object" -> RANK_COMPOSITE;
                        case "String" -> RANK_VARIABLE_ARRAY;
                        default -> unhandled(typeName);
                    };
                    case "org.opendaylight.yangtools.binding" -> switch (typeName.simpleName()) {
                        case "BindingInstanceIdentifier" -> RANK_INSTANCE_IDENTIFIER;
                        default -> unhandled(typeName);
                    };
                    case "org.opendaylight.yangtools.yang.common" -> switch (typeName.simpleName()) {
                        case "Empty", "Uint8", "Uint16", "Uint32", "Uint64" -> RANK_FIXED_SIZE;
                        default -> unhandled(typeName);
                    };
                    default -> RANK_COMPOSITE;
                };
            }
        };
    }

    @NonNullByDefault
    private static int unhandled(final JavaTypeName typeName) {
        throw new VerifyException("Unhandled " + typeName);
    }
}
