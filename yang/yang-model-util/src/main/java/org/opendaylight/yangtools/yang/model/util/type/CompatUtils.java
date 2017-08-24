/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.RangeMap;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

/**
 * Compatibility utilities for dealing with differences between the old parser's ExtendedType-driven type
 * representation versus the representation this package models.
 *
 * @deprecated This class is provided strictly for compatibility only. No new users should be introduced, as this class
 *             is scheduled for removal when its two OpenDaylight users, Java Binding v1 and YANG JMX Bindings are
 *             removed.
 */
@Deprecated
public final class CompatUtils {
    private CompatUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * This package's type hierarchy model generates a type which encapsulates the default value and units for leaves.
     * Java Binding specification is implemented in a way, where it needs to revert this process if the internal
     * declaration has not restricted the type further -- which is not something available via
     * {@link TypeDefinition#getBaseType()}.
     *
     * <p>
     * Here are the possible scenarios:
     *
     * <pre>
     * leaf foo {
     *     type uint8 {
     *         range 1..2;
     *     }
     * }
     * </pre>
     * The leaf type's schema path does not match the schema path of the leaf. We do NOT want to strip it, as
     * we need to generate an inner class to hold the restrictions.
     *
     * <pre>
     * leaf foo {
     *     type uint8 {
     *         range 1..2;
     *     }
     *     default 1;
     * }
     * </pre>
     * The leaf type's schema path will match the schema path of the leaf. We do NOT want to strip it, as we need
     * to generate an inner class to hold the restrictions.
     *
     * <pre>
     * leaf foo {
     *     type uint8;
     *     default 1;
     * }
     * </pre>
     * The leaf type's schema path will match the schema path of the leaf. We DO want to strip it, as we will deal
     * with the default value ourselves.
     *
     * <pre>
     * leaf foo {
     *     type uint8;
     * }
     * </pre>
     * The leaf type's schema path will not match the schema path of the leaf. We do NOT want to strip it.
     *
     * <p>
     * The situation is different for types which do not have a default instantiation in YANG: leafref, enumeration,
     * identityref, decimal64, bits and union. If these types are defined within this leaf's statement, a base type
     * will be instantiated. If the leaf defines a default statement, this base type will be visible via getBaseType().
     *
     * <pre>
     * leaf foo {
     *     type decimal64 {
     *         fraction-digits 2;
     *     }
     * }
     * </pre>
     * The leaf type's schema path will not match the schema path of the leaf, and we do not want to strip it, as it
     * needs to be generated.
     *
     * <pre>
     * leaf foo {
     *     type decimal64 {
     *         fraction-digits 2;
     *     }
     *     default 1;
     * }
     * </pre>
     * The leaf type's schema path will match the schema path of the leaf, and we DO want to strip it.
     *
     * @param leaf Leaf for which we are acquiring the type
     * @return Potentially base type of the leaf type.
     */
    @Nonnull public static TypeDefinition<?> compatLeafType(@Nonnull final LeafSchemaNode leaf) {
        final TypeDefinition<?> leafType = leaf.getType();
        Preconditions.checkNotNull(leafType);

        if (!leaf.getPath().equals(leafType.getPath())) {
            // Old parser semantics, or no new default/units defined for this leaf
            return leafType;
        }

        // We are dealing with a type generated for the leaf itself
        final TypeDefinition<?> baseType = leafType.getBaseType();
        Preconditions.checkArgument(baseType != null, "Leaf %s has type for leaf, but no base type", leaf);

        if (leaf.getPath().equals(baseType.getPath().getParent())) {
            // Internal instantiation of a base YANG type (decimal64 and similar)
            return baseType;
        }

        // At this point we have dealt with the easy cases. Now we need to perform per-type checking if there are no
        // new constraints introduced by this type. If there were not, we will return the base type.
        if (leafType instanceof BinaryTypeDefinition) {
            return baseTypeIfNotConstrained((BinaryTypeDefinition) leafType);
        } else if (leafType instanceof DecimalTypeDefinition) {
            return baseTypeIfNotConstrained((DecimalTypeDefinition) leafType);
        } else if (leafType instanceof InstanceIdentifierTypeDefinition) {
            return baseTypeIfNotConstrained((InstanceIdentifierTypeDefinition) leafType);
        } else if (leafType instanceof IntegerTypeDefinition) {
            return baseTypeIfNotConstrained((IntegerTypeDefinition) leafType);
        } else if (leafType instanceof StringTypeDefinition) {
            return baseTypeIfNotConstrained((StringTypeDefinition) leafType);
        } else if (leafType instanceof UnsignedIntegerTypeDefinition) {
            return baseTypeIfNotConstrained((UnsignedIntegerTypeDefinition) leafType);
        } else {
            // Other types cannot be constrained, return the base type
            return baseType;
        }
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final BinaryTypeDefinition type) {
        final BinaryTypeDefinition base = type.getBaseType();
        return baseTypeIfNotConstrained(type, type.getLengthConstraints(), base, base.getLengthConstraints());
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final DecimalTypeDefinition type) {
        final DecimalTypeDefinition base = type.getBaseType();
        return baseTypeIfNotConstrained(type, type.getRangeConstraints(), base, base.getRangeConstraints());
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final InstanceIdentifierTypeDefinition type) {
        final InstanceIdentifierTypeDefinition base = type.getBaseType();
        return type.requireInstance() == base.requireInstance() ? base : type;
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final IntegerTypeDefinition type) {
        final IntegerTypeDefinition base = type.getBaseType();
        return baseTypeIfNotConstrained(type, type.getRangeConstraints(), base, base.getRangeConstraints());
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final StringTypeDefinition type) {
        final StringTypeDefinition base = type.getBaseType();
        final List<PatternConstraint> patterns = type.getPatternConstraints();
        final RangeMap<Integer, ConstraintMetaDefinition> lengths = type.getLengthConstraints();

        if ((patterns.isEmpty() || patterns.equals(base.getPatternConstraints()))
                && (lengths.asMapOfRanges().isEmpty() || lengths.equals(base.getLengthConstraints()))) {
            return base;
        }

        return type;
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final UnsignedIntegerTypeDefinition type) {
        final UnsignedIntegerTypeDefinition base = type.getBaseType();
        return baseTypeIfNotConstrained(type, type.getRangeConstraints(), base, base.getRangeConstraints());
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final TypeDefinition<?> type,
            final List<?> typeConstraints, final TypeDefinition<?> base, final List<?> baseConstraints) {
        return typeConstraints.isEmpty() || typeConstraints.equals(baseConstraints) ? base : type;
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final TypeDefinition<?> type,
            final RangeMap<?, ?> typeConstraints, final TypeDefinition<?> base, final RangeMap<?, ?> baseConstraints) {
        return typeConstraints.asMapOfRanges().isEmpty() || typeConstraints.equals(baseConstraints) ? base : type;
    }
}
