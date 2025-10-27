/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.annotations.Beta;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMember;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * By type member {@link Comparator} which provides sorting by type for members (variables)
 * in a generated class.
 *
 * @param <T> TypeMember type
 */
@Beta
final class ByTypeMemberComparator<T extends TypeMember> implements Comparator<T>, Serializable {
    @Serial
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

    private static final Set<Type> FIXED_TYPES = Set.of(
        BaseYangTypes.INT8_TYPE,
        BaseYangTypes.INT16_TYPE,
        BaseYangTypes.INT32_TYPE,
        BaseYangTypes.INT64_TYPE,
        BaseYangTypes.DECIMAL64_TYPE,
        BaseYangTypes.UINT8_TYPE,
        BaseYangTypes.UINT16_TYPE,
        BaseYangTypes.UINT32_TYPE,
        BaseYangTypes.UINT64_TYPE,
        BaseYangTypes.BOOLEAN_TYPE,
        BaseYangTypes.EMPTY_TYPE);

    /**
     * Singleton instance.
     */
    private static final @NonNull ByTypeMemberComparator<?> INSTANCE = new ByTypeMemberComparator<>();

    private ByTypeMemberComparator() {
        // Hidden on purpose
    }

    /**
     * Returns the one and only instance of this class.
     *
     * @return this comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends TypeMember> ByTypeMemberComparator<T> getInstance() {
        return (ByTypeMemberComparator<T>) INSTANCE;
    }

    public static <T extends TypeMember> Collection<T> sort(final Collection<T> input) {
        if (input.size() < 2) {
            return input;
        }

        final List<T> ret = new ArrayList<>(input);
        ret.sort(getInstance());
        return ret;
    }

    @Override
    public int compare(final T member1, final T member2) {
        final Type type1 = getConcreteType(member1.getReturnType());
        final Type type2 = getConcreteType(member2.getReturnType());
        if (!type1.getIdentifier().equals(type2.getIdentifier())) {
            final int cmp = rankOf(type1) - rankOf(type2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return member1.getName().compareTo(member2.getName());
    }

    @Serial
    @SuppressWarnings("static-method")
    private Object readResolve() {
        return INSTANCE;
    }

    private static Type getConcreteType(final Type type) {
        return switch (type) {
            case ConcreteType concrete -> concrete;
            case ParameterizedType generated -> generated.getRawType();
            case GeneratedTransferObject gto -> {
                var rootGto = gto;
                while (rootGto.getSuperType() != null) {
                    rootGto = rootGto.getSuperType();
                }
                for (var s : rootGto.getProperties()) {
                    if (TypeConstants.VALUE_PROP.equals(s.getName())) {
                        yield s.getReturnType();
                    }
                }
                yield type;
            }
            default -> type;
        };
    }

    private static int rankOf(final Type type) {
        if (FIXED_TYPES.contains(type) || BindingTypes.isIdentityType(type)) {
            return RANK_FIXED_SIZE;
        }
        if (type.equals(BaseYangTypes.STRING_TYPE) || type.equals(Types.BYTE_ARRAY)) {
            return RANK_VARIABLE_ARRAY;
        }
        if (type.equals(BaseYangTypes.INSTANCE_IDENTIFIER)) {
            return RANK_INSTANCE_IDENTIFIER;
        }
        if (type instanceof GeneratedTransferObject gto) {
            final TypeDefinition<?> typedef = topParentTransportObject(gto).getBaseType();
            if (typedef instanceof BitsTypeDefinition) {
                return RANK_VARIABLE_ARRAY;
            }
        }
        return RANK_COMPOSITE;
    }

    private static GeneratedTransferObject topParentTransportObject(final GeneratedTransferObject type) {
        GeneratedTransferObject ret = type;
        GeneratedTransferObject parent = ret.getSuperType();
        while (parent != null) {
            ret = parent;
            parent = ret.getSuperType();
        }
        return ret;
    }
}
